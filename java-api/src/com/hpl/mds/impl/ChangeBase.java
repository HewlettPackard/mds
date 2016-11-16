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


package com.hpl.mds.impl;

/** ChangeBase
 *  the base class for all Change class types
 *  used to track object changes and compare them with conflicting changes.
 *  - Tracking changes (reads, writes, ...) during task execution for task-based conflict resolution
 *  - Representing conflicting changes in PubResult conflicts list on IsolationContext.publish failure
 */
abstract public class ChangeBase {

	/** 
	 * Every ChangeBase subclass must have attributes that represent the change subtype.
	 * For example, a change to an integer record field may be represented as: 
	 *       private long objectId;
     *       private long changeId;
     * where objectId contains the handle to the ManagedRecord
     * and   changeId contains the handle to the RecordField<int>
     * 
     * There are intentionally no default attributes in ChangeBase, to avoid mistaken comparisons.
     * Every change is compared on its type first and then, if the type matches, on its attributes.
	 */

	/** 
	 * The equals(), hashCode(), toString() and setToParent() methods are declared abstract here
	 * to force ChangeBase subclasses to provide their own implementations.
	 */
	@Override
    public abstract boolean equals(Object o);
	
	@Override
    public abstract int hashCode();
	
	@Override
	public abstract String toString();
	
	/**
	 * setToParent: 
	 * - whatever this Change represents in the current Isolation Context, 
	 * - set it to the state in the parent Isolation Context
	 */
	public abstract void setToParent();

} // end class ChangeBase