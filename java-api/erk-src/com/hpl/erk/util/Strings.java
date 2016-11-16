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

import java.util.Comparator;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.hpl.erk.LineWrapper;
import com.hpl.erk.SelfWrapping;
import com.hpl.erk.formatters.SeqFormatter;
import com.hpl.erk.func.NullaryFunc;
import com.hpl.erk.func.UnaryFuncToInt;
import com.hpl.erk.func.Predicate;
import com.hpl.erk.func.UnaryFunc;
import com.hpl.erk.text.English;



/**
 * Some utility functions for dealing with strings.
 * @author Evan Kirshenbaum
 *
 */
public class Strings {
  /**
   * There's no point in creating one of these.
   */
  private Strings() {}
  
  /**
   * The size of the blank string cache.
   */
  private static final int N_CACHED_SPACES_STRINGS = 20;

  /**
   * An array containing previously-referred-to short strings of spaces.
   */
  private static final String[] spacesCache = new String[N_CACHED_SPACES_STRINGS];
  
  /**
   * Return a string containing the specified number of spaces. 
   * For lengths less than {@link #N_CACHED_SPACES_STRINGS} ({@value #N_CACHED_SPACES_STRINGS}),
   * the result is cached, so it is cheap to call this multiple times.
   * 
   * @param n the number of spaces
   * @return A string containing <code>n</code> spaces 
   */
  public static String spaces(int n) {
    if (n < spacesCache.length) {
      String s = spacesCache[n];
      if (s == null) {
        s = rep(" ", n);
        spacesCache[n] = s;
      }
      return s;
    }
    return rep(" ", n);
  }
  
  /**
   * Return a string containing <code>n</code> repetitions of the string <code>s</code>.
   * @param s the string to repeat
   * @param n the number of repetitions.  Must be at least zero.
   * @return the constructed string.
   */
  public static String rep(final String s, final int n) {
    if (n < 0) { 
      throw new IllegalArgumentException();
    }
    final StringBuilder b = new StringBuilder(n*s.length());
    for (int i=0; i<n; i++) {
      b.append(s);
    }
    return b.toString();
  }

  /**
   * Like calling <code>s.split("\n")</code>, but without having to compile the Pattern.
   * @param s the string to split
   * @return an array of strings, not including the separators
   */
  public static String[] intoLines(String s) {
    return Patterns.NL_PATTERN.split(s);
  }
  
  /**
   * Like calling <code>s.split("\t")</code>, but without having to compile the Pattern.
   * @param s the string to split
   * @return an array of strings, not including the separators
   */
  public static String[] splitOnTabs(String s) {
    return Patterns.TAB_PATTERN.split(s);
  }
  
  /**
   * Like calling <code>s.split("\\p{Space}")</code>, but without having to compile the Pattern.
   * @param s the string to split
   * @return an array of strings, not including the separators
   */
  public static String[] splitOnWS(String s) {
    return Patterns.WS_PATTERN.split(s);
  }
  
  /**
   * Remove initial whitespace from the given string.  Note that unlike with {@link String#trim()}, this pays
   * attention to non-ASCII whitespace.  If there is no initial whitespace, the original string is returned.
   * @param s the string to trim
   * @return the trimmed string.
   */
  public static String trimLeft(String s) {
    Matcher m = Patterns.INITIAL_SPACE_PATTERN.matcher(s);
    if (m.find()) {
      return s.substring(m.end());
    }
    return s;
  }
  
  /**
   * Remove trailing whitespace from the given string.  Note that unlike with {@link String#trim()}, this pays
   * attention to non-ASCII whitespace.  If there is no trailing whitespace, the original string is returned.
   * @param s the string to trim
   * @return the trimmed string.
   */
  public static String trimRight(String s) {
    Matcher m = Patterns.TRAILING_SPACE_PATTERN.matcher(s);
    if (m.find()) {
      return s.substring(0, m.start());
    }
    return s;
  }
  
  /**
   * Remove initial and trailing whitespace from the given string.  Note that unlike with {@link String#trim()}, this pays
   * attention to non-ASCII whitespace.  If there is no initial or trailing whitespace, the original string is returned.
   * @param s the string to trim
   * @return the trimmed string.
   */
  public static String trim(String s) {
    return trimLeft(trimRight(s));
  }
  
