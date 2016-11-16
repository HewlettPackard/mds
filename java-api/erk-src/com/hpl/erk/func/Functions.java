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

package com.hpl.erk.func;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

import com.hpl.erk.chain.Chain;
import com.hpl.erk.chain.exceptions.ChainException;
import com.hpl.erk.files.FileOption;
import com.hpl.erk.files.FileUtil;
import com.hpl.erk.files.FileOption.OptionSet;
import com.hpl.erk.iter.ConstIterator;

public class Functions {
  private static final SourceExhausted SOURCE_EXHAUSTED = SourceExhausted.INSTANCE;

  public static <X> Reducer<X, Integer> count() {
    return new Reducer<X, Integer>() {
      @Override
      public Integer update(Integer old, X current) {
        if (old == null) {
          return 1;
        }
        return old+1;
      }
    };
  }

  public static Reducer<Integer, Integer> intSum() {
    return new Reducer<Integer, Integer>() {
      @Override
      public Integer update(Integer old, Integer current) {
        if (old == null) {
          return current;
        }
        return old+current;
      }

    };
  }
  
  public static <X extends Comparable<? super X>> Reducer<X,X> max() {
    return new Reducer<X, X>() {
      @Override
      public X update(X old, X current) {
        if (old == null || old.compareTo(current) < 0) {
          return current;
        }
        return old;
      }
    };
  }
  public static <X extends Comparable<? super X>> Reducer<X,X> min() {
    return new Reducer<X, X>() {
      @Override
      public X update(X old, X current) {
        if (old == null || old.compareTo(current) > 0) {
          return current;
        }
        return old;
      }
    };
  }

  public static <X> Reducer<X,X> max(final Comparator<? super X> cptr) {
    return new Reducer<X,X>() {
      @Override
      public X update(X old, X current) {
        if (old == null || cptr.compare(current, old) > 0) {
          return current;
        }
        return old;
      }
    };
  }

  public static <X> Reducer<X,X> min(final Comparator<? super X> cptr) {
    return new Reducer<X,X>() {
      @Override
      public X update(X old, X current) {
        if (old == null || cptr.compare(current, old) < 0) {
          return current;
        }
        return old;
      }
    };
  }

  public static <X> Comparator<X> naturalOrder() { 
    return new Comparator<X>() {
//      @SuppressWarnings("unchecked")
      @Override
      public int compare(X o1, X o2) {
        if (o1 == o2) {
          return 0;
        }
        if (o1 != null) {
          @SuppressWarnings("unchecked")
          final Comparable<? super X> c1 = (Comparable<? super X>)o1;
          return c1.compareTo(o2);
        } else {
          @SuppressWarnings("unchecked")
          final Comparable<? super X> c2 = (Comparable<? super X>)o2;
          return -c2.compareTo(o1);
        }
      }};
  }
  
  public static <X> Comparator<X> opposite(final Comparator<X> cptr) {
    return new Comparator<X>() {
      @Override
      public int compare(X o1, X o2) {
        return cptr.compare(o2, o1);
      }};
  }
  
  public static Comparator<Object> byHashCode() {
    return new Comparator<Object>() {
      @Override
      public int compare(Object o1, Object o2) {
        return o1==o2 ? 0 
                      : o1==null ? 1 
                                 : o2==null ? -1 
                                            : Integer.compare(o1.hashCode(), o2.hashCode());
      }
    };
  }
  
  public static <X,Y extends Comparable<Y>> Comparator<X> mappedComparator(final UnaryFunc<? super X, ? extends Y> transformer) {
    return new Comparator<X>() {
      @Override
      public int compare(X o1, X o2) {
        final Y x = transformer.call(o1);
        if (x == null) {
          return -1;
        }
        return x.compareTo(transformer.call(o2));
      }};
  }
  public static <X,Y> Comparator<X> mappedComparator(final UnaryFunc<? super X, ? extends Y> transformer, final Comparator<? super Y> cptr) {
    return new Comparator<X>() {
      @Override
      public int compare(X o1, X o2) {
        return cptr.compare(transformer.call(o1), transformer.call(o2));
      }};
  }
  


