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

package com.hpl.mds.annotations.processors;

public class Util {

    private static final JCompiler COMPILER = JCompiler.getInstance();
    private ParserConfig config;
    private String testPath;
    private String testPkg;

    public Util(String testPath, String testPkg) {
        this.testPath = testPath;
        this.testPkg = testPkg;
    }

    public void setConfig(ParserConfig config) {
        this.config = config;
    }

    public void testSchema(String className, String mdsType) throws Exception {
        COMPILER.compileSchema(testPath + className + "Schema.java");
        testGeneratedRecord(className, mdsType);
    }

    public void testSchema(String className) throws Exception {
        testSchema(className, testPkg + className);
    }

    public void testWithOffPkgClasses(String className, String... classFiles) throws Exception {
        COMPILER.compileSchema(classFiles);
        COMPILER.compileSchema(testPath + className + "Schema.java");
        testGeneratedRecord(className, testPkg + className);
    }

    public void testWithClasses(String className, String... classes) throws Exception {
        String[] fileNames = new String[1 + classes.length];
        fileNames[classes.length] = testPath + className + "Schema.java";
        for (int i = 0; i < classes.length; i++) {
            fileNames[i] = testPath + classes[i] + ".java";
        }
        COMPILER.compileSchema(fileNames);
        testGeneratedRecord(className, testPkg + className);
    }

    public void testGeneratedRecord(String className) throws Exception {
        testGeneratedRecord(className, testPkg + className);
    }

    private void testGeneratedRecord(String className, String mdsType) throws Exception {
        config.setMdsType(mdsType);
        config.setClassName(className);
        String javaSrcFile = testPath + className + ".java";
        COMPILER.compileLoadRecord(javaSrcFile);
        COMPILER.parse(javaSrcFile, config);
    }

    public void addPublic(String methodName, String returnType, String... args) {
        MethodDesc methodDesc = new MethodDesc(methodName, returnType, args);
        config.addPublic(methodDesc);
        config.addPrivate(methodDesc);
    }

    public void addProtected(String methodName, String returnType, String... args) {
        MethodDesc methodDesc = new MethodDesc(methodName, returnType, args);
        config.addProtected(methodDesc);
        config.addPrivate(methodDesc);
    }

    public void addPrivate(String methodName, String returnType, String... args) {
        config.addPrivate(new MethodDesc(methodName, returnType, args));
    }

}