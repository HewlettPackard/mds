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

package com.hpl.erk.config.type;

import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hpl.erk.Portion;
import com.hpl.erk.Rational;
import com.hpl.erk.ReadableString;
import com.hpl.erk.TrueOnce;
import com.hpl.erk.config.PType;
import com.hpl.erk.config.ex.CantReadError;
import com.hpl.erk.config.ex.ReadError;
import com.hpl.erk.config.func.ArgSet;
import com.hpl.erk.config.func.Param;
import com.hpl.erk.util.NumUtils;

public class PortionType extends SimpleType<Portion> {
  
  protected PortionType() {
    super(Portion.class);
  }

  static final Pattern dec = Pattern.compile("0?\\.[0-9_]+(?<!_)|1.[0_]*(?<!_)(?![0-9])");
  static final Pattern pct = Pattern.compile("(100(?:\\.0*)|\\p{Digit}{1,2}(?:.\\p{Digit}*))%");
  static final Pattern fraction = Pattern.compile("([0-9][0-9_]*(?<!_))/([1-9][0-9_]*(?<!_))");
  static final Pattern exact = Pattern.compile("(?:exactly\\s+)?([0-9][0-9_]*(?<!_)(?!\\.))", Pattern.CASE_INSENSITIVE);
  
  
  @Override
  public Portion readVal(ReadableString input, String valTerminators) throws ReadError {
    int resetTo = input.getCursor();
    input.skipWS();
    Matcher m = input.consume(pct);
    if (m != null) {
      final String digitString = m.group(1).replaceAll("_", "");
      final double n = Double.parseDouble(digitString);
      return Portion.asPercent(n);
    }
    m = input.consume(fraction);
    if (m != null) {
      final String numString = m.group(1).replaceAll("_", "");
      final String denomString = m.group(2).replaceAll("_", "");
      final int num = Integer.parseInt(numString);
      final int denom = Integer.parseInt(denomString);
      final Rational r = new Rational(num, denom);
      return Portion.as(r);
    }
    m = input.consume(dec);
    if (m != null) {
      final String digitString = m.group().replaceAll("_", "");
      final double n = Double.parseDouble(digitString);
      return Portion.as(n);
    }
    m = input.consume(exact);
    if (m != null) {
      final String digitString = m.group(1).replaceAll("_", "");
      final int n = Integer.parseInt(digitString);
      return Portion.exactly(n);
    }
    throw new CantReadError(input, resetTo, this);
  }

  
  
  private static final TrueOnce needInit = new TrueOnce();
  public static void ensureConfigParams() {
    if (!needInit.check()) {
      return;
    }
    
    final PType<Portion> type = PType.of(Portion.class);
    
    type.constant("all", Portion.ALL)
    .setHelp("Use all");
    type.constant("none", Portion.NONE)
    .setHelp("Use none");
    type.constant("majority", Portion.MAJORITY)
    .setHelp("Use a majority");
    
    type.new CFunc("rest", Param.opt("weight", double.class, 1.0)) {
      @Override
      public Portion make(ArgSet args) throws MFailed {
        Double weight = args.get(double.class, "weight");
        double w = NumUtils.whenNull(weight, 1);
        return Portion.remainder(w);
      }
    };
    type.new CFunc("rest", Param.opt("remainder", double.class, 1.0)) {
      @Override
      public Portion make(ArgSet args) throws MFailed {
        Double weight = args.get(double.class, "weight");
        double w = NumUtils.whenNull(weight, 1);
        return Portion.remainder(w);
      }
    };
    type.new CFunc("exactly", Param.req("n", int.class)) {
      @Override
      public Portion make(ArgSet args) throws MFailed {
        Integer n = args.get(int.class, "n");
        if (n == null) {
          throw new MFailed(this, args, String.format("Null parameter n"));
        }
        return Portion.exactly(n);
      }
    };
    type.new CFunc("fraction", Param.req("num", int.class), Param.req("denom", int.class)) {
      @Override
      public Portion make(ArgSet args) throws MFailed {
        Integer num = args.get(int.class, "num");
        if (num == null) {
          throw new MFailed(this, args, String.format("Null parameter num"));
        }
        Integer denom= args.get(int.class, "denom");
        if (denom== null) {
          throw new MFailed(this, args, String.format("Null parameter denom"));
        }
        return Portion.as(new Rational(num, denom));
      }
    };
    type.new CFunc("pct", Param.req("p", double.class)) {
      @Override
      public Portion make(ArgSet args) throws MFailed {
        Double p = args.get(double.class, "p");
        if (p == null) {
          throw new MFailed(this, args, String.format("Null parameter p"));
        }
        return Portion.asPercent(p);
      }
    };
    type.new CFunc("portion", Param.req("p", double.class)) {
      @Override
      public Portion make(ArgSet args) throws MFailed {
        Double p = args.get(double.class, "p");
        if (p == null) {
          throw new MFailed(this, args, String.format("Null parameter p"));
        }
        return Portion.as(p);
      }
    };
  }


  @Override
  public String describe() {
    return "A portion";
  }
  
  
  public static void main(String[] args) {
    ensureConfigParams();
    PType<Portion> dt = PType.of(Portion.class);
    PrintStream out = System.out;
    dt.testRead(out, "1/4", Portion.as(new Rational(1, 4)));
    dt.testRead(out, "all", Portion.all());
    dt.testRead(out, "15%", Portion.asPercent(15));
    dt.testRead(out, ".5", Portion.as(0.5));
    dt.testRead(out, "0.5", Portion.as(0.5));
    dt.testRead(out, "exactly[5]", Portion.exactly(5));
    dt.testRead(out, "exactly 5", Portion.exactly(5));
    dt.testRead(out, "1.5", CantReadError.class);
  }
  
  

}