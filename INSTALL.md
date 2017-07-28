===============================================================================
      Managed Data Structures
      Copyright © 2016 Hewlett Packard Enterprise Development Company LP.
===============================================================================

# INSTALL instructions for Managed Data Structures (MDS)

Author: Susan Spence (susan.spence@hpe.com)

## Prerequisites

64-bit, Linux, C++ 14, JDK1.8, pthreads

- g++ 4.9.2, or g++ 5.4.0 or later   
  Note that MDS is known **not** to work with gcc 5.0, because of bugs in that version related to compilation of templates, which we use extensively.

- Java JDK 1.8   
  Note that you must use a Java JDK, not a more minimal Java installation.  This is needed for use of the JNI code.

- Managed Data Structures (MDS)

- Multi Process Garbage Collector (MPGC)

## Installations tested

* Linux: Debian Jessie (8.6), Ubuntu Xenial Xerus (16.04.1 LTS) on x86_64 architecture, [linux-l4fame](https://github.com/FabricAttachedMemory/linux-l4fame)

* g++ 4.9.2 /usr/local/gcc-4.9.2/bin/gcc

    * Compiled from: source downloaded from mirror via https://gcc.gnu.org/

* jdk 1.8 /opt/jdk/jdk1.8.0_25/bin/java

    * Installed from: http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html

* MDS: https://github.com/HewlettPackard/mds

* MPGC: https://github.com/HewlettPackard/mpgc

MDS runs on 64-bit Linux; it has been tested on x86 and ARM architectures.

**NB**: MDS has not been tested on Windows.  Early attempts to
  get the JNI component of the MDS Java API to compile and run proved
  fruitless.  Early versions of the MDS C++ API did run under Windows,
  but we have not attempted that configuration with recent code.

**NB**: MDS won't run on a virtual machine if the virtual machine doesn't support a shared mmap file.

## Building and Using the MDS C++ API

### Build process (MDS C++ API)

This assumes that `<mds>` and `<mpgc>` are sibling directories,
respectively named `mds` and `mpgc` containing the MDS and MPGC source
files and `<config>` is a build configuration, e.g., **intel-debug**
to create debuggable libraries or ***intel-opt** to create optimized
libraries.

1. Go the the appropriate build directory in `<mds>/cpp-api`:
~~~bash
cd <mds>/cpp-api/build/<config>
~~~

1. Build and install the library:
~~~bash
make -j8 install-recursive
~~~
  
   This will build all needed libraries (`libmds_cpp_api.a`,
   `libmds_core.a`, `libmpgc.a`, and `libruts.a`) and move all
   necessary libraries, header files, and tools to a sibling of `mds`
   and `mpgc` called `install`.  This target can be changed by setting
   `$(install_dir)`.

   It is strongly encouraged to use a `-j` parameter to allow parallel
   builds.  **The argument should not exceed the number of hyperthreads
   on the machine.**
   

### Using the MDS library (MDS C++ API)

To compile a C++ program that uses MDS, 

1. The source files should be compiled with `-I<install>/include`.

1. The program must link against the following libraries
   * `<install>/lib/libmds-cpp.a`
   * `<install>/lib/libmds_core.a`
   * `<install>/lib/libmpgc.a`
   * `<install>/lib/libruts.a`

1. The source code must
~~~cpp
#include "mds.h"
~~~
For detailed information on the MDS C++ API, see the documentation:
  https://github.com/HewlettPackard/mds/blob/master/doc/MDS%20C++%20API.pdf
  
### Demo and Test using MDS (C++)

Example programs using the MDS C++ API can be found in `<mds>/cpp-api/tests`.  To test out the implementation, we recommend building and running the `inventory` demo.  To build it the demo:

~~~bash
cd <mds>/cpp-api/build/<config>
make -j8 tests/inventory
~~~

This builds one application, `tests/inventory` that has two
subcommands, `init` and `run`.  To run the demo, within the `<config>`
directory, first create the MDS heap:

~~~
<install>/bin/createheap 10G
~~~

The parameter should be the desired heap size.  It should be less than
the amount of memory on the machine.

Next, initialize the demo:

~~~
./tests/inventory init
~~~

Finally, run the demo:

~~~
./tests/inventory run
~~~

Once the demo has been initialized, any number of `run` processes can
be created, and they can be killed and restarted at any time.  (Note
that eventually, the heap will fill up.)  By default, each process
runs with 5 worker threads in addition to a GC thread.  The number of
worker threads can be changed by specifying a `--nthreads` argument
**following** the `run` command.

  

## Building and Using the MDS Java API

### Build process (MDS Java API)

Set environment variable JAVA_HOME to point to the top-level directory of your JDK 1.8 installation.

Ensure the top-level MDS directory is called "mds".  (If you downloaded the zip file, it will be called mds-master and you will need to move it to mds instead.)

Assuming MDS and its dependency MPGC have been cloned to the same top-level directory, to compile sources for MDS:

    cd mds/java-api
    ant -f build-all.xml

The build will take about 30-40 minutes to complete on an average Linux server.

    
### Build details (MDS Java API)

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
By default it is built optimised; to build debug instead: 

    cd mds/java-api    
    ant -f build-all.xml -Dbuild=debug    

To see if the build has completed successfully, 
where <build> is either "opt" or "debug", 
check for the existence of the following files: 

    mds/java-api/jars/mds-annotations-processor.jar
    mds/java-api/jars/mds-java-api.jar
    mds/java-api/build/intel-<build>/libs/libmds-jni.so    

If you have a problem, and need to clean the build and start again: 

    cd mds/java-api
    ant -f build-all.xml clean


### Using the MDS library (MDS Java API)

To compile a Java application that uses MDS, the programmer uses the JAR files for the MDS Java API.

See the Usage section of [README.md](README.md) for a few simple code examples that illustrate basic use of MDS.

For more detailed information on the MDS Java API, see the documentation:
  https://github.com/HewlettPackard/mds/blob/master/doc/MDS%20Java%20API.pdf


### Demo using MDS (Java)

To see an example Java program using MDS, have a look at the MDS Inventory Demo in: [demo/inventory](demo/inventory).  
Follow the instructions in [demo/inventory/INSTALL.md](demo/inventory/INSTALL.md) to compile the demo sources and run the MDS Inventory demo.  

