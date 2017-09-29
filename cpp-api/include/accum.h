/*
 *
 *  Managed Data Structures
 *  Copyright © 2016 Hewlett Packard Enterprise Development Company LP.
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 *  As an exception, the copyright holders of this Library grant you permission
 *  to (i) compile an Application with the Library, and (ii) distribute the 
 *  Application containing code generated by the Library and added to the 
 *  Application during this compilation process under terms of your choice, 
 *  provided you also meet the terms and conditions of the Application license.
 *
 */

/*
 * iso_ctxt.h
 *
 *  Created on: Mar 16, 2015
 *      Author: evank
 */

#ifndef MDS_ACCUM_H_
#define MDS_ACCUM_H_

#include "iso_ctxt.h"
#include "ruts/weak_key.h"
#include <unordered_set>

namespace mds {
  template <typename A, void(*RBFn)(A&,const A&) = __rollback_by_sub<A> >
  class accumulator {
    struct node {
      const std::shared_ptr<node> parent;
      A accum;
      template <typename...Args>
      node(const std::shared_ptr<node> &pnode,
           Args&&...args)
        : parent{pnode},
          accum{std::forward<Args>(args)...}
      {}

      void roll_back() {
        for (std::shared_ptr<node> n = parent;
             n != nullptr;
             n = n->parent)
          {
            RBFn(n->accum, accum);
          }
      }

      template <typename Fn, typename...Args>
      auto get(Fn&& fn, Args&&...args) const {
        return std::forward<Fn>(fn)(accum, std::forward<Args>(args)...);
      }
    };

    struct state : std::enable_shared_from_this<state> {
      const std::function<std::shared_ptr<node>(const std::shared_ptr<node>)> creation_fn;
      const task top_level_task;
      const std::shared_ptr<node> top_level;
      /*
       * Mutable so that we can erase expired keys during get.
       */
      using key = ruts::weak_key<weak_handle<task>>;
      mutable std::unordered_set<key> writers;
      mutable std::unordered_map<key, std::shared_ptr<node>> nodes;
      using std::enable_shared_from_this<state>::shared_from_this;
      /*
       * I'm assuming that the number of readers will be relatively
       * small, so it's probably not worth using a shared_mutex.  And
       * I'm going to lock the whole accumulator so that I can remove
       * nodes when their keys expire.
       */
      mutable std::mutex mtx;
      std::shared_ptr<node> node_for(task &t) {
        key k{t};
        std::shared_ptr<node> n = nodes[k];
        if (n == nullptr) {
          task parent = t.parent();
          if (parent == nullptr) {
            /*
             * I used the parent context's top-level task in the Java
             * version, but I'm not sure why I didn't use this context's
             * creation task.
             */
            parent = t.context().parent().top_level_task();
          }
          std::shared_ptr<node> pn = node_for(parent);
          n = std::make_shared<node>(pn);
          /*
           * We do the lookup again, because the recursive call may have
           * invalidated the reference we would have gotten back earlier.
           */
          nodes[k] = n;
          /*
           * If the accumulator's gone, we don't want to hold onto its nodes.
           */
          std::weak_ptr<node> wn = n;
          std::weak_ptr<state> ws = shared_from_this();
          t.on_prepare_for_redo([wn, ws](const task &t){
              auto n = wn.lock();
              if (n != nullptr) {
                n->roll_back();
              }
              auto s = ws.lock();
              if (s != nullptr) {
                /*
                 * If the state is still there when we redo the task,
                 * the task is no longer a writer and we don't use its
                 * node.
                 */
                s->nodes.erase(t);
                s->writers.erase(t);
              }
            });
        }
        return n;
      }
      template <typename...Args>
        state(Args&&...args)
        : creation_fn([args...](const std::shared_ptr<node> &pnode){
            return std::make_shared<node>(pnode, std::forward<Args>(args)...);
          }),
        top_level_task{iso_ctxt::global().top_level_task()},
        top_level{creation_fn(nullptr)}
        {
          nodes[top_level_task] = top_level;
        }
        template <typename Fn, typename...Args>
          auto get(Fn&& fn, Args&&...args) const {
          std::lock_guard<std::mutex> lck(mtx);
          task me = task::current();
          for (auto kp = writers.begin(); kp != writers.end(); ) {
            task t = kp->lock();
            if (t == nullptr) {
              nodes.erase(*kp);
              kp = writers.erase(kp);
            } else {
              me.depends_on(t);
              kp++;
            }
          }
          return top_level->get(std::forward<Fn>(fn), std::forward<Args>(args)...);
        }
        template <typename Fn, typename...Args>
          void add_using(Fn&& fn, Args&&...args) {
          std::lock_guard<std::mutex> lck(mtx);
          task me = task::current();
          writers.insert(me);
          for (std::shared_ptr<node> n = node_for(me);
               n != nullptr;
               n = n->parent)
            {
              std::forward<Fn>(fn)(n->accum, std::forward<Args>(args)...);
            }
        }
    };

