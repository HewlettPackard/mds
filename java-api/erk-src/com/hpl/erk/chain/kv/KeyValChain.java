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

package com.hpl.erk.chain.kv;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.hpl.erk.chain.Chain;
import com.hpl.erk.chain.Flow;
import com.hpl.erk.func.NullaryFunc;
import com.hpl.erk.func.Functions;
import com.hpl.erk.func.KVTransformer;
import com.hpl.erk.func.Pair;
import com.hpl.erk.func.Predicate;
import com.hpl.erk.func.SinkFull;
import com.hpl.erk.func.SourceExhausted;
import com.hpl.erk.func.UnaryFunc;
import com.hpl.erk.iter.ConstIterator;

public abstract class KeyValChain<From, K, V>  implements Iterable<Pair<? extends K, ? extends V>> {
  public static final SourceExhausted SOURCE_EXHAUSTED = SourceExhausted.INSTANCE;
  public static final SinkFull SINK_FULL = SinkFull.INSTANCE;

  public boolean complete;

  protected KeyValChain(boolean complete) {
    this.complete = complete;
  }

  public abstract <H,T extends From> KeyValChain<H,K,V> prepend(Chain<H,T> chain); 
  public abstract Context createContext();
  
  public static interface Receiver<K,V> {
    boolean receive(K key, V value);
    void close();
  }
  
  public static abstract class FinalReceiver<K,V> implements Receiver<K,V> {
    @Override
    public void close() {
      // do nothing
    }
  }
  
  public static abstract class ChainedReceiver<Kin,Vin, Kout, Vout> implements Receiver<Kin, Vin> {
    protected final Receiver<? super Kout, ? super Vout> sink;

    protected ChainedReceiver(Receiver<? super Kout, ? super Vout> sink) {
      this.sink = sink;
    }
    
    @Override
    public void close() {
      sink.close();
    }
  }
  
  public abstract Flow pipeInto(Receiver<? super K, ? super V> sink);
  
  public int expectedSize() {
    return Integer.MAX_VALUE;
  }
  
  @Override
  public final Iterator<Pair<? extends K, ? extends V>> iterator() {
    final Context context = createContext();
    return context.iterator();
  }
  
  public void fill(Receiver<? super K, ? super V> sink) {
    Flow flow = pipeInto(sink);
    flow.perform();
  }

  
  public abstract class Context implements Iterable<Pair<? extends K, ? extends V>> {
    public abstract Pair<? extends K, ? extends V> produce() throws SourceExhausted;
    
    public final void exhaustedIf(boolean exhaustedp) throws SourceExhausted {
      if (exhaustedp) {
        throw SOURCE_EXHAUSTED;
      }
    }
    
    @Override
    public Iterator<Pair<? extends K, ? extends V>> iterator() {
      return new ConstIterator<Pair<? extends K,? extends V>>() {
        boolean done = false;
        Pair<? extends K, ? extends V> next = getNext();


        private Pair<? extends K, ? extends V> getNext() {
          if (done) {
            return null;
          }
          try {
            return produce();
          } catch (SourceExhausted e) {
            done = true;
            return null;
          }
        }

        @Override
        public boolean hasNext() {
          return !done;
        }

        @Override
        public Pair<? extends K, ? extends V> next() {
          Pair<? extends K, ? extends V> val = next;
          next = getNext();
          return val;
        }};
    }

  }

  protected <C extends KeyValLink<From, ? super K, ? super V, ?, ?>> C activate(C link) {
    link.activate();
    return link;
  }
  
  protected <C extends JoinLink<From, ? super K, ? super V, ?>> C activate(C link) {
    link.activate();
    return link;
  }
  
  protected Pair<K,V> pair(K key, V val) {
    return new Pair<>(key, val);
  }

//  protected KVWrappedChain<From, K, V> wrap(Chain<From, Pair<K,V>> chain) {
//    return new KVWrappedChain<>(chain);
//  }

  // -----------------------
  public <OutK, OutV> KeyValChain<From, OutK, OutV> map(NullaryFunc<? extends KVTransformer<? super K, ? super V, ? extends OutK, ? extends OutV>> creator) {
    //    return activate(new KeyValTransformerLink<>(this, creator));
    KeyValTransformerLink<From,K,V,OutK,OutV> c = new KeyValTransformerLink<>(this, creator);
    activate(c);
    return c;
  }
  
  public <OutK, OutV> KeyValChain<From, OutK, OutV> map(KVTransformer<? super K, ? super V, ? extends OutK, ? extends OutV> transformer) {
    return map(Functions.always(transformer));
  }  
  