  /** generates Integers in range [min,max] *inclusive* */
  public static IndexedGenerator<Integer> ints(final int min, final int max) {
    return new IndexedGenerator<Integer>() {
      @Override
      public Integer generate(int index) throws SourceExhausted {
        int val = index+min;
        if (val > max) {
          throw SOURCE_EXHAUSTED;
        }
        return val;
      }
    };
  }

  public static Generator<Integer> intsFrom(int min) {
    return ints(min, Integer.MAX_VALUE);
  }

  public static <K,V,X extends K, Y extends V> Generator<Pair<K,V>> foreach(final Map<X,Y> map) {
    return new Generator<Pair<K,V>>() {
      Iterator<Map.Entry<X,Y>> iterator = map.entrySet().iterator();

      @Override
      public Pair<K, V> generate() throws SourceExhausted {
        if (!iterator.hasNext()) {
          throw SOURCE_EXHAUSTED;
        }
        Map.Entry<X,Y> entry = iterator.next();
        return new Pair<K, V>(entry.getKey(), entry.getValue());
      }
    };
  }

  public static <X,Y extends X> Generator<X> foreach(final Iterable<Y> iterable) {
    return new Generator<X>() {
      Iterator<Y> iterator = iterable.iterator();
      @Override
      public X generate() throws SourceExhausted {
        if (!iterator.hasNext()) {
          throw SOURCE_EXHAUSTED;
        }
        return iterator.next();
      }

    };
  }

  public static <X> IndexedGenerator<X> foreach(final X[] array) {
    return new IndexedGenerator<X>() {
      @Override
      public X generate(int index) throws SourceExhausted {
        if (index > array.length) {
          throw SOURCE_EXHAUSTED;
        }
        return array[index];
      }};
  }

  public static Generator<Integer> foreach(final int[] array) {
    return new IndexedGenerator<Integer>() {
      @Override
      public Integer generate(int index) throws SourceExhausted {
        if (index >= array.length) {
          throw SOURCE_EXHAUSTED;
        }
        return array[index];
      }};
  }
  public static IndexedGenerator<Long> foreach(final long[] array) {
    return new IndexedGenerator<Long>() {
      @Override
      public Long generate(int index) throws SourceExhausted {
        if (index >= array.length) {
          throw SOURCE_EXHAUSTED;
        }
        return array[index];
      }};
  }
  public static IndexedGenerator<Double> foreach(final double[] array) {
    return new IndexedGenerator<Double>() {
      @Override
      public Double generate(int index) throws SourceExhausted {
        if (index >= array.length) {
          throw SOURCE_EXHAUSTED;
        }
        return array[index];
      }};
  }



  public static Consumer<Object> streamConsumer(final PrintStream out) {
    return new Consumer<Object>() {
      @Override
      public boolean see(Object val) {
        out.println(val);
        return true;
      }
    };
  }
  
  public static <T> Consumer<T> consumer(final Collection<T> coll) {
    return new Consumer<T>() {
      @Override
      public boolean see(T val) {
        coll.add(val);
        return true;
      }
    };
  }
  
  public static <T> Consumer<T> fillArray(final T[] array) {
    return new Consumer<T>() {
      int next = 0;
      final int end = array.length;
      @Override
      public boolean see(T val) {
        if (next >= end) {
          return false;
        }
        array[next++] = val;
        return next < end;
      }};
  }
  
  /** generates Integers in range [min,max] *inclusive*. */
  public static Generator<Integer> randomInts(final int min, final int max) {
    return new Generator<Integer>() {
      @Override
      public Integer generate() throws SourceExhausted {
        return ThreadLocalRandom.current().nextInt(min, max+1);
      }
    };
  }
  
  public static Generator<Double> randomDoubles() {
    return new Generator<Double>() {
      @Override
      public Double generate() throws SourceExhausted {
        return ThreadLocalRandom.current().nextDouble();
      }
    };
  }
  public static Generator<Double> gaussian(final double mean, final double stdDev) {
    return new Generator<Double>() {
      @Override
      public Double generate() throws SourceExhausted {
        return mean+stdDev*ThreadLocalRandom.current().nextGaussian();
      }
    };
  }
  public static Generator<Boolean> randomBooleans(final double prob) {
    return new Generator<Boolean>() {
      @Override
      public Boolean generate() throws SourceExhausted {
        return ThreadLocalRandom.current().nextDouble() < prob;
      }
    };
  }
  
