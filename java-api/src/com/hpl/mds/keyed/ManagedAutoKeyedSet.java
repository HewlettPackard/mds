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

package com.hpl.mds.keyed;

import com.hpl.mds.ManagedCollection;
import com.hpl.mds.ManagedContainer;
import com.hpl.mds.ManagedObject;
import com.hpl.mds.ManagedSet;
import com.hpl.mds.impl.Stub;

public interface ManagedAutoKeyedSet<EK extends ManagedObject, V extends ManagedObject & AutoKeyed<? extends EK>> 
extends ManagedKeyedSet<EK,V> {
  interface ForManagedAutoKeyedSet extends ManagedKeyedSet.ForManagedKeyedSet {}
  interface UsageOps<UF extends ForManagedAutoKeyedSet, U extends UsageOps<UF,U>> {}
  interface Usage extends UsageOps<ForManagedAutoKeyedSet, Usage> {}
  static Usage usage() {
    // TODO
    return Stub.notImplemented();
  }
  
  interface Type<EK extends ManagedObject, V extends ManagedObject> extends ManagedKeyedSet.Type<EK,V> {
    ManagedKeyedSet<EK, V> create(Usage hints);
    ManagedKeyedSet<EK, V> create(ManagedKeyedSet.Usage hints);
    ManagedKeyedSet<EK, V> create(ManagedSet.Usage hints);
    ManagedKeyedSet<EK, V> create(ManagedCollection.Usage hints);
    ManagedKeyedSet<EK, V> create(ManagedContainer.Usage hints);
  }
  
  default boolean addKeyed(V val) {
    return addKeyed(val, val.keyed());
  }
  
  


}