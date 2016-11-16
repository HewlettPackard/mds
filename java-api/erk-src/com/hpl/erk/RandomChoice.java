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

package com.hpl.erk;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ThreadLocalRandom;

public class RandomChoice<T> {
  private final HashMap<T, Weight> choices = new HashMap<T, Weight>();
  private double totalWeight = 0;
  private T onlyChoice = null;
  private int size = 0;
  
  private static class Weight {
    double weight;

    public Weight(double weight) {
      this.weight = weight;
    }
    
    @Override
    public String toString() {
      return Double.toString(weight);
    }
    
  }
  
  public boolean add(T choice, double weight) {
    if (weight == 0) {
      return remove(choice);
    }
    if (weight < 0) {
      throw new IllegalArgumentException(String.format("Choice weight must be >= 0, was %f", weight));
    }
    Weight w = choices.get(choice);
    boolean added;
    if (w == null) {
      choices.put(choice, new Weight(weight));
      totalWeight += weight;
      added = true;
      size++;
    } else {
      totalWeight += w.weight-weight;
      w.weight = weight;
      added = false;
    }
    if (size == 1) {
      onlyChoice = choice;
    } else {
      onlyChoice = null;
    }
    return added;
  }
  
  public boolean remove(T choice) {
    Weight w = choices.remove(choice);
    if (w == null) {
      return false;
    }
    totalWeight -= w.weight;
    size--;
    if (onlyChoice == choice) {
      onlyChoice = null;
    } else if (size == 1) {
      onlyChoice = choices.keySet().iterator().next();
    }
    return true;
  }

  public boolean add(T choice) {
    return add(choice, 1);
  }
  
  public <U extends T, N extends Number> void addAll(Map<U, N> map) {
    for (Entry<U,N> e : map.entrySet()) {
      add(e.getKey(), e.getValue().doubleValue());
    }
  }
  
  public <U extends T> void addAll(Collection<U> coll) {
    for (U e : coll) {
      add(e);
    }
  }
  public <U extends T> void addAll(U[] array) {
    for (U e : array) {
      add(e);
    }
  }
  
  public T choose() {
    if (size == 1) {
      return onlyChoice;
    }
    if (size == 0) {
      return null;
    }
    double r = ThreadLocalRandom.current().nextDouble()*totalWeight;
    for (Entry<T,Weight> e : choices.entrySet()) {
      double w = e.getValue().weight;
      if (r < w) {
        return e.getKey();
      }
      r -= w;
    }
    throw new IllegalStateException("Ran off the end of choice set");
  }
  
  @SafeVarargs
  public static <T> RandomChoice<T> over(T...vals) {
    RandomChoice<T> choice = new RandomChoice<>();
    choice.addAll(vals);
    return choice;
  }


}