  public Chain<From,K> keys() {
    //    return activate(new JoinLink<From, K, V, K>(this) {
    JoinLink<From, K, V, K> c = new JoinLink<From,K,V,K>(this) {
        @Override
        public int expectedSize() {
          return pred.expectedSize();
        }
        @Override
        public Flow pipeInto(final Chain.Receiver<? super K> sink) {
          return pred.pipeInto(new KeyValChain.Receiver<K, V>() {
              @Override
              public boolean receive(K key, V value) {
                return sink.receive(key);
              }
              @Override
              public void close() {
                sink.close();
              }
            });
        }
        @Override
        public Context createContext() {
          return new Context() {
            @Override
            public K produce() throws SourceExhausted {
              return source.produce().key;
            }
          };
        }};
    activate(c);
    return c;
  }
  public Chain<From,V> values() {
    //return activate(new JoinLink<From, K, V, V>(this) {
    JoinLink<From, K, V, V> c = new JoinLink<From,K,V,V>(this) {
        @Override
        public int expectedSize() {
          return pred.expectedSize();
        }
        @Override
        public Flow pipeInto(final Chain.Receiver<? super V> sink) {
          return pred.pipeInto(new KeyValChain.Receiver<K, V>() {
              @Override
              public boolean receive(K key, V value) {
                return sink.receive(value);
              }
              public void close() {
                sink.close();
              };
            });
        }

        @Override
        public Context createContext() {
          return new Context() {
            @Override
            public V produce() throws SourceExhausted {
              return source.produce().value;
            }
          };
        }
      };
    activate(c);
    return c;
  }


  public <M extends Map<? super K, ? super V>> MapSink<From,K,V,M> into(M map) {
    //    return activate(new MapSink<>(this, map));
    MapSink<From,K,V,M> c = new MapSink<>(this, map);
    activate(c);
    return c;
  }

  public MapSink<From,K,V,Map<K,V>> intoMap() {
    return into((Map<K,V>)new HashMap<K, V>());
  }

  public Map<K,V> asMap() {
    return intoMap().val();
  }

  public KeyValChain<From,K,V> removeKey(NullaryFunc<? extends Predicate<? super K>> creator) {
    //    return activate(new FilterKeyLink<>(this, creator, false));
    FilterKeyLink<From,K,V> c = new FilterKeyLink<>(this, creator, false);
    activate(c);
    return c;
  }

  public KeyValChain<From, K, V> removeKey(Predicate<? super K> pred) {
    return removeKey(Functions.always(pred));
  }

  public KeyValChain<From,K,V> removeValue(NullaryFunc<? extends Predicate<? super V>> creator) {
    //    return activate(new FilterValLink<>(this, creator, false));
    FilterValLink<From,K,V> c = new FilterValLink<>(this, creator, false);
    activate(c);
    return c;
  }

  public KeyValChain<From, K, V> removeValue(Predicate<? super V> pred) {
    return removeValue(Functions.always(pred));
  }

  public KeyValChain<From,K,V> keepKey(NullaryFunc<? extends Predicate<? super K>> creator) {
    //    return activate(new FilterKeyLink<>(this, creator, true));
    FilterKeyLink<From,K,V> c = new FilterKeyLink<>(this, creator, true);
    activate(c);
    return c;
  }

  public KeyValChain<From, K, V> keepKey(Predicate<? super K> pred) {
    return keepKey(Functions.always(pred));
  }

  public KeyValChain<From,K,V> keepValue(NullaryFunc<? extends Predicate<? super V>> creator) {
    //    return activate(new FilterValLink<>(this, creator, true));
    FilterValLink<From,K,V> c = new FilterValLink<>(this, creator, true);
    activate(c);
    return c;
  }

  public KeyValChain<From, K, V> keepValue(Predicate<? super V> pred) {
    return keepValue(Functions.always(pred));
  }

  public <K2> KeyValChain<From, K2, V> mapKey(NullaryFunc<? extends UnaryFunc<? super K, ? extends K2>> creator) {
    //    return activate(new KeyTransformerLink<>(this, creator));
    KeyTransformerLink<From,K,K2,V> c = new KeyTransformerLink<>(this, creator);
    activate(c);
    return c;
  }

  public <K2> KeyValChain<From, K2, V> mapKey(UnaryFunc<? super K, ? extends K2> transformer) {
    return mapKey(Functions.always(transformer));
  }