  /**
   * Return a string equal to the given string with all lines prefixed by the given prefix.  If <code>prefixFirst</code> is
   * <code>false</code>, the first line is not prefixed.
   * @param s the string to process
   * @param prefix the prefix to add to each line
   * @param prefixFirst whether to add the prefix to the first line
   * @return the resulting string
   */
  public static String prefixLines(String s, String prefix, boolean prefixFirst) {
    String[] lines = intoLines(s);
    String indented = SeqFormatter.lines(prefix).addAll(lines).toString();
    return prefixFirst ? prefix + indented : indented;
  }

  /**
   * Return a string equal to the given string with all lines prefixed by the given prefix.  
   * @param s the string to process
   * @param prefix the prefix to add to each line
   * @return the resulting string
   */
  public static String prefixLines(String s, String prefix) {
    return prefixLines(s, prefix, true);
  }
  
  /**
   * Return a string equal to the given string with all lines indented by prefixing the given number of spaces.
   * If <code>indentFirst</code> is
   * <code>false</code>, the first line is not indented.
   * @param s the string to process
   * @param n the number of spaces to indent
   * @param indentFirst whether to indent the first line
   * @return the resulting string
   */
  public static String indentLines(String s, int n, boolean indentFirst) {
    return prefixLines(s, spaces(n), indentFirst);
  }
  
  /**
   * Return a string equal to the given string with all lines indented by prefixing the given number of spaces.
   * @param s the string to process
   * @param n the number of spaces to indent
   * @return the resulting string
   */
  public static String indentLines(String s, int n) {
    return indentLines(s, n, true);
  }
  
  /**
   * Wrap the object (as a string) with a given line width.  If the object is a {@link SelfWrapping}, calls
   * {@link SelfWrapping#toWrappedString(int)}.  Otherwise, calls {@link LineWrapper#wrapLines(String, int)}.
   * @param o
   * @param width
   * @return
   */
  public static String wrap(Object o, int width) {
    if (o instanceof SelfWrapping) {
      return ((SelfWrapping)o).toWrappedString(width);
    }
    return LineWrapper.wrapLines(String.valueOf(o), width);
  }
  
  public static String padLeft(String s, int width, String pad) {
    int padLen = width-s.length();
    if (padLen <= 0) {
      return s;
    }
    return repToWidth(padLen, pad)+s;
  }

  public static String padRight(String s, int width, String pad) {
    int padLen = width-s.length();
    if (padLen <= 0) {
      return s;
    }
    return s+repToWidth(padLen, pad);
  }
  
  public static String center(String s, int width, String pad) {
    int padLen = width-s.length();
    if (padLen <= 0) {
      return s;
    }
    int leftWidth = padLen >> 1;
    int rightWidth = padLen-leftWidth;
    return repToWidth(leftWidth, pad)+s+repToWidth(rightWidth, pad);
  }
  
  

  public static String repToWidth(int padLen, String pad) {
    if (padLen <= 0) {
      return "";
    }
    if (pad.equals(" ")) {
      return spaces(padLen);
    }
    int reps = padLen/pad.length();
    int extra = padLen%pad.length();
    if (extra == 0) {
      return rep(pad, reps);
    }
    return rep(pad, reps)+pad.substring(0, extra);
  }
  
  public static Comparator<String> caseSensitive() {
    return new Comparator<String>() {
      @Override
      public int compare(String o1, String o2) {
        return o1.compareTo(o2);
      }
    };
  }
  
  public static Comparator<String> caseInsensitive() {
    return new Comparator<String>() {
      @Override
      public int compare(String o1, String o2) {
        return o1.compareToIgnoreCase(o2);
      }
    };
  }
  
  public static String flag(boolean guard, String trueFlag, String falseFlag) {
    return guard ? trueFlag : falseFlag;
  }
  
  public static String flag(boolean guard, NullaryFunc<String> trueCreator, NullaryFunc<String> falseCreator) {
    return guard ? trueCreator.call() : falseCreator.call();
  }
  public static String flagWhen(boolean guard, String flag) {
    return flag(guard, flag, "");
  }
  
  public static String flagWhen(boolean guard, NullaryFunc<String> creator) {
    return guard ? creator.call() : "";
  }
  
  public static String flagUnless(boolean guard, String flag) {
    return flagWhen(!guard, flag);
  }
  
  public static String flagUnless(boolean guard, NullaryFunc<String> creator) {
    return flagWhen(!guard, creator);
  }
  public static String notNull(String s) {
    return s == null ? "" : s;
  }
  
  public static NullaryFunc<String> delayedFormat(final String fmt, Object...args) {
    final Object[] argsToUse = forceCreation(args);
    return new NullaryFunc<String>() {
      @Override
      public String call() {
        return String.format(fmt, argsToUse);
      }
    };
  }
  
