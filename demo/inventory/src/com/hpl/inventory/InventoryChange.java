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


package com.hpl.inventory;

import java.util.Random;
import org.apache.log4j.Logger;


public class InventoryChange {
	
	private static final Logger log = Logger.getLogger(InventoryChange.class);

    protected static final int productsMax = 1000000; // Up to 1M unique names
    protected static final int countMax = 1000;   // 0-999 count
    protected static final int valueMax = 100000; // $0-$1000 value
    protected static final double percentageUpdated = 0.01; // 1% products updated

    protected static Random nameRandom1 = new Random();

    protected String inventoryName;
    protected Inventory inventory;
    protected int inventorySize;
    protected int nbrProducts;
    protected Random nameRandom;   // used to generate the number in the productName
    protected Random nbrRandom;    // used to generate productCount, productValue
    protected int productNameTail = 0; // used as upper bound to generate number in productName
    protected boolean bias = false;    // used to limit productNames used to increase probability of conflicts


    public InventoryChange(String inventoryName) {
        this.inventoryName = inventoryName;
        this.nameRandom = new Random();
        this.nbrRandom = new Random();
    }

    public InventoryChange(String inventoryName, int productNameTail, boolean bias) {
    	this(inventoryName);
    	this.productNameTail = productNameTail;
    	this.bias = bias;
    }

    public InventoryChange(String inventoryName, int nbrProducts, long seed) {
        this.inventoryName = inventoryName;
        this.nbrProducts = nbrProducts;
        this.nameRandom = new Random(seed);
        this.nbrRandom = new Random();
    }


    public void lookupInventory() {
        //inventory = (Inventory) NameService.lookup(inventoryName);
        inventory = Inventory.TYPE.lookupName(inventoryName);
        if (inventory == null)
            throw new NullPointerException(
                "inventory " + inventoryName + " not found in NameService");
        // inventory.size() just returned zero if empty list
        // but now do I have to check products is non-null before calling size 
        // and set inventorySize to zero if products is null?
        // how about an Inventory.size method to do this then!
        inventorySize = inventory.getProducts().getSize();
    }


    /**
     * calcNbrProductsUpdated
     * 
     * return nbrProducts if already set
     * otherwise: 
     * nbrProductsUpdated calculated as percentage of 
     * the number of products in the inventory (inventorySize) 
     *
     * If the result is less than 1, return 1
     *   (so this task always does at least one update
     * Otherwise, calculated double result is rounded up and returned as int
     */
    public int calcNbrProductsUpdated() {
        if (nbrProducts > 0) return nbrProducts;

        double nbr = (inventorySize * percentageUpdated);
        return (nbr < 1) ? 1 : (int) Math.round(nbr);
    }


    public String generateProductName(boolean bias) {
    	int productNbr = 0;
    	if (bias) {
            productNbr = 1; // introducing extreme bias = all tasks work on product P00001!
    	}
    	else {
	        // Generate name (of product to be updated) using random number
	        // Product name composed of "P" plus nbr
	        // default: pad nbr to six digits (000000-999999) with leading zeros
	
	        // small inventory, large productsMax: likelihood of conflict low
	        // productNbr = random.nextInt(productsMax);
	        // ensure we always update an existing product to increase conflict
	        productNbr = (productNameTail > 0) ?
	            nameRandom1.nextInt(productNameTail) :
	            nameRandom.nextInt(inventorySize);
    	}
        String productName =
            "P" + Integer.toString(productNbr + productsMax).substring(1);
        log.debug("generateProductName: tail = " + productNameTail + " , productNbr = " + productNbr + ", productName = " + productName);
        return productName;
    }


    // testing: create and register a new inventory
    public static void createInventory(String inventoryName) {
        Inventory inventory = Inventory.create.record();
        //NameService.bind(inventoryName, inventory);
        inventory.bindName(inventoryName);
    }
    
    // load C++ library to use MDS
    static {
    	// System.out.format("Loading library: %s\n", System.mapLibraryName("mds-jni"));
    	log.debug("Loading library: " + System.mapLibraryName("mds-jni"));
        System.loadLibrary("mds-jni");  // MDS Java API JNI plus MDS Core - Linux
        // System.loadLibrary("libmds-jni");  // MDS Java API JNI plus MDS Core - Windows
    }

} // end class InventoryChange