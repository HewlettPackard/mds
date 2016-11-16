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

package com.hpl.erk.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import com.hpl.erk.adt.SmallMap;
import com.hpl.erk.func.NullaryFunc;
import com.hpl.erk.func.Predicate;
import com.hpl.erk.func.UnaryFunc;

public class MapUtils {

  public static <K,V> V getOrCreate(Map<? super K, V> map, K key, NullaryFunc<? extends V> creator) {
    V val = map.get(key);
    if (val == null) {
      val = creator.call();
      map.put(key, val);
    }
    return val;
  }
  public static <K,V> V getOrCreate(Map<? super K, V> map, K key, UnaryFunc<? super K, ? extends V> creator) {
    V val = map.get(key);
    if (val == null) {
      val = creator.call(key);
      map.put(key, val);
    }
    return val;
  }

  public static <K,V,M extends Map<? super K, ? super V>> M putIn(M map, K key, V val, NullaryFunc<? extends M> creator) {
    if (map == null) {
      map = creator.call();
    }
    map.put(key, val);
    return map;
  }

  public static <K,V> V getFrom(Map<? super K, ? extends V> map, K key) {
    if (map == null) {
      return null;
    }
    return map.get(key);
  }

  public static <K> boolean containsKey(Map<? super K, ?> map, K key) {
    if (map == null) {
      return false;
    }
    return map.containsKey(key);
  }

  public static <K, V> V removeFrom(Map<? super K, V> map, K key) {
    if (map == null) {
      return null;
    }
    return map.remove(key);
  }

  public static int sizeOf(Map<?,?> map) {
    return map == null ? 0 : map.size();
  }

  public static boolean isEmpty(Map<?,?> map) {
    return map == null || map.isEmpty();
  }

  public static <K,V> NullaryFunc<HashMap<K, V>> hashMapCreator() {
    return new NullaryFunc<HashMap<K,V>>() {
      @Override
      public HashMap<K, V> call() {
        return new HashMap<>();
      }};
  }

  public static <K,V> NullaryFunc<HashMap<K, V>> hashMapCreator(final int capacity) {
    return new NullaryFunc<HashMap<K,V>>() {
      @Override
      public HashMap<K, V> call() {
        return new HashMap<>(capacity);
      }};
  }

  public static <K,V> NullaryFunc<IdentityHashMap<K, V>> identityHashMapCreator() {
    return new NullaryFunc<IdentityHashMap<K,V>>() {
      @Override
      public IdentityHashMap<K, V> call() {
        return new IdentityHashMap<>();
      }};
  }

  public static <K,V> NullaryFunc<IdentityHashMap<K, V>> identityHashMapCreator(final int capacity) {
    return new NullaryFunc<IdentityHashMap<K,V>>() {
      @Override
      public IdentityHashMap<K, V> call() {
        return new IdentityHashMap<>(capacity);
      }};
  }
  
  public static <K,V> NullaryFunc<SmallMap<K, V>> smallHashMapCreator() {
    return new NullaryFunc<SmallMap<K,V>>() {
      @Override
      public SmallMap<K, V> call() {
        return new SmallMap<>();
      }};
  }

  public static <K,V> NullaryFunc<SmallMap<K, V>> smallHashMapCreator(final int maxCompact) {
    return new NullaryFunc<SmallMap<K,V>>() {
      @Override
      public SmallMap<K, V> call() {
        return new SmallMap<>(maxCompact);
      }};
  }

  

  public static <K,V> Map<K,V> maybeNullMap(Map<K, V> map) {
    if (map == null) {
      return Collections.emptyMap();
    }
    return map;
  }

  public static Predicate<Map<?, ?>> isEmptyMap() {
    return new Predicate<Map<?,?>>() {
      @Override
      public boolean test(Map<?,?> val) {
        return isEmpty(val);
      }
    };
  }

  public static Predicate<Map<?,?>> notEmptyMap() {
    return new Predicate<Map<?,?>>() {
      @Override
      public boolean test(Map<?,?> val) {
        return !isEmpty(val);
      }
    };
  }

  public static UnaryFunc<Map<?,?>, Integer> mapSize() {
    return new UnaryFunc<Map<?,?>, Integer>() {
      @Override
      public Integer call(Map<?,?> val) {
        return sizeOf(val);
      }
    };
  }

}