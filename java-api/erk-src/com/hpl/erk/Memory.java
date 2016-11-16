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

public class Memory {
  public static final int DEFAULT_PRECISION = 2;
  public static final int K = 1024;
  public static final int M = 1024*K;
  public static final int G = 1024*M;
  public static final long T = 1024L*G;
  private static final double dK = K;
  private static final double dM = M;
  private static final double dG = G;
  private static final double dT = T;
  
  public static class Snapshot {
    final long size;

    Snapshot(long size) {
      this.size = size;
    }
    
    public Snapshot deltaFrom(Snapshot other) {
      return new Snapshot(size-other.size);
    }
    
    public double inT() {
      return Memory.inT(size);
    }
    public double inG() {
      return Memory.inG(size);
    }
    public double inM() {
      return Memory.inM(size);
    }
    public double inK() {
      return Memory.inK(size);
    }
    public long inBytes() {
      return size;
    }
    
    public String toString() {
      return readable(size, DEFAULT_PRECISION, "B");
    }
  }

  public static Snapshot memoryUsed() {
    return memoryUsed(true);
  }
  public static Snapshot memoryUsed(boolean cleanup) {
    Runtime rt = runtime(cleanup);
    return new Snapshot(memoryUsed(rt));
  }
  public static Snapshot totalMemory() {
    return totalMemory(true);
  }
  public static Snapshot totalMemory(boolean cleanup) {
    Runtime rt = runtime(cleanup);
    return new Snapshot(totalMemory(rt));
  }
  public static Snapshot freeMemory() {
    return freeMemory(true);
  }
  public static Snapshot freeMemory(boolean cleanup) {
    Runtime rt = runtime(cleanup);
    return new Snapshot(freeMemory(rt));
  }
  public static Snapshot maxMemory() {
    return maxMemory(true);
  }
  public static Snapshot maxMemory(boolean cleanup) {
    Runtime rt = runtime(cleanup);
    return new Snapshot(maxMemory(rt));
  }
  
  public static long[] stats() {
    return stats(true);
  }
  public static long[] stats(boolean cleanup) {
    Runtime rt = runtime(cleanup);
    return new long[] {totalMemory(rt), freeMemory(rt), maxMemory(rt)};
  }

  private static long memoryUsed(Runtime rt) {
    long mem = rt.totalMemory()-rt.freeMemory();
    return mem;
  }

  private static long totalMemory(Runtime rt) {
    long mem = rt.totalMemory();
    return mem;
  }

  private static long maxMemory(Runtime rt) {
    long mem = rt.maxMemory();
    return mem;
  }

  private static long freeMemory(Runtime rt) {
    long mem = rt.freeMemory();
    return mem;
  }

  private static Runtime runtime(boolean cleanup) {
    Runtime rt = Runtime.getRuntime();
    if (cleanup) {
      rt.runFinalization();
      rt.gc();
    }
    return rt;
  }
  
  public static String formattedMemoryUsed(int decimalPlaces) {
    Snapshot mem = memoryUsed();
    String val = readable(mem.inBytes(), decimalPlaces, "B");
    return val;
  }
  public static String formattedMemoryUsed() {
    return formattedMemoryUsed(DEFAULT_PRECISION);
  }
  
  public static double inK(long size) {
    return size/dK;
  }
  public static double inM(long size) {
    return size/dM;
  }
  public static double inG(long size) {
    return size/dG;
  }
  public static double inT(long size) {
    return size/dT;
  }
  public static String readable(long size, int decimalPlaces, String unit) {
    if (size < 0) {
      return "-"+readable(-size, decimalPlaces, unit);
    }
    double val;
    String prefix;
    if (size >= T) {
      val = inT(size);
      prefix = "T";
    } else if (size >= G) {
      val = inG(size);
      prefix = "G";
    } else if (size >= M) {
      val = inM(size);
      prefix = "M";
    } else if  (size >= K) {
      val = inK(size);
      prefix = "k";
    } else {
      val = size;
      prefix = "";
    }
    String fmt = String.format("%%1.%df %%s%%s", decimalPlaces);
    String s = String.format(fmt, val, prefix, unit);
    return s;
  }

  public static String describe() {
    long[] stats = Memory.stats();
    long total = stats[0];
    long free = stats[1];
    long max = stats[2];
    long used = total-free;
    double pct = (100.0*used)/total;
    double mpct = (100.0*used)/max;
    return String.format("Used: %s of %s (%1.2f%%); %s free; used %1.2f%% of %s max; %s free",
        Memory.readable(used, 1, "B"),
        Memory.readable(total, 1, "B"),
        pct,
        Memory.readable(free, 1, "B"),
        mpct,
        Memory.readable(max, 1, "B"),
        Memory.readable(max-used, 1, "B"));
  }

}