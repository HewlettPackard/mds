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

package com.hpl.erk.config.func;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hpl.erk.LineWrapper;
import com.hpl.erk.adt.IdentityHashSet;
import com.hpl.erk.config.PType;
import com.hpl.erk.config.func.Param.Kind;
import com.hpl.erk.config.type.SimpleType;
import com.hpl.erk.formatters.SeqFormatter;
import com.hpl.erk.formatters.SeqFormatter.Option;
import com.hpl.erk.func.Pair;
import com.hpl.erk.util.CollUtils;
import com.hpl.erk.util.MapUtils;

public class ConfigFunc {
  private final String name;
  private List<Param> positional;
  private Map<String, Param> keyword;
  private String help = null;
  
  public enum Registration {
    REGISTERED, UNREGISTERED
  }
  
  public static class MFailed extends Exception {

    private static final long serialVersionUID = 6176445608713647675L;
    
    public final ConfigFunc func;
    public final ArgSet args;

    public MFailed(ConfigFunc func, ArgSet args) {
      super(String.format("Error processing %s", func));
      this.func = func;
      this.args = args;
    }

    public MFailed(ConfigFunc func, ArgSet args, String message, Throwable cause) {
      super(String.format("Error processing %s: %s", func, message), cause);
      this.func = func;
      this.args = args;
    }

    public MFailed(ConfigFunc func, ArgSet args, String message) {
      super(String.format("Error processing %s: %s", func, message));
      this.func = func;
      this.args = args;
    }

    public MFailed(ConfigFunc func, ArgSet args, Throwable cause) {
      super(String.format("Error processing %s", func), cause);
      this.func = func;
      this.args = args;
    }
    
  }

  
  public ConfigFunc(String name, Param...params) {
    this.name = name;
    Kind lastKind = Kind.REQUIRED;
    for (Param p : params) {
      if (p.kind == Kind.KEYWORD) {
        if (keyword == null) {
          keyword = new HashMap<>(1);
        }
        Param old = keyword.put(normalize(p.name), p);
        if (old != null) {
          throw new IllegalArgumentException(String.format("function %s has two keyword parameters that normalize to '%s': '%s' and '%s'",
                                                           name, normalize(p.name), old, p));
        }
        continue;
      }
      if (p.kind.compareTo(lastKind) < 0) {
        throw new IllegalArgumentException(String.format("%s param cannot come before %s param", lastKind, p.kind));
      }
      if (p.kind == Kind.REST && lastKind == Kind.REST) {
        throw new IllegalArgumentException(String.format("Only one %s parameter allowed", p.kind));
      }
      if (positional == null) {
        positional = new ArrayList<>(1);
      }
      positional.add(p);
      lastKind = p.kind;
    }
  }
  
  public String getName() {
    return name;
  }
  
  public Param[] params() {
    int n = positional().size()+byKwd().size();
    Param[] array = (Param[]) Array.newInstance(Param.class, n);
    ArrayList<Param> list = new ArrayList<>(positional());
    list.addAll(byKwd().values());
    return list.toArray(array);
  }
  
  public List<Param> positional() {
    return CollUtils.maybeNullList(positional);
  }
  public Map<String,Param> byKwd() {
    return MapUtils.maybeNullMap(keyword);
  }

  @SuppressWarnings("serial")
  private static class CFuncCache<T> extends ArrayList<ConcreteFunc<? extends T>> {
  }
  private static Map<Pair<String, PType<?>>, CFuncCache<?>> cfuncCache = new HashMap<>();
  public static <T> Collection<ConcreteFunc<? extends T>> lookup(String name, PType<? extends T> type) {
    String normalizedName = normalize(name);
    Pair<String,PType<?>> key = new Pair<String,PType<?>>(normalizedName, type);
    CFuncCache<T> coll = (CFuncCache<T>)cfuncCache.get(key);
    if (coll == null) {
      Set<ConcreteFunc<?>> cfuncs = ConcreteFunc.lookupCFuncs(normalizedName);
      coll = new CFuncCache<T>();
      for (ConcreteFunc<?> f : cfuncs) {
        ConcreteFunc<? extends T> converted = f.convertedTo(type);
        if (converted != null) {
          coll.add(converted);
        }
      }
      cfuncCache.put(key, coll);
    }
    // TODO Add generics
    return coll;
  }
  
  public static <T> Collection<ConcreteFunc<? extends T>> lookup(String name, PType<T> type, String under) {
    Set<SimpleType<?>> types = SimpleType.lookup(under);
    Collection<ConcreteFunc<? extends T>> coll = new IdentityHashSet<>();
    for (SimpleType<?> t : types) {
      Collection<ConcreteFunc<?>> funcs = lookup(name, t);
      for (ConcreteFunc<?> f : funcs) {
        ConcreteFunc<? extends T> converted = f.convertedTo(type);
        if (converted != null) {
          coll.add(converted);
        }
      }
    }
    // TODO add generics
    return coll;
  }
  
  @Override
  public String toString() {
    final int psize = positional().size();
    final int ksize = byKwd().size();
    final int arity = psize+ksize;
    if (arity == 0) {
      return name;
    }
    SeqFormatter<String> sf = SeqFormatter.<String>list(Option.empty(""));
    for (Param p : positional) {
      sf.add(p.inArgList());
    }
    String kwdIntro = "BY NAME: ";
    for (Param p : byKwd().values()) {
      sf.add(kwdIntro+p.inArgList());
      kwdIntro = "";
    }
    return name+sf.toString();
  }
  
  public ConfigFunc setHelp(String help) {
    this.help = help;
    return this;
  }
  
  public String help(int lineWidth) {
    StringBuilder b = null;
    if (help != null) {
      b = new StringBuilder(LineWrapper.wrapLines(help, lineWidth, "  "));
      b.append('\n');
    }
    for (Param p : positional()) {
      b = addParamHelp(p, b, lineWidth);
    }
    for (Param p : byKwd().values()) {
      b = addParamHelp(p, b, lineWidth);
    }
    if (b == null) {
      return null;
    }
    return b.toString();
  }

  private static StringBuilder addParamHelp(Param p, StringBuilder b, int lineWidth) {
    String h = p.help();
    if (h != null) {
      if (b == null) {
        b = new StringBuilder();
      }
      LineWrapper w = new LineWrapper(lineWidth).indent(8).firstPrefix("  "+p.name+": ");
      b.append(w.wrap(h));
      b.append('\n');
    }
    return b;
  }
  
  private static final Pattern sepRE = Pattern.compile("[_.]");
  public static String normalize(String s) {
    Matcher m = sepRE.matcher(s.toLowerCase());
    return m.replaceAll("");
  }


}