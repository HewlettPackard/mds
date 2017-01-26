
===============================================================================
      Managed Data Structures
      Copyright © 2016 Hewlett Packard Enterprise Development Company LP.
===============================================================================

# MDS Inventory Demo

Author: Susan Spence (susan.spence@hpe.com)

Contributors: Evan Kirshenbaum, Susan Spence, Lokesh Gidra, 
Abraham Alcantara, Sergio Gonzalez, Sergei Uversky.


## Prerequisites:

The prerequisites for running the MDS Inventory Demo are the same as those 
for building MDS.  See: [MDS INSTALL.md - Prerequisites section](https://github.com/HewlettPackard/mds/blob/master/INSTALL.md#Prerequisites)


## Build process: 

1. To build MPGC and MDS, follow the instructions in [MDS INSTALL.md - Build process section](INSTALL.md#Buildprocess).


2. Build the createheap tool, which is used by MDS applications 
to create a "Managed Space" to hold persistent managed data structures.
Where {mpgc build} is mpgc/build/intel-opt for optimised build, 
or mpgc/build/intel-debug for debug build:  

    cd {mpgc build}
    make tools/createheap


3. Set up demo access to MDS dependencies and libraries: 

    cd mds/demo/inventory/   
    mkdir libs      
    cd libs

   In order to compile the project you will need to create some symbolic links the MDS dependencies and libraries.  
Where {mds} is the top-level directory of the mds installation, 
and {mds build} is mds/java-api/build/intel-opt for optimised build,
or mds/java-api/build/intel-debug for debug build:

   Use the ln command to create links to these files:

        ln -s {mds}/java-api/jars/mds-java-api.jar

        ln -s {mds}/java-api/jars/mds-annotations-processor.jar

        ln -s {mds}/java-api/external/log4j-1.2.15.jar
        
        ln -s {mds}/java-api/external/commons-compress-1.1.jar

        ln -s {mpgc build}/tools/createheap

   Finally, create the symbolic link to the MDS Library inside of the native folder, if not exist create one:

        cd demo/inventory/libs/native   
        ln -s {mds build}/libs/libmds-jni.so

**NB:** If you change build configuration, say from optimised to debug, 
ensure that the correct build of the mds library libmds-jni.so 
is linked in the mds/demo/inventory/libs/native directory for the demo.


4. Build the demo 

    cd mds/demo/inventory   
    ant build

This will build all the Java classes under demo/inventory.   
You may have to run this build twice to get the whole compilation to succeed.


## Run MDS inventory demo
    
The MDS inventory demo scripts and configuration files can be found under the demo3-multithread directory.  Running the MDS inventory demo:
- creates a persistent data store and populates it with an inventory of products (with gaps in product naming);
- runs three "shop" processes in parallel to generate tasks that make changes to the inventory.

First, ensure the demo configuration is set to pick up 

    cd mds/demo/inventory/run/demo3-multithread/demo
  
    ../rundemo3init
    - creates a store by invoking createheap with a specified size of 1 GB.
    - populates an inventory with products

    ../rundemo3shop1
    - runs transactions to order products and restock products in inventory
    - run three invocations of this rundemo3shop1 script concurrently 
      to exercise multi-process support

    ../rundemo3report
    - run this process concurrent with the shop processes 
    - prints to stdout a report on a snapshot of the state of the inventory of products, and then terminates.

The three demo3shop1 processes will run continously until interrupted.

**NB:** If the processes appear to be running sluggishly, 
this may be because they are accessing the persistent store across NFS.
To avoid this problem, you need to ensure the store is created on a local disk, 
or that it is created in RAM. 

To ensure the store is created in RAM with tmpfs: 
    - you will see a directory in your current directory called "heaps".  This is the directory containing the files for your persistent store.  It was created when you invoked the rundemo3init script.
    - The tmpfs filesytem uses a standard memory device on Linux called /dev/shm.  To create a heaps directory on this memory device: delete the existing heaps directory and create a symbolic link to a heaps directory on /dev/shm: 
        
    cd mds/demo/inventory/run/demo3-multithread/demo
    rm -rf heaps
    mkdir /dev/shm/heaps
    ln -s /dev/shm/heaps

    - Now, rerun the steps above, starting from rundemo3init, to create the store and run the demo, using local memory.

  
For a list of other tests associated with this demo, 
see demo/inventory/run/demo3-multi-thread/README.md.

## Notes:

  Persistent memory is currently simulated through the use of files.  You will
  most likely need to rerun the createheap executable in each demo 
  after changes have been made to the demos, to "clear the NVM".

-------------------------------------------------------------------------


