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

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.omg.CORBA.IntHolder;

import com.hpl.mds.annotations.Accessor;
import com.hpl.mds.annotations.Accessors;
import com.hpl.mds.annotations.RecordSchema;
import java.util.function.ToIntFunction;

@RecordSchema(name = "com.hpl.inventory.ListOfProducts")
public interface ListOfProductsSchema {
	
	@Accessor(Accessors.INCREMENT)
	int size();
	
	Product head();
	
	static Product findBefore(ListOfProducts.Private self, Predicate<? super Product> pred) {
		Product prev = null;
		for (Product current = self.getHead(); 
				current != null && !pred.test(current);
				prev = current, current = current.getNext()) 
		{
			// Nothing to do
		}
		return prev;
	}
	static Product findBefore(ListOfProducts.Private self, String name) {
		return findBefore(self, (p) -> name.compareTo(p.getName()) <= 0);
	}
	
	static Product findLast(ListOfProducts.Private self) {
		Product prev = null;
		for (Product current = self.getHead();
				current != null; 
				prev = current, current = current.getNext())
		{
			// Nothing to do
		}
		return prev;
	}
	
	static boolean add(ListOfProducts.Private self, Product product) {
		Product prev = findBefore(self, product.getName());

		product.setPrev(prev);
		if (prev == null) {
			// adding to head of list
			product.setNext(self.getHead());
			self.setHead(product);
		} else {
			product.setNext(prev.getNext());
			prev.setNext(product);
		}
		self.incSize(1);
		return true;
	}
	
	static boolean append(ListOfProducts.Private self, Product product) {
		Product prev = findLast(self);
		
		product.setPrev(prev);
		if (prev == null) {
			// adding to head of list
			product.setNext(self.getHead());
			self.setHead(product);
		}
		else {
			// adding to end of list
			// sanity check that the new product name should be last
			if (product.getName().compareTo(prev.getName()) >= 0) {
				// prev.name precedes or is equal to new product.name
				prev.setNext(product);
			}
			else {
				// throw exception complaining new product.name
				// should not be appended to end of list
				// because it doesn't belong last in list sorted by product.name
				return false;				
			}
		}
		self.incSize(1);
		return true;
	}
	
	static Product remove(ListOfProducts.Private self, String productName) {
        Product prev = findBefore(self, productName);
        
        Product candidate = prev == null ? self.getHead() : prev.getNext();
    
        if (candidate == null) { 
             return null;
        }
        if (!productName.equals(candidate.getName())) {
        	return null;
        }
        Product next = candidate.getNext();
        if (prev == null) {
        	self.setHead(next);
        } else {
        	prev.setNext(next);
        }
        if (next != null) {
        	next.setPrev(candidate.getPrev());
        }
        candidate.setPrev(null);
        candidate.setNext(null);
        return candidate;
	}
	
	static Product get(ListOfProducts.Private self, String productName) {
        Product prev = findBefore(self, productName);
        Product candidate = prev == null ? self.getHead() : prev.getNext();
        
        if (candidate == null) { 
             return null;
        }
        if (!productName.equals(candidate.getName())) {
        	return null;
        }
        return candidate;
	}
	
	static void forEach(ListOfProducts.Private self, Consumer<? super Product> sink) {
        Product current = self.getHead();
        while (current != null) {
        	sink.accept(current);
            current = current.getNext();
        }
	}
	
	static List<Product> asList(ListOfProducts.Private self) {
		List<Product> list = new ArrayList<>(self.getSize());
		self.forEach((p) -> list.add(p));
		return list;
	}
	
	static int total(ListOfProducts.Private self, ToIntFunction<? super Product> func) {
		IntHolder h = new IntHolder(0);
		self.forEach((p) -> h.value+=func.applyAsInt(p));
		return h.value;
	}
	
}