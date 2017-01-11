===============================================================================
      Managed Data Structures
      Copyright © 2016 Hewlett Packard Enterprise Development Company LP.
===============================================================================

# INSTALL instructions for Managed Data Structures (MDS)

Author: Susan Spence (susan.spence@hpe.com)

## Prerequisites

64-bit, Linux, C++ 14, JDK1.8, pthreads

- g++ 4.9.2

- jdk 1.8

- Managed Data Structures (MDS)

- Multi Process Garbage Collector (MPGC)

## Installations tested

* g++ 4.9.2 /usr/local/gcc-4.9.2/bin/gcc

    * Compiled from: source downloaded from mirror via https://gcc.gnu.org/

* jdk 1.8 /opt/jdk/jdk1.8.0_25/bin/java

    * Installed from: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html

* MDS: https://github.com/HewlettPackard/mds

* MPGC: https://github.com/HewlettPackard/mpgc

MDS runs on 64-bit Linux; it has been tested on x86 and ARM architectures.

**NB**: MDS does not compile on Windows. We tried compiling it on Windows (we tried
really quite hard) but there are compilation problems with libraries we use. So
don't waste your time trying to compile on Windows; just compile on Linux!

## Build process

Assuming MDS and its dependency MPGC have been cloned to the same top-level directory, to compile sources for MDS:

    cd mds/java-api
    ant -f build-all.xml
    
## Build details

We use ant at the top level and in our demo directories, with build files that were handcrafted by one of our developers.

The C++ implementation of MDS and MPGC is built using an Autobuild process which is documented in
    [java-api/build/BUILDING.md](java-api/build/BUILDING.md)

The build process:
- compiles the code common to both MDS and MPGC first, into libruts.a;
- then compiles the code for MPGC into libmpgc.a;
- then compiles the MDS core implementation and JNI code and incorporates it, libruts.a and libmpgc.a into libmds-jni.so;
- then compiles the MDS Java API and annotations processing code into two jar files mds-annotations-processor.jar and mds-java-api.jar.


The code common to both MDS and MPGC is compiled to libruts.a:
where "ruts" stands for "Really Useful ToolS".

The MDS library libmds-jni.so can be built either optimised or debug.

When built optimized, the .so library is created under
    mds/java-api/build/intel-opt/libs

When built debug, the .so library is created under
    mds/java-api/build/intel-debug/libs


## Using the MDS library

To compile a Java application that uses MDS, the programmer uses the JAR files for the MDS Java API.

See the Usage section of [README.md](README.md) for a few simple code examples that illustrate basic use of MDS.

For more detailed information on the MDS API, see the documentation:
  https://github.com/HewlettPackard/mds/blob/master/doc/MDS%20Java%20API.pdf

To see an example program using MDS, have a look at the MDS Inventory Demo in: [demo/inventory](demo/inventory).  
Follow the instructions in [demo/inventory/README.md](demo/inventory/README.md) to compile the demo sources and run the MDS Inventory demo.  