    std::shared_ptr<state> _state;


  public:
    accumulator(const accumulator &) = default;
    accumulator(accumulator &) = default;

    accumulator() : _state(std::make_shared<state>()) {}

    template <typename First, typename...Args,
              typename =std::enable_if_t<!std::is_same<std::decay_t<First>,
                                                       accumulator>::value> >
    explicit accumulator(First &&first, Args&&...args)
      : _state(std::make_shared<state>(std::forward<First>(first),
                                       std::forward<Args>(args)...))
    {
    }
    accumulator &operator =(const accumulator &) = default;
    accumulator &operator =(accumulator &) = default;

    
    template <typename Fn, typename...Args>
    auto get(Fn&& fn, Args&&...args) const {
      return _state->get(std::forward<Fn>(fn), std::forward<Args>(args)...);
    }
    auto get() const {
      return get([](const A &accum) { return accum; });
    }

    operator A() const {
      return get();
    }

    template <typename Fn, typename...Args>
    void add_using(Fn&& fn, Args&&...args) {
      _state->add_using(std::forward<Fn>(fn), std::forward<Args>(args)...);
    }

    template <typename T>
    void add(const T &t) {
      add_using([&t](A &a) {
          a += t;
        });
    }

    template <typename T>
    void sub(const T &t) {
      add_using([&t](A &a) {
          a -= t;
        });
    }

    template <typename T>
    void operator +=(const T &t) {
      add(t);
    }

    template <typename T>
    void operator -=(const T &t) {
      sub(t);
    }

    void inc() {
      add(1);
    }
    void operator ++() {
      inc();
    }
    void operator ++(int) {
      inc();
    }
      
    void dec() {
      sub(1);
    }
    void operator --() {
      dec();
    }
    void operator --(int) {
      dec();
    }
      
  // unsigned test() {
  //   static accumulator<unsigned> a;
  //   a += 5;
  //   a--;
  //   return a;
  // }

    
  };

  class summary_statistics {
    using size_t = std::size_t;
    struct state {
      size_t _count = 0;
      double _sum = 0;
      double _sum_sq = 0;

      void operator -=(const state &other) {
        _count -= other._count;
        _sum -= other._sum;
        _sum_sq -= other._sum_sq;
      }

      void add(double val, size_t weight) {
        _count += weight;
        _sum += val*weight;
        _sum_sq += val*val*weight;
      }

      size_t count() const {
        return _count;
      }

      double sum() const {
        return _sum;
      }

      double mean() const {
        return _sum/_count;
      }

      double sum_sq() const {
        return _sum_sq;
      }

      double mean_sq() const {
        return _sum_sq/_count;
      }

      double rms() const {
        return std::sqrt(mean_sq());
      }

      double variance() const {
        double m = mean();
        return (1.0/(_count-1))*(_sum_sq-2*m*_sum+m*m*_count);
      }

      double std_dev() const {
        return std::sqrt(variance());
      }

      double std_err() const {
        return std_dev()/std::sqrt(_count);
      }

      double high_95() const {
        return mean()+std_err()*1.96;
      }

      double low_95() const {
        return mean()-std_err()*1.96;
      }
    };

    accumulator<state> accum;
  public:
    void add(double val, std::size_t weight = 1) {
      accum.add_using([&](state &s) {
          s.add(val, weight);
        });
    }

    void operator +=(double val) {
      add(val);
    }

    std::size_t count() const {
      return accum.get(std::mem_fn(&state::count));
    }

    double sum() const {
      return accum.get(std::mem_fn(&state::sum));
    }

    double mean() const {
      return accum.get(std::mem_fn(&state::mean));
    }

    double sum_sq() const {
      return accum.get(std::mem_fn(&state::sum_sq));
    }

    double mean_sq() const {
      return accum.get(std::mem_fn(&state::mean_sq));
    }

    double rms() const {
      return accum.get(std::mem_fn(&state::rms));
    }

    double variance() const {
      return accum.get(std::mem_fn(&state::variance));
    }

    double std_dev() const {
      return accum.get(std::mem_fn(&state::std_dev));
    }

    double std_err() const {
      return accum.get(std::mem_fn(&state::std_err));
    }

    double high_95() const {
      return accum.get(std::mem_fn(&state::high_95));
    }
    double low_95() const {
      return accum.get(std::mem_fn(&state::low_95));
    }
  };

#endif // MDS_ACCUM_H_  
}
