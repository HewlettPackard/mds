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

import com.hpl.erk.config.ConfigParam;
import com.hpl.erk.config.RunConfig;
import com.hpl.erk.config.RunContext;
import com.hpl.erk.config.ex.ConfigErrorsSeen;
import com.hpl.inventory.TextStyle.Color;
import com.hpl.mds.holder.IntHolder;

import static com.hpl.mds.IsolationContext.inReadOnlySnapshot;
import static com.hpl.inventory.Formatting.formatCurrency;

public class Demo3_Analysis extends Demo3Base {
	static final RunContext rc = demo3rc.subContext("report").activate();
    
    static final ConfigParam<String> nameParam = rc.param(String.class, "name")
    		.help("The name of the report")
    		.defaultVal("Simple Report");
	

    public static void main(String[] args) throws ConfigErrorsSeen {
    	args = RunConfig.process(Demo3_Analysis.class, args);

    	Inventory inventory = Inventory.TYPE.lookupName(invNameParam.v());
    	inReadOnlySnapshot(() -> {
        	ListOfProducts products = inventory.getProducts();

        	IntHolder total = new IntHolder();
        	products.forEach((p) -> total.value += p.getRevenue());
        	System.out.format("Revenue is %s%n", formatCurrency(total.value));

        	wait(1_000);
        	wait(1_000);
        	wait(1_000);
        	wait(1_000);

        	IntHolder new_total = new IntHolder();
        	products.forEach((p) -> new_total.value += p.getRevenue());
        	System.out.format("Revenue is now %s%n", formatCurrency(new_total.value));
        	
        	System.out.format("The snapshot %s%n", 
        			total.value == new_total.value ? Color.Green.bg(Color.Black).format("worked :-)")
        					: Color.Red.bg.format("didn't work :-("));
    		
    	});

}
    
    private static void wait(int ms) {
    	System.out.format("Waiting%n");
    	try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
			// ignore
		}
    }

} 