  private static Object[] forceCreation(Object[] args) {
    if (!hasCreators(args)) {
      return args;
    }
    final int n = args.length;
    Object[] array = new Object[n];
    for (int i=0; i<n; i++) {
      Object arg = args[i];
      array[i] = (arg instanceof NullaryFunc) ? ((NullaryFunc<?>)arg).call() : arg;
    }
    return array;
  }

  private static boolean hasCreators(Object[] args) {
    for (Object arg : args) {
      if (arg instanceof NullaryFunc) {
        return true;
      }
    }
    return false;
  }

  public static String ucFirst(String word) {
    return word.substring(0,1).toUpperCase()+word.substring(1);
  }
  
  public static UnaryFunc<String, String> toLowerCase() {
    return new UnaryFunc<String, String>() {
      @Override
      public String call(String val) {
        return val.toLowerCase();
      }
    };
  }
  public static UnaryFunc<String, String> toUpperCase() {
    return new UnaryFunc<String, String>() {
      @Override
      public String call(String val) {
        return val.toUpperCase();
      }
    };
  }
  
  public static String chopFromStart(int len, String s) {
    return s.substring(len);
  }
  public static String chopFromEnd(int len, String s) {
    return s.substring(0, s.length()-len);
  }
  
  public static String noun(long n, String plural, String singular) {
    return English.Noun.find(plural, singular).format(n);
  }
  public static String noun(long n, String plural) {
    return English.Noun.find(plural).format(n);
  }
  public static String verb(long n, String plural, String singular) {
    return English.Verb.find(plural, singular).format(n);
  }
  public static String verb(long n, String plural) {
    return English.Verb.find(plural).format(n);
  }
  public static String verb(long n, boolean positive, String plural, String singular) {
    return English.Modal.bind(English.Verb.find(plural, singular), positive).format(n);
  }
  public static String verb(long n, boolean positive, String plural) {
    return English.Modal.bind(English.Verb.find(plural), positive).format(n);
  }
  
  public static String s(long n) {
    return flagWhen(n==1, "s");
  }
  public static String es(long n) {
    return flagWhen(n==1, "es");
  }
  public static String ies(long n) {
    return n==1 ? "y" : "ies";
  }
  public static String men(long n) {
    return n==1 ? "man" : "men";
  }
  public static String not(boolean b) {
    return flagWhen(b, " not");
  }
  public static String nt(boolean b) {
    return flagWhen(b, "n't");
  }
  
  public static UnaryFunc<Object, String> formatUsing(final String fmt) {
    return new UnaryFunc<Object, String>() {
      @Override
      public String call(Object val) {
        return String.format(fmt, val);
      }
    };
  }
  
  public static int lengthOf(String s) {
    return s == null ? 0 : s.length();
  }

  public static UnaryFunc<String,String[]> split(final Pattern splitter) {
    return new UnaryFunc<String, String[]>() {
      @Override
      public String[] call(String val) {
        return splitter.split(val);
      }
    };
  }

  public static UnaryFunc<String,String[]> split(String regExp) {
    return split(Pattern.compile(regExp));
  }

  public static UnaryFunc<String, String[]> tsv() {
    return split("\t");
  }

  public static Predicate<String> startsWith(final String prefix) {
    return new Predicate<String>() {
      @Override
      public boolean test(String val) {
        return val.startsWith(prefix);
      }
    };
  }

  public static Predicate<String> endsWith(final String suffix) {
    return new Predicate<String>() {
      @Override
      public boolean test(String val) {
        return val.endsWith(suffix);
      }
    };
  }

  public static Predicate<String> isEmpty() {
    return new Predicate<String>() {
      @Override
      public boolean test(String val) {
        return val == null || val.isEmpty();
      }
    };
  }
  public static Predicate<String> notEmpty() {
    return new Predicate<String>() {
      @Override
      public boolean test(String val) {
        return val != null && !val.isEmpty();
      }
    };
  }
  
  public static UnaryFuncToInt<String> length() {
    return new UnaryFuncToInt<String>() {
      @Override
      public int primCall(String val) {
        return lengthOf(val);
      }
    };
  }
  
  public static Iterable<Character> chars(final CharSequence s) {
    return new Iterable<Character>() {
      @Override
      public Iterator<Character> iterator() {
        return new Iterator<Character>() {
          int index = 0;
          @Override
          public void remove() {
            throw new UnsupportedOperationException("remove");
          }
          
          @Override
          public Character next() {
            return s.charAt(index++);
          }
          
          @Override
          public boolean hasNext() {
            return index < s.length();
          }
        };
      }};
  }
}