  public static <E extends Enum<E>> Generator<E> random(final Class<E> clss) {
    return randomFrom(clss.getEnumConstants());
  }
  
  public static <T, U extends T> Generator<T> randomFrom(final U[] array) {
    return new Generator<T>() {
      final int n = array.length;
      @Override
      public T generate() throws SourceExhausted {
        return array[ThreadLocalRandom.current().nextInt(n)];
      }};
  }

  /** create() always returns val.  Doesn't create instances/clones. */
  public static <X> NullaryFunc<X> always(final X val) {
    return new NullaryFunc<X>() {
      @Override
      public X call() {
        return val;
      }};
  }
  
  public static <X> NullaryFunc<X> makeNew(final Class<? extends X> clss) {
    return new NullaryFunc<X>() {
      @Override
      public X call() {
        try {
          return clss.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
          throw new ChainException(e);
        }
      }};
  }
  
  public static <X,Y> NullaryFunc<X> makeNew(final Class<? extends X> clss, final Y arg) {
    return new NullaryFunc<X>() {
      @Override
      public X call() {
        try {
          Constructor<? extends X> ctor = clss.getConstructor(arg.getClass());
          return ctor.newInstance(arg);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
          throw new ChainException(e);
        }
      }};
  }

  public static <X,Y,Z> NullaryFunc<X> makeNew(final Class<? extends X> clss, final Y arg1, final Z arg2) {
    return new NullaryFunc<X>() {
      @Override
      public X call() {
        try {
          Constructor<? extends X> ctor = clss.getConstructor(arg1.getClass(), arg2.getClass());
          return ctor.newInstance(arg1, arg2);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | SecurityException | IllegalArgumentException | InvocationTargetException e) {
          throw new ChainException(e);
        }
      }};
  }

