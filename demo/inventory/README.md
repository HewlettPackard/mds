
===============================================================================
      Managed Data Structures
      Copyright © 2016 Hewlett Packard Enterprise Development Company LP.
===============================================================================

# MDS Inventory Demo

Author: Susan Spence (susan.spence@hpe.com)

Contributors: Evan Kirshenbaum, Susan Spence, Lokesh Gidra, 
Abraham Alcantara, Sergio Gonzalez, Sergei Uversky.


## Prerequisites:

64-bit, Linux, C++ 14, JDK1.8, pthreads

- g++ 4.9.2
- jdk 1.8

- Managed Data Structures (MDS)
- Multi Process Garbage Collector (MPGC)


NB: MDS does not compile on Windows.  We tried compiling it on Windows 
(we tried really quite hard) but there are compilation problems with 
libraries we use. So don't waste your time trying to compile on Windows;
just compile on Linux!


Installations tested: 

- g++ 4.9.2
  /usr/local/gcc-4.9.2/bin/gcc

  Compiled from:
  source downloaded from mirror via https://gcc.gnu.org/ 

- jdk 1.8
  /opt/jdk/jdk1.8.0_25/bin/java

  Installed from: 
  http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html

- MDS: https://github.com/HewlettPackard/mds

- MPGC: https://github.com/HewlettPackard/mpgc


## Command-line compilation: 

We typically do commandline compilation, 
for flexibility in supporting multiple compilation targets,
and ease of compilation over the network.

We assume builds have two repositories at top level: mds and mpgc.

To compile MDS from the command line: 

1. cd mds/java-api 

     Run "ant -f build-all.xml"

     - this will build all the Java classes under the java-api and the
       JNI shared library.

     For more information see the README file under java-api
     directory, but

     - to speed up the compilation of the shared library
       (dramatically) add "-Dthreads=8" (or some other number).

     - The shared library build expects to find JNI header files at
       "/opt/jdk1.8.0_51" in the "include" directory.  If this is not
       the case on your system, set "$(JDKHOME)" to the directory that
       contains the include dir.


2. cd mpgc/build/intel-opt

     Run "make tools/createheap"


3. cd mds/demo/inventory/

     mkdir libs
     cd libs

     In order to compile the project you will need to create some symbolic links the MDS dependencies and libraries.

     Use ln command to create links to this files.

        ln -s {java-api repo}/jars/mds-java-api.jar

        ln -s {java-api repo}/jars/mds-annotations-processor.jar

        ln -s {java-api repo}/external/log4j-1.2.15.jar
        
        ln -s {java-api repo}/external/commons-compress-1.1.jar

        ln -s {mpgc repo}/build/intel-opt/tools/createheap

    Finally, create the symbolic link to the MDS Library inside of the native folder, if not exist create one:

        cd demo/inventory/libs/native

        ln -s {java-api repo}/build/intel-opt/libs/libmds-jni.so


4. cd mds/demo/inventory

     Run "ant build"
     - this will build all the Java classes under demo/inventory
     - you may have to run this twice to get whole compilation to succeed 


If you change configs, run 'make install' to ensure that the
correct library is in the directory for the demo.


## Run demo inventory demos
    
Run demo3-multithread
- creates store and populates it with an inventory (with gaps in product naming)
- runs three processes in parallel to generate tasks working on inventory

  cd mds/demo/inventory/run/demo3-multithread/demo
  
  ../../../libs/createheap \<size\>
    - creates a heap of the specified size in GBs e.g. 30 for a 30GB heap

  ../rundemo3init
    - populates an inventory with products

  ../rundemo3shop1
    - runs transactions to order products and restock products in inventory
    - run three invocations of this script concurrently 
      to exercise multi-process support
  
For more detailed tests of this demo, follow execution instructions in: 
  git/demo/inventory/run/demo3-multithread/README
(also check out the other demo3 tests that are not explicitly mentioned in the
  demo3 readme)

## Notes:

  Persistent memory is currently simulated through the use of files.  You will
  most likely need to rerun the createheap executable in each demo 
  after changes have been made to the demos, to "clear the NVM".

-------------------------------------------------------------------------


