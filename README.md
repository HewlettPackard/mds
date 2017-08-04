===============================================================================
      Managed Data Structures
      Copyright © 2016 Hewlett Packard Enterprise Development Company LP.
===============================================================================

# Managed Data Structures (MDS)

Author: Susan Spence (susan.spence@hpe.com)

Contributors: Evan Kirshenbaum, Susan Spence, Lokesh Gidra,
Abraham Alcantara, Sergio Gonzalez, Sergei Uversky.


## Description

The Managed Data Structures library delivers a simple, high-level
programming model for persistent memory.  It is designed to take full
advantage of the large, random-access, non-volatile memory and
highly-parallel processing of The Machine and other persistent memory
architectures.  Application programmers use and persist their data
directly in their application, in common data structures such as
lists, maps and graphs, and the MDS library manages this data.  The
library supports multi-threaded, multi-process creation, use and
sharing of managed data structures, via APIs in multiple programming
languages, Java and C++.

## Source

Releases of the MDS source code are available open source on GitHub:
https://github.com/HewlettPackard/mds

Maturity: Alpha.

The Managed Data Structures software library has been developed as
part of the Managed Data Structures research project at Hewlett
Packard Labs.  As such, releases of the MDS source code made
available on GitHub are not production quality, but rather "alpha"
quality.  The MDS APIs have been specified and they have been
implemented sufficiently to demonstrate proof of concept so far.
However, MDS is not functionally complete; we have documented which
APIs have not yet been implemented, and an attempt to call an
implemented method will result in a "stub not implemented" error
message.  MDS releases have gone through basic testing, but may be
unstable and could cause crashes or data loss.

MDS is an active research project; we intend to make further releases
with more and/or better features and bug fixes as the project
continues.

## License

Managed Data Structures is distributed under the LGPLv3 license 
with an exception.
See license files [COPYING](COPYING) and [COPYING.LESSER](COPYING.LESSER).

## Dependencies

Multi-Process Garbage Collector: https://github.com/HewlettPackard/mpgc

## Usage

Documentation on The Managed Data Structures Library: Java API is
available on the MDS GitHub site:
https://github.com/HewlettPackard/mds/blob/master/doc/MDS%20Java%20API.pdf

The Managed Data Structures Library: C++ API is available on the MDS
GitHub site:
https://github.com/HewlettPackard/mds/blob/master/doc/MDS%20C++%20API.pdf

The user guides describe in detail how Java and C++ applications can create
and use managed data structures in an MDS managed heap, using the MDS
Java API, and how they can safely share those data structures between
processes, using high-level transactional support provided by the MDS
library.

Here are a few simple code examples, to illustrate some basic use of
MDS. These examples use the MDS Java API.

### 1. Create a managed data structure

A programmer creates a managed data structure using the MDS API.  For
example, using the MDS Java API, the programmer can write code in
their Java application to create a ManagedArray of integers:

    ManagedIntArray inventory_i = ManagedInt.TYPE.createArray(size);

- **Managed Space**. When a programmer creates a managed data
    structure, like this ManagedArray, MDS allocates this object
    directly in the MDS Managed Space. This Managed Space is one
    large virtual pool of memory; a shared, persistent heap in which
    MDS allocates and manages MDS application objects, with the help
    of the Multi-Process Garbage Collector.
- **Create object**. Note that MDS objects are allocated in the MDS
    Managed Space using create method calls on an object type; in
    contrast with a programmer calling the new method to create a
    standard Java object in the Java single-process volatile heap.
- **Typed object**. Note also that all MDS objects are strongly
    typed, so that the programmer always knows what type of object
    they are dealing with.  An object is typically created with a
    well-defined type, by creating it via a create method on the
    object type itself.  In the case of managed arrays, managed types
    support a createArray method to create a managed array of that
    type: thus, ManagedInt.TYPE, the managed type for integers in
    MDS, supports a createArray method call to create instances of
    ManagedIntArray, for managed arrays of integers.
- **Supported types**. MDS is designed to support primitive types,
    strings, data structures and records, and it enables users to
    specify their own record types composed of these MDS supported
    types.
- **Records via annotations**. To avoid the user having to write a
    lot of boiler-plate code for a ManagedRecord type, MDS provides
    support for high-level specification of a ManagedRecord type
    using Java annotations; the programmer then runs an annotations
    processor provided with MDS to automatically generate a lot of
    the standard code for accessing and using objects of that
    ManagedRecord type.

### 2. Use a managed data structure

The programmer uses an MDS object just like a standard Java object in
a Java application.

They can populate an MDS data structure with elements.

    inventory_i.set(0,0);
    inventory_i.set(1,1);
    inventory_i.set(2,2);

The MDS Java API supports a style of programming that is close to
that of standard Java code, as much as possible. Thus, we have
defined ManagedIntArray so that it can take integers directly as
arguments.

However, given that a ManagedIntArray is really a managed array of
ManagedInt elements, the programmer could also write equivalent code
to create a ManagedInt object, using a call to a create method on the
type as described before, and set an element of the ManagedIntArray
to reference this new ManagedInt object.

    ManagedInt intElement = ManagedInt.Type.create(1);
    inventory_i.set(1,intElement);

Then the programmer can use the ManagedArray and its elements,
writing very similar code to standard Java.  They can get and set
elements of the array:

    ManagedInt productCount = inventory_i.get(2);
    newCount = productCount.asInt() - countDecr;
    inventory_i.set(2, newCount);