  public static <X> Iterator<X> iterator(final Generator<? extends X> generator) {
    return new ConstIterator<X>() {
      boolean done = false;
      X next = getNext();

      private X getNext() {
        if (done) {
          return null;
        }
        try {
          return generator.generate();
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
      public X next() {
        X val = next;
        next = getNext();
        return val;
      }};
  }

  /** NB: all calls to iterator() will share the same generator */
  public static <X> Iterable<X> iterable(final Generator<? extends X> generator) {
    return new Iterable<X>() {
      @Override
      public Iterator<X> iterator() {
        return Functions.<X>iterator(generator);
      }};
  }
  
  public static <X> Iterable<X> iterable(final Iterator<X> iterator) {
    return new Iterable<X>() {
      @Override
      public Iterator<X> iterator() {
        return iterator;
      }
    };
  }

  public static Generator<String> foreach(final BufferedReader in) {
    return foreach(in, false);
  }
  public static Generator<String> foreach(final BufferedReader in, final boolean closeInFinalize) {

    return new Generator<String>() {

      protected void finalize() throws Throwable {
        if (closeInFinalize) {
          in.close();
        }
      };
      @Override
      public String generate() throws SourceExhausted {
        try {
          String line = in.readLine();
          if (line == null) {
            in.close();
            throw SOURCE_EXHAUSTED;
          }
          return line;
        } catch (IOException e) {
          try {
            in.close();
          } catch (IOException e1) {
            // ignore for now
          }
          throw SOURCE_EXHAUSTED;
        }
      }
    };

  }

  public static <X> Generator<X> empty() {
    return new Generator<X>() {
      @Override
      public X generate() throws SourceExhausted {
        throw SOURCE_EXHAUSTED;
      }};
  }

  public static Generator<String> foreach(File file, FileOption...options) {
    try {
      BufferedReader in = FileUtil.reader(file, options);
      return foreach(in, true);
    } catch (final IOException e) {
      return new Generator<String>() {
        @Override
        public String generate() throws SourceExhausted {
          throw new SourceExhausted(e);
        }
      };
    }
  }
  public static Generator<String> foreach(Path path, FileOption...options) {
    return foreach(path.toFile(), options);
  }

  public static <X> Process<X,X> keep(final Predicate<X> pred) {
    return new Process<X, X>() {
      @Override
      public List<X> process(X arg) {
        if (pred.test(arg)) {
          return Collections.singletonList(arg);
        } else {
          return Collections.emptyList();
        }
      }

      @Override
      public List<X> close() {
        return Collections.emptyList();
      }
    };
  }
  public static <X> Process<X,X> remove(final Predicate<X> pred) {
    return new Process<X, X>() {
      @Override
      public List<X> process(X arg) {
        if (!pred.test(arg)) {
          return Collections.singletonList(arg);
        } else {
          return Collections.emptyList();
        }
      }

      @Override
      public List<X> close() {
        return Collections.emptyList();
      }
    };
  }

  public static <X,Y> Process<X,Y> map(final UnaryFunc<X,Y> transformer) {
    return new Process<X, Y>() {
      @Override
      public List<Y> process(X arg) {
        return Collections.singletonList(transformer.call(arg));
      }

      @Override
      public List<Y> close() {
        return Collections.emptyList();
      }
    };
  }
  public static <X> Process<X,X> check(final Control<X> control) {
    return new Process<X, X>() {
      @Override
      public List<X> process(X arg) {
        switch (control.check(arg)) {
        case USE:
          return Collections.singletonList(arg);
        case SKIP:
          return Collections.emptyList();
        case STOP:
          return null;
        }
        throw new IllegalStateException();
      }

      @Override
      public List<X> close() {
        return Collections.emptyList();
      }
    };
  }

  public static Process<String,String[]> csv(FileOption...options) {
    if (options == null) {
      return new CSVProcess();
    }
    OptionSet oset = new OptionSet(options);
    CSVProcess.WS wsPolicy = oset.csvStripWS() ? CSVProcess.WS.STRIP : CSVProcess.WS.KEEP;
    return new CSVProcess(wsPolicy);
  }
  
  public static <X> UnaryFunc<X,X> identity() {
    return new UnaryFunc<X, X>() {
      @Override
      public X call(X val) {
        return val;
      }
      
    };
  }

  public static UnaryFunc<File, Iterable<String>> openFile(final FileOption...options) {
    return new UnaryFunc<File, Iterable<String>>() {
      @Override
      public Iterable<String> call(File val) {
        return Chain.from(val, options);
      }
    }; 
  }

  public static UnaryFunc<Path, Iterable<String>> openPath(final FileOption...options) {
    return new UnaryFunc<Path, Iterable<String>>() {
      @Override
      public Iterable<String> call(Path val) {
        return Chain.from(val, options);
      }
    }; 
  }

  public static UnaryFunc<String, File> namedFile() {
    return new UnaryFunc<String, File>() {
      @Override
      public File call(String val) {
        return new File(val);
      }
    };
  }

  public static <K,V> Predicate<Pair<K,V>> keyPred(final Predicate<? super K> pred) {
    return new Predicate<Pair<K,V>>() {
      @Override
      public boolean test(Pair<K, V> val) {
        return pred.test(val.key);
      }
    };
  }
  public static <K,V> Predicate<Pair<K,V>> valuePred(final Predicate<? super V> pred) {
    return new Predicate<Pair<K,V>>() {
      @Override
      public boolean test(Pair<K, V> val) {
        return pred.test(val.value);
      }
    };
  }

  public static <K,K2,V> UnaryFunc<Pair<K,V>, Pair<K2,V>> keyTransformer(final UnaryFunc<? super K, ? extends K2> transformer) {
    return new UnaryFunc<Pair<K,V>, Pair<K2,V>>() {
      @Override
      public Pair<K2, V> call(Pair<K, V> val) {
        return new Pair<K2, V>(transformer.call(val.key), val.value);
      }
    };

  }
  public static <K,V,V2> UnaryFunc<Pair<K,V>, Pair<K,V2>> valueTransformer(final UnaryFunc<? super V, ? extends V2> transformer) {
    return new UnaryFunc<Pair<K,V>, Pair<K,V2>>() {
      @Override
      public Pair<K, V2> call(Pair<K, V> val) {
        return new Pair<K, V2>(val.key, transformer.call(val.value));
      }
    };

  }
  public static <V> UnaryFunc<Pair<?,? extends V>,V> values() {
    return new UnaryFunc<Pair<?,? extends V>, V>() {
      @Override
      public V call(Pair<?, ? extends V> val) {
        return val.value;
      }
    };
  }
  public static <K> UnaryFunc<Pair<? extends K,?>,K> keys() {
    return new UnaryFunc<Pair<? extends K,?>, K>() {
      @Override
      public K call(Pair<? extends K, ?> val) {
        return val.key;
      }
    };
  }
  
  public static <T> UnaryFunc<Integer,T>indexInto(final T[] array) {
    return new UnaryFunc<Integer, T>() {
      @Override
      public T call(Integer index) {
        return array[index];
      }};
  }
  
  public static <T> UnaryFunc<Integer,T>indexInto(final List<? extends T> list) {
    return new UnaryFunc<Integer, T>() {
      @Override
      public T call(Integer index) {
        return list.get(index);
      }};
  }
  
  public static <K,V> UnaryFunc<K,V>indexInto(final Map<? extends K, ? extends V> map) {
    return new UnaryFunc<K,V>() {
      @Override
      public V call(K key) {
        return map.get(key);
      }};
  }
  
  public static <T> UnaryFunc<T[], T>atArrayIndex(final int index) {
    return new UnaryFunc<T[], T>() {
      @Override
      public T call(T[] array) {
        return array[index];
      }};
  }
  public static <V, L extends List<? extends V>> UnaryFunc<L, V>atListIndex(final int index) {
    return new UnaryFunc<L, V>() {
      @Override
      public V call(L list) {
        return list.get(index);
      }};
  }
  public static <K, T, M extends Map<? extends K, ? extends T>> UnaryFunc<M, T>atMapIndex(final K index) {
    return new UnaryFunc<M, T>() {
      @Override
      public T call(M map) {
        return map.get(index);
      }};
  }

    
  public static <C,T> UnaryFunc<C,T>field(final Field field) {
    return new UnaryFunc<C, T>() {
      @Override
      public T call(C val) {
        try {
          @SuppressWarnings("unchecked")
          final T fv = (T)field.get(val);
          return fv;
        } catch (IllegalArgumentException | IllegalAccessException e) {
          throw new ChainException(e);
        }
      }};
  }
  public static <C,T> UnaryFunc<C,T>field(Class<? super C> fromClass, String name, Class<? extends T> fieldClass) {
    try {
      final Field field = fromClass.getDeclaredField(name);
      field.setAccessible(true);
      return Functions.<C,T>field(field);
    } catch (NoSuchFieldException | SecurityException e) {
      throw new ChainException(e);
    }
  }
  
  public static <C,T> UnaryFunc<C,T>call(final Method method, final Object ...args) {
    return new UnaryFunc<C, T>() {
      @Override
      public T call(C val) {
        try {
          @SuppressWarnings("unchecked")
          T mv = (T)method.invoke(val, args);
          return mv;
        } catch (IllegalAccessException | IllegalArgumentException
                 | InvocationTargetException e) 
        {
          throw new ChainException(e);
        }
      }};
  }
  
  /** predicate test() returns true for items that are not .equal() with any prior item tested */
  public static <To> NullaryFunc<Predicate<To>> unique() {
    return new NullaryFunc<Predicate<To>>() {
      @Override
      public Predicate<To> call() {
        return new Predicate<To>() {
          final Set<To> seen = new HashSet<>();
          @Override
          public boolean test(To val) {
            return seen.add(val);
          }
        };
      }};
  }

  /** predicate test() returns true for items that are not == with the previous value.  NB: Doesn't keep a long history.  NB: Doesn't use equals() */
  public static <To> NullaryFunc<Predicate<To>> different() {
    return new NullaryFunc<Predicate<To>>() {
      @Override
      public Predicate<To> call() {
        return new Predicate<To>() {
          To last = null;
          @Override
          public boolean test(To val) {
            if (val == last) {
              return false;
            }
            last = val;
            return true;
          }
        };
      }};
  }

  public static <X, Y, Z> UnaryFunc<X, Y> compose(final UnaryFunc<? super X, Z> t1, final UnaryFunc<? super Z, ? extends Y> t2) {
    return new UnaryFunc<X, Y>() {
      @Override
      public Y call(X val) {
        return t2.call(t1.call(val));
      }};
  }
  
  public static <X, Y> Predicate<X> compose(final UnaryFunc<? super X, Y> t, final Predicate<? super Y> p) {
    return new Predicate<X>() {
      @Override
      public boolean test(X val) {
        return p.test(t.call(val));
      }};
  }
  
  public static <X,Y> Comparator<X> compose(final UnaryFunc<? super X, Y> t, final Comparator<? super Y> c) {
    return new Comparator<X>() {
      @Override
      public int compare(X o1, X o2) {
        return c.compare(t.call(o1), t.call(o2));
      }};
  }
  
  public static <X,Y> Generator<Y> compose(final Generator<X> g, final UnaryFunc<? super X, ? extends Y> t) {
    return new Generator<Y>() {
      @Override
      public Y generate() throws SourceExhausted {
        return t.call(g.generate());
      }};
  }
  
  public static <X,Y> Observer<X> compose(final UnaryFunc<? super X, Y> t, final Observer<? super Y> o) {
    return new Observer<X>() {
      @Override
      public boolean observe(X val) {
        return o.observe(t.call(val));
      }};
  }
  
  public static <X,Y> Control<X> compose(final UnaryFunc<? super X, Y> t, final Control<? super Y> c) {
    return new Control<X>() {
      @Override
      public Control.ControlVal check(X val) {
        return c.check(t.call(val));
      }};
  }
  
  public static <X,Y> Consumer<X> compose(final UnaryFunc<? super X, Y> t, final Consumer<? super Y> c) {
    return new Consumer<X>() {
      @Override
      public boolean see(X val) {
        return c.see(t.call(val));
      }};
  }
  
  public static <X,Y,Z> Process<X,Y> compose(final UnaryFunc<? super X, Z> t, final Process<? super Z, Y> p) {
    return new Process<X, Y>() {
      @Override
      public List<Y> process(X arg) {
        return p.process(t.call(arg));
      }

      @Override
      public List<Y> close() {
        return p.close();
      }};
  }
  public static <X,Y,Z> Reducer<X,Y> compose(final UnaryFunc<? super X,Z> t, final Reducer<? super Z, Y> r) {
    return new Reducer<X, Y>() {
      @Override
      public Y update(Y old, X current) {
        return r.update(old, t.call(current));
      }
    };
  }
  
  public static <X,Y> Process<X, Y> compose(final Predicate<? super X> p, final UnaryFunc<? super X, ? extends Y> t) {
    return new Process<X, Y>() {
      @Override
      public List<Y> process(X arg) {
        if (p.test(arg)) {
          return Collections.singletonList((Y)t.call(arg));
        } else {
          return Collections.emptyList();
        }
      }

      @Override
      public List<Y> close() {
        return null;
      }};
  }
  
  public static Reducer<String, String> join(final String sep) {
    return new Reducer<String, String>() {
      @Override
      public String update(String old, String current) {
        if (old == null || old.isEmpty()) {
          return current;
        } else {
          return old+sep+current;
        }
      }
    };
  }
  public static Reducer<String,String> joinLines() {
    return join("\n");
  }
  public static Reducer<String,String> commaSeparated() {
    return join(",");
  }
  
  public static UnaryFunc<String, String> bracket(final String open, final String close) {
    return new UnaryFunc<String, String>() {
      @Override
      public String call(String val) {
        return open+val+close;
      }
    };
  }
  
  public static <X> Predicate<X> not(final Predicate<? super X> pred) {
    return new Predicate<X>() {
      @Override
      public boolean test(X val) {
        return !pred.test(val);
      }
    };
  }
  
  public static <X> Predicate<X> and(final Predicate<? super X> p1, final Predicate<? super X> p2) {
    return new Predicate<X>() {
      @Override
      public boolean test(X val) {
        return p1.test(val) && p2.test(val);
      }
    };
  }
  public static <X> Predicate<X> or(final Predicate<? super X> p1, final Predicate<? super X> p2) {
    return new Predicate<X>() {
      @Override
      public boolean test(X val) {
        return p1.test(val) || p2.test(val);
      }
    };
  }
  public static <X> Predicate<X> xor(final Predicate<? super X> p1, final Predicate<? super X> p2) {
    return new Predicate<X>() {
      @Override
      public boolean test(X val) {
        return p1.test(val) ^ p2.test(val);
      }
    };
  }
  @SafeVarargs
  public static <X> Predicate<X> all(final Predicate<? super X>...predicates) {
    return new Predicate<X>() {
      @Override
      public boolean test(X val) {
        for (Predicate<? super X> pred : predicates) {
          if (!pred.test(val)) {
            return false;
          }
        }
        return true;
      }
    };
  }
  @SafeVarargs
  public static <X> Predicate<X> any(final Predicate<? super X>...predicates) {
    return new Predicate<X>() {
      @Override
      public boolean test(X val) {
        for (Predicate<? super X> pred : predicates) {
          if (pred.test(val)) {
            return true;
          }
        }
        return false;
      }
    };
  }
  @SafeVarargs
  public static <X> Predicate<X> atLeast(final int n, final Predicate<? super X>...predicates) {
    return new Predicate<X>() {
      @Override
      public boolean test(X val) {
        int count = 0;
        for (Predicate<? super X> pred : predicates) {
          if (pred.test(val)) {
            if (++count >= n) {
              return true;
            }
          }
        }
        return false;
      }
    };
  }
  @SafeVarargs
  public static <X> Predicate<X> atMost(final int n, final Predicate<? super X>...predicates) {
    return new Predicate<X>() {
      @Override
      public boolean test(X val) {
        int count = 0;
        for (Predicate<? super X> pred : predicates) {
          if (pred.test(val)) {
            if (++count > n) {
              return false;
            }
          }
        }
        return true;
      }
    };
  }
  @SafeVarargs
  public static <X> Predicate<X> countSatisfies(final Predicate<? super Integer> countPred, final Predicate<? super X>...predicates) {
    return new Predicate<X>() {
      @Override
      public boolean test(X val) {
        int count = 0;
        for (Predicate<? super X> pred : predicates) {
          if (pred.test(val)) {
            ++count;
          }
        }
        return countPred.test(count);
      }
    };
  }
  
  public static <X extends Comparable<? super X>> Predicate<X> isMax() {
    return new Predicate<X>() {
      X current = null;
      @Override
      public boolean test(X val) {
        if (current == null || current.compareTo(val) < 0) {
          current = val;
          return true;
        }
        return false;
      }
    };
  }
  public static <X extends Comparable<? super X>> Predicate<X> isMin() {
    return new Predicate<X>() {
      X current = null;
      @Override
      public boolean test(X val) {
        if (current == null || current.compareTo(val) > 0) {
          current = val;
          return true;
        }
        return false;
      }
    };
  }
  public static <X> Predicate<X> isMax(final Comparator<? super X> cptr) {
    return new Predicate<X>() {
      X current = null;
      @Override
      public boolean test(X val) {
        if (current == null || cptr.compare(current, val) < 0) {
          current = val;
          return true;
        }
        return false;
      }
    };
  }
  public static <X> Predicate<X> isMin(final Comparator<? super X> cptr) {
    return new Predicate<X>() {
      X current = null;
      @Override
      public boolean test(X val) {
        if (current == null || cptr.compare(current, val) > 0) {
          current = val;
          return true;
        }
        return false;
      }
    };
  }
  public static <X,Y> UnaryFunc<X, Y> toConst(final Y constant) {
    return new UnaryFunc<X, Y>() {
      @Override
      public Y call(X val) {
        return constant;
      }
    };
  }
  public static <X> Process<X, X> replicate(final int n) {
    return new Process<X, X>() {
      @Override
      public List<X> process(X arg) {
        ArrayList<X> list = new ArrayList<>(n);
        for (int i=0; i<n; i++) {
          list.add(arg);
        }
        return list;
      }

      @Override
      public List<X> close() {
        return null;
      }
    };
  }
  
  public static <X> Generator<X> generate(final int n, final X val) {
    return new Generator<X>() {
      int i = 0;
      @Override
      public X generate() throws SourceExhausted {
        if (i++ >= n) {
          throw new SourceExhausted();
        }
        return val;
      }
    };
  }
  public static <X> Generator<X> infinite(final X val) {
    return new Generator<X>() {
      @Override
      public X generate() {
        return val;
      }
    };
  }
  
  public static Predicate<Object> always() {
    return new Predicate<Object>() {
      @Override
      public boolean test(Object val) {
        return true;
      }
    };
  }
  
  
  public static Predicate<Object> never() {
    return new Predicate<Object>() {
      @Override
      public boolean test(Object val) {
        return false;
      }
    };
  }
  
  public static Predicate<Object> withProb(final double prob) {
    return new Predicate<Object>() {
      @Override
      public boolean test(Object val) {
        return ThreadLocalRandom.current().nextDouble() < prob;
      }
    };
  }
  
  public static <K,V> Pair<K,V> pair(K key, V value) {
    return new Pair<>(key, value);
  }
  
  public static <X,Y> Iterable<Y> mapped(final Iterable<? extends X> source, 
                                         final UnaryFunc<? super X, ? extends Y> transformer) {
    return mapped(source, transformer, always());
  }
  public static <X> Iterable<X> satisfying(final Iterable<? extends X> source, 
                                           final Predicate<? super X> predicate) {
    return mapped(source, Functions.<X>identity(), predicate);
  }
  /**
   * Note: don't call remove() after hasNext().  Will throw IllegalStateException()
   */
  public static <X,Y> Iterable<Y> mapped(final Iterable<? extends X> source, 
                                         final UnaryFunc<? super X, ? extends Y> transformer,
                                         final Predicate<? super Y> predicate) {
    return new Iterable<Y>() {
      @Override
      public Iterator<Y> iterator() {
        return new Iterator<Y>() {
          Iterator<? extends X> iter = source.iterator();
          boolean hasNextCalled = false;
          boolean hasNext;
          Y next;
          @Override
          public boolean hasNext() {
            if (hasNextCalled) {
              return hasNext;
            }
            hasNextCalled = true;
            while (iter.hasNext()) {
              next = transformer.call(iter.next());
              if (predicate.test(next)) {
                return hasNext = true;
              }
            }
            return hasNext = false;
          }

          @Override
          public Y next() {
            if (!hasNextCalled) {
              hasNext();
            }
            if (!hasNext) {
              throw new NoSuchElementException();
            }
            hasNextCalled = false;
            return next;
          }

          @Override
          public void remove() {
            if (hasNextCalled) {
              throw new IllegalStateException("Mapped iterators do not allow remove() after hasNext()");
            }
            iter.remove();
          }
        };
      }
    };
  }
  
  public static <X> Predicate<X> asPredicate(final UnaryFunc<X, Boolean> func) {
    return new Predicate<X>() {
      @Override
      public boolean test(X val) {
        return func.call(val);
      }};
  }
  public static <X> Predicate<X> asPredicate(final UnaryFuncToBoolean<X> func) {
    return new Predicate<X>() {
      @Override
      public boolean test(X val) {
        return func.primCall(val);
      }};
  }
  public static <X> UnaryFuncToBoolean<X> asFunction(final Predicate<X> pred) {
    return new UnaryFuncToBoolean<X>() {
      @Override
      public boolean primCall(X val) {
        return pred.test(val);
      }};
  }
  
  public static <X,Y> Relation<X, Y> asRelation(final BinaryFunc<X, Y, Boolean> func) {
    return new Relation<X, Y>() {
      @Override
      public boolean test(X a, Y b) {
        return func.call(a, b);
      }};
  }
  public static <X,Y> Relation<X, Y> asRelation(final BinaryFuncToBoolean<X, Y> func) {
    return new Relation<X, Y>() {
      @Override
      public boolean test(X a, Y b) {
        return func.call(a, b);
      }};
  }
  public static <X,Y> BinaryFuncToBoolean<X, Y> asFunction(final Relation<X,Y> rel) {
    return new BinaryFuncToBoolean<X, Y>() {
      @Override
      public boolean primCall(X a, Y b) {
        return rel.test(a, b);
      }};
  }
  
}