
===============================================================================
      Managed Data Structures
      Copyright © 2016 Hewlett Packard Enterprise Development Company LP.
===============================================================================

## MDS Inventory Demo

Author: Susan Spence (susan.spence@hpe.com)

Contributors: Evan Kirshenbaum, Susan Spence, Lokesh Gidra,
Abraham Alcantara, Sergio Gonzalez, Sergei Uversky.

Documentation on demo scripts associated with the Inventory demo in this demo3-multithread directory.

## Running the demo

See ../../INSTALL.md for instructions on building and running the Inventory demo.

There are a number of additional tests that can be run, beyond what is described in the instructions above.
 
## Running multiple instances of the Inventory demo concurrently.

This demo supports running multiple instances with different heaps.  
This is a feature supported for MDS Visualisation Dashboard testing internally.

For each instance you need to first create a new sub-directory, for example:

    mkdir demo
    cd demo

Once inside the directory, 
use the following scripts to populate and execute a demo instance.

## Demo scripts

### Demo scripts used in basic MDS Inventory Demo testing

createheap   
- Creates a persistent data store. 
- When executed, this script creates two files representing the store, 
  in a heaps directory in the current directory.
- Creates a 1 GB heap by default.
- To create larger heaps, invoke the createheap executable with a size parameter.
    ../../../libs/createheap <size>

- For example, to create a heap that is 30 GB in size: 

    cd mds/demo/inventory/run/demo3-multithread/demo
    ../../../libs/createheap 30


setup-run   
- every MDS demo uses a setup-run script to set up the execution environment for that demo.  
- It should not be necessary to edit this script to run the basic MDS Demo Inventory scripts described in ../../INSTALL.md.
- Many MDS demos share one common setup-run script.  However, demo3-multithread uses its own setup-run script, to ensure the environment is set correctly for MDS Visualisation Dashboard testing internally.
- The demo3 setup-run script adds to the path libraries that are specific to demo3 for internal testing including RabbitMQ.  These can be safely ignored by external users.


rundemo3init            
- creates store and populates it with an inventory (with gaps in product naming)

rundemo3shop1  
rundemo3shop2
rundemo3shop3
- run these three processes in parallel to generate tasks working on inventory
- the only difference between these three scripts is the reported name of the shop - useful for demo purposes only to emphasise that these are three separate processes.


### Other Inventory Demo scripts 

rundemo3report
- run this process concurrent with one or more of the shop processes to get a test report on a snapshot of the state of the inventory of products.

rundemo3shop   
- a one-script test, to create a store and run a single shop with default parameters.

rundemo3shopbias
- test run shop with bias set true - all tasks work on product P00001

runmdsuser3
- run to generate report on current contents of inventory in store