They can iterate through the array elements:

    int total = 0;
    for (ManagedInt i: inventory_i) {
        System.out.println("inventory: i: " + i.asInt());
        total += i.asInt();
        System.out.println("total so far: " + total);
    }
    System.out.println("Total inventory: " + total);

### 3. Share a managed data structure between multiple processes

To make an MDS object accessible beyond the process that creates it,
we bind the object to a name in the MDS namespace.

    inventory_i.bindName("inventory_i");

A different application process can then look up the same MDS object
by name, either concurrently or long after the original creator
process has finished, and get back a reference to it in the MDS
Managed Space.

    ManagedIntArray inventory_i = ManagedIntArray.TYPE.lookupName("inventory_i");

### 4. Use a managed record

MDS enables programmers to specify their own record types composed of
the MDS supported types, as mentioned above.  To avoid the user
having to write a lot of boiler-plate code for a ManagedRecord type,
MDS provides support for high-level specification of a ManagedRecord
type using Java annotations; the programmer then runs an annotations
processor provided with MDS to automatically generate a lot of the
standard code for accessing and using objects of that ManagedRecord
type. The MDS user guide goes into detail about how a programmer
defines the basics of a record type and runs it through the
annotations processor to get a complete definition.  To keep this
introduction to MDS usage simple, we elide the details of record type
definition here and focus on a simple example of usage.

Following on from our initial example, showing creation and use of a
managed array of integers, the creation and use of a managed array of
ManagedRecord objects looks very similar.  Given a ManagedRecord
definition for a Product type, the code for creating a managed array
of Product objects is:

    ManagedArray<Product> inventory_p = Product.TYPE.createArray(size);
    Product product = null;
    product = Product.create.record("A4 Art Pad", 100, 200);
    inventory_p.set(0,product);
    product = Product.create.record("Pencil HB", 1000, 10);
    inventory_p.set(1,product);
    product = Product.create.record("English Oxford Dictionary", 50, 450);
    inventory_p.set(2,product);

### 5. Share a managed data structure safely

**Atomic operations**. To make a simple modification to a managed
  object, we can use atomic operations supported on that object:

    Product product = inventory_p.get(2);
    product.decCount(1);

**Transactional IsolationContext**. To make an update to a managed
  object, that might potentially conflict with an update being made
  concurrently by another thread or process, we can isolate the
  update in an MDS transactional IsolationContext:

    isolated(() -> {
        inventory_p.set(1, null);
    });

**Snapshots**. To run an analysis on managed objects, while other
  threads or processes may be updating them concurrently, we can run
  the analysis on a light-weight, zero-copy snapshot of the data:

    inReadOnlySnapshot(() -> {
        int total = 0;
        for (Product p: inventory_p) {
            if (p != null) {
                total += p.getCount();
            }
        }
        System.out.println("Total inventory count: " + total);
    });

### 6. Using the library

MDS runs and has been tested on x86 and ARM architectures.

To compile a Java application that uses MDS, the programmer uses the
JAR files for the MDS Java API.

Details for compiling MDS can be found in [INSTALL.md](INSTALL.md). 
The basics are as follows...

Assuming MDS and its dependency MPGC have been cloned to the same 
top-level directory, to compile sources for MDS: 

    cd mds/java-api
    ant -f build-all.xml

To compile sources for MDS Inventory demo: 
follow instructions in mds/demo/inventory/README.md

The programmer loads the MDS library dynamically during the initial
execution of their Java program, by invoking System.loadLibrary from
a static code block:

    static {
        System.loadLibrary("mds-jni");
    }

The programmer then uses the MDS Java API by simply invoking method
calls to methods of the MDS Java API classes.

Full details of how to use the MDS library are described in our user
guide and illustrated in demo code supplied as part of the MDS open
source release.

To compile a C++ application that uses MDS, assuming that `<install>`
refers to the directory that was the target of `make install`
(typically `install` as a sibling of `mds` and `mpgc`):

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

## Notes

MDS can be used for object management and sharing by multiple
application processes using a shared object heap in shared (volatile)
memory: the MDS Managed Space is a file mmapped into memory and, on
normal process shutdown, changes to objects in Managed Space will be
written back to that file.  However, MDS is really designed to
deliver the benefits of persistent memory programming on The Machine
and other persistent memory architectures.  To benefit from the
fault-tolerant aspects of MDS (and its fault-tolerant automatic
memory management provided by MPGC), where MDS object state is
maintained consistently in the face of process and system failures,
MDS should be run in a system with persistent memory where caches are
flushed on failure; otherwise, we do not guarantee that the heap will
be uncorrupted after failures.

**Limitations**.
MDS currently supports only a limited number of managed types: it
supports primitives, strings, user-defined managed records, and
managed arrays of primitives, strings and records.  Managed arrays of
managed arrays are not yet supported.

**Known bugs**.
We know that the current MDS release has memory leaks as a result of
holding on to the entire version history of objects.  This will be
addressed in future releases.

## See Also

- The Multi Process Garbage Collector, which is used by MDS for
  its automatic memory management:
  https://github.com/HewlettPackard/mpgc

Other tools for programming with non-volatile memory include:

- [Atlas](https://github.com/HewlettPackard/Atlas): Fault-tolerant programming model for non-volatile memory
- [FOEDUS](https://github.com/hkimura/foedus): Fast optimistic engine for data unification services

## Release History

- **1.0** *2016-11-15*
  - Initial release.  MDS core.  Java API
  
- **2.0** *2017-08-07*
  - Added C++ API
  - Significant internal redesign for performance and correctness.