  public <V2> KeyValChain<From, K, V2> mapValue(NullaryFunc<? extends UnaryFunc<? super V, ? extends V2>> creator) {
    //    return activate(new ValueTransformerLink<>(this, creator));
    ValueTransformerLink<From,K,V,V2> c = new ValueTransformerLink<>(this, creator);
    activate(c);
    return c;
  }

  public <V2> KeyValChain<From, K, V2> mapValue(UnaryFunc<? super V, ? extends V2> transformer) {
    return mapValue(Functions.always(transformer));
  }

  public KeyValChain<From, K, V> monitor(final String format, final PrintStream out) {
    //    return activate(new KeyValLink<From, K, V, K, V>(this) {
    KeyValLink<From,K,V,K,V> c = new KeyValLink<From, K, V, K, V>(this) {
        @Override
        public int expectedSize() {
          return pred.expectedSize();
        }
        @Override
        public Flow pipeInto(final Receiver<? super K, ? super V> sink) {
          return pred.pipeInto(new ChainedReceiver<K, V,K,V>(sink) {
              @Override
              public boolean receive(K key, V value) {
                out.format(format, key, value);
                return sink.receive(key, value);
              }});
        }

        @Override
        public Context createContext() {
          return new Context() {
            @Override
            public Pair<? extends K, ? extends V> produce() throws SourceExhausted {
              Pair<? extends K, ? extends V> pair = source.produce();
              out.format(format, pair.key, pair.value);
              return pair;
            }
          };
        }
      };
    activate(c);
    return c;
  }
  
  public Chain<From, Pair<? extends K,? extends V>> pairs() {
    //    return activate(new JoinLink<From, K, V, Pair<? extends K,? extends V>>(this) {
    JoinLink<From,K,V,Pair<? extends K,? extends V>>
      c = new JoinLink<From, K, V, Pair<? extends K,? extends V>>(this) {
        @Override
        public Flow pipeInto(final Chain.Receiver<? super Pair<? extends K, ? extends V>> sink) {
          return pred.pipeInto(new KeyValChain.Receiver<K, V>() {

              @Override
              public boolean receive(K key, V value) {
                return sink.receive(new Pair<>(key, value));
              }
              @Override
              public void close() {
                sink.close();
              }
            });
        }

        @Override
        public Context createContext() {
          return new Context() {
            @Override
            public Pair<? extends K, ? extends V> produce() throws SourceExhausted {
              return source.produce();
            }
          };
        }

      };
    activate(c);
    return c;
  }
  
  public KeyValChain<From, K,V> whenKey(final Predicate<? super K> predicate) {
    //    return activate(new KeyValLink<From, K, V, K, V>(this) {
    KeyValLink<From,K,V,K,V> c = new KeyValLink<From, K, V, K, V>(this) {

        @Override
        public Context createContext() {
          return new Context() {
            @Override
            public Pair<? extends K, ? extends V> produce() throws SourceExhausted {
              while (true) {
                Pair<? extends K, ? extends V> pair = source.produce();
                if (predicate.test(pair.key)) {
                  return pair;
                }
              }
            }
          };
        }

        @Override
        public Flow pipeInto(final Receiver<? super K, ? super V> sink) {
          return pred.pipeInto(new ChainedReceiver<K, V, K, V>(sink) {
              @Override
              public boolean receive(K key, V value) {
                if (predicate.test(key)) {
                  return sink.receive(key, value);
                }
                return true;
              }
            });
        }

      };
    activate(c);
    return c;
  }
  public KeyValChain<From, K,V> whenValue(final Predicate<? super V> predicate) {
    //    return activate(new KeyValLink<From, K, V, K, V>(this) {
    KeyValLink<From,K,V,K,V> c = new KeyValLink<From, K, V, K, V>(this) {
        @Override
        public Context createContext() {
          return new Context() {
            @Override
            public Pair<? extends K, ? extends V> produce() throws SourceExhausted {
              while (true) {
                Pair<? extends K, ? extends V> pair = source.produce();
                if (predicate.test(pair.value)) {
                  return pair;
                }
              }
            }
          };
        }

        @Override
        public Flow pipeInto(final Receiver<? super K, ? super V> sink) {
          return pred.pipeInto(new ChainedReceiver<K, V, K, V>(sink) {
              @Override
              public boolean receive(K key, V value) {
                if (predicate.test(value)) {
                  return sink.receive(key, value);
                }
                return true;
              }
            });
        }
      };
    activate(c);
    return c;
  }

}
