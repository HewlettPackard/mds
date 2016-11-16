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

package com.hpl.erk.config.type;

import java.util.HashSet;
import java.util.Set;

import com.hpl.erk.config.PType;
import com.hpl.erk.types.TypeToken;

public class SetType<ET> extends CollType<ET, Set<ET>> {
  private static Generic GENERIC = null;
  
  
  @SuppressWarnings("rawtypes")
  public static class Generic extends CollType.Generic<Set, Object> {
    private Generic() {
      super(TypeToken.generic(Set.class));
    }
    
    public final <ET> PType<Set<ET>> of(final PType<ET> eltType) {
      return this.of(PType.<Set<ET>>sig(), eltType);
    }
    
    @Override
    public <T extends Set, P1> PType<T> makeNewType(Sig<T> sig, TypeToken token, PType<P1> p1Type) {
      @SuppressWarnings("unchecked")
      final PType<T> listType = (PType<T>)new SetType<>(p1Type);
      return listType;
    }


  }

  protected SetType(PType<ET> eltType) {
    super(generic(), generic().token(eltType), eltType);
  }
  
  @Override
  public Set<ET> createEmpty() {
    return new HashSet<>();
  }

  public static Generic generic() {
    if (GENERIC == null) {
      GENERIC = new Generic();
    }
    return GENERIC;
  }
  
  
  public static <ET> PType<Set<ET>> of(PType<ET> eltType) {
    return generic().of(eltType);
  }

}