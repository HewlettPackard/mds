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

package com.hpl.erk.mash;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipInputStream;

public class Compile {
	
	static int nadded = 0;

  /** Compile a matcher. <pre>
   * Usage:  $0  outfile.matcher  40  filelist
   * where 40 is the number of bits to preserve
   * where filelist can contain directories to recurse,
   * *.zip files to scan contents of,
   * .atitles files to parse, or
   * .titles.gz files to read all strings from (each whole line is a title)
   */
  public static void main(String[] args) throws IOException {
    File output = new File(args[0]);
//    OpenHashTitleMatcher.TRACE = true;
    int bytes = Integer.parseInt(args[1]);
    if (bytes > 8) {
      bytes = (int)Math.ceil(bytes/8.0);
    }
    OpenHashTitleMatcher matcher = new OpenHashTitleMatcher(1<<21, 1);
    for (int i=2; i<args.length; i++) {
      addTo(matcher, new File(args[i]));
    }
    System.err.printf("%,d titles compiled in.\n", nadded);
    UniformTableTitleMatcher utm = matcher.compact(bytes);
    utm.dumpTo(output);
    System.err.printf("%,d bytes in compiled file.\n", output.length());
  }

  private static void addTo(OpenHashTitleMatcher matcher, File zis2) throws IOException {
    if (zis2.isDirectory()) {
      for (File f : zis2.listFiles(new FilenameFilter() {
        public boolean accept(File dir, String name) {
          return !name.startsWith(".");
        }}))
      {
        addTo(matcher, f);
      }
      return;
    }
    if (zis2.getName().toLowerCase().endsWith(".zip")) {
      ZipInputStream zis = new ZipInputStream(new BufferedInputStream(new FileInputStream(zis2)));
      while (zis.getNextEntry() != null) {
        addTo(matcher, zis);
        zis.closeEntry();
      }
      zis.close();
      return;
    }
    if (zis2.getName().toLowerCase().endsWith(".titles.gz")) {//GF: each whole line is a title
    	GZIPInputStream gis = new GZIPInputStream(new BufferedInputStream(new FileInputStream(zis2)));
    	BufferedReader in = new BufferedReader(new InputStreamReader(gis));
    	for (String line; (line = in.readLine()) != null; nadded++) {
    		matcher.addTarget(line, 0);
    	}
    	in.close();
    	return;
    }
    InputStream in = new BufferedInputStream(new FileInputStream(zis2));
    addTo(matcher, in);
  }

  private static final Pattern inputPattern = Pattern.compile("^\\S+\\s+(.*)");
  private static void addTo(OpenHashTitleMatcher matcher, InputStream in) throws IOException {
    BufferedReader rdr = new BufferedReader(new InputStreamReader(in));
    String line;
    while ((line = rdr.readLine()) != null) {
      java.util.regex.Matcher m = inputPattern.matcher(line);
      if (m.find()) {
        String target = m.group(1);
//        System.out.println(target);
        nadded++;
        matcher.addTarget(target);
      } else {
        System.err.format("Weird line: '%s'%n", line);
      }
    }
  }


}