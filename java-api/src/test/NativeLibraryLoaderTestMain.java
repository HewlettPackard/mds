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

package test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Scanner;

public class NativeLibraryLoaderTestMain {

	private static final int TOTAL_TEST = 2;

	private int passedTests = 0;

	private interface Test {
		public boolean run() throws Exception;
	}

	public static void main(String[] args) {
		NativeLibraryLoaderTestMain test = new NativeLibraryLoaderTestMain();
		test.testDefaultConfiguration();
		test.testLoadingFromProperty();
		test.printResults();
	}

	/**
	 * Verifies the loading of the native library by setting a system property
	 */
	private void testLoadingFromProperty() {
		runTest(() -> {
			Path tempFile = Files.createTempFile("lib", ".so");
			return runProcess("java -classpath .:bin:external/log4j-1.2.15.jar -Dmds.jni.library.path="
					+ tempFile.toAbsolutePath().toString() + " "
					+ NativeLibraryLoaderChildTest.class.getCanonicalName());
		});
	}

	/**
	 * Prints the test results
	 */
	private void printResults() {
		System.out.println("test passed: " + passedTests + " / " + TOTAL_TEST);
	}

	/**
	 * Verifies the loading of the native library with default configuration
	 */
	public void testDefaultConfiguration() {
		runTest(() -> {
			Path tempDir = Files.createTempDirectory("mdsTest");
			Files.createFile(tempDir.resolve(System.mapLibraryName("mds-jni")));
			return runProcess("java -classpath .:bin:external/log4j-1.2.15.jar -Djava.library.path="
					+ tempDir.toAbsolutePath().toString() + " "
					+ NativeLibraryLoaderChildTest.class.getCanonicalName());
		});
	}

	/**
	 * Runs the given test safely, printing any exception and counting the
	 * passed tests
	 * 
	 * @param test
	 */
	private void runTest(Test test) {
		try {
			if (test.run()) {
				passedTests++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Executes the given command in a new process and verifies the content of
	 * the process output
	 * 
	 * @param command
	 * @return true if the log of the process contains expected messages, false
	 *         otherwise
	 * @throws InterruptedException
	 *             if the caller thread gets interrupted while waiting the
	 *             process to terminate
	 * @throws IOException
	 *             if fails to run the process or retrieving its output
	 */
	private static boolean runProcess(String command) throws InterruptedException, IOException {
		Process process = Runtime.getRuntime().exec(command);
		process.waitFor();
		boolean result = false;
		try (InputStream inputStream = process.getInputStream(); Scanner scanner = new Scanner(inputStream);) {
			while (scanner.hasNext()) {
				String currentLine = scanner.nextLine();
				System.out.println(currentLine); // for debug
				if (currentLine.contains(UnsatisfiedLinkError.class.getCanonicalName())
						&& currentLine.contains("file too short")) {
					result = true;
				}
			}
		}
		System.out.println("-----------------------------------------");
		return result;
	}

}