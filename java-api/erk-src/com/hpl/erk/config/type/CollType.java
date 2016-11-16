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

import java.util.Collection;

import com.hpl.erk.ReadableString;
import com.hpl.erk.config.CollectionCP;
import com.hpl.erk.config.ConfigParam;
import com.hpl.erk.config.DelayedVal;
import com.hpl.erk.config.GenericCache;
import com.hpl.erk.config.GenericType;
import com.hpl.erk.config.PType;
import com.hpl.erk.config.ReadDelayedVal;
import com.hpl.erk.config.RunContext;
import com.hpl.erk.config.ex.IllegalValueException;
import com.hpl.erk.config.ex.ReadError;
import com.hpl.erk.types.GenericTypeToken;
import com.hpl.erk.types.TypeToken;

public class CollType<ET, CT extends Collection<ET>> extends OneArgCompositeType<CT, ET> implements SeqType<CT,ET>{
  private final Generic<? super CT, ? super ET> cgeneric;
  
  
  @SuppressWarnings("rawtypes")
  public static class Generic<GT extends Collection, B1> extends OneArgCompositeType.Generic<GT, B1> {
    private CollectionFactory factory = null;
    
    public abstract class CollectionFactory {
      protected CollectionFactory() {
        factory = this;
      }
      public abstract <ET extends B1> GT makeEmpty(PType<ET> etype);
    }
    
    public Generic(GenericTypeToken generic) {
      super(generic);
    }
    
    public final <ET extends B1, T extends GT> PType<T> of(Sig<T> sig, final PType<ET> eltType) {
      return CollType.of(sig, this, eltType);  
    }
    
    public <CT extends GT, ET extends B1> CT makeEmpty(PType<CT> ctype, PType<ET> etype) {
      if (factory == null) {
        throw new IllegalStateException(String.format("%s doesn't know how to make a %s", this, ctype));
      }
      @SuppressWarnings("unchecked")
      CT val = (CT)factory.makeEmpty(etype);
      return val;
    }
    
    public void setFactory(CollectionFactory factory) {
      this.factory = factory;
    }
    
    @Override
    public <T extends GT, P1 extends B1> PType<T> makeNewType(PType.Sig<T> sig, TypeToken token, PType<P1> p1Type) {
      return new CollType<>(this, token, p1Type);
    }
    
    
  }
  
//  public static interface CollectionFactory<GT extends Collection, B1> {
//    public <CT extends GT, ET extends B1> CT makeEmpty(Type<CT> ctype, Type<ET> etype);
//  }

  protected CollType(GenericType<? super CT> generic, TypeToken typeToken, PType<ET> eltType) {
    super(generic, typeToken, eltType);
    if (!(generic instanceof Generic)) {
      throw new IllegalArgumentException(String.format("%s is not a Collection Generic for %s", generic, typeToken));
    }
    @SuppressWarnings("unchecked")
    Generic<? super CT, ? super ET> downcast = (Generic<? super CT, ? super ET>)generic;
    cgeneric = downcast;
  }

  public CT createEmpty() {
    return cgeneric.makeEmpty(this, p1Type);
  }
  
  @Override
  public Collection<ET> asCollection(CT composite) {
    return composite;
  }
  
  @Override
  public PType<CT> asType() {
    return this;
  }

  @Override
  public CT copyFrom(Collection<? extends ET> val) {
    CT collection = createEmpty();
    asCollection(collection).addAll(val);
    return collection;
  }
  
  @Override
  public PType<ET> elementType() {
    return p1Type;
  }
  
  @Override
  public TypeToken typeToken() {
    return typeToken;
  }
  

  /**
   * @throws ReadError  
   * @throws ReadDelayedVal 
   */
  protected CT readSpecialForms(ReadableString input, int resetTo) throws ReadError, ReadDelayedVal {
    return null;
  }

  private Collection<ET> asCol(CT collection) {
    return collection;
  }
  
  @Override
  public CT readVal(ReadableString input, String valTerminators) throws ReadError, ReadDelayedVal {
    int resetTo = input.getCursor();
    CT collection = readSpecialForms(input, resetTo);
    Collection<ET> coll = asCol(collection);
    if (collection != null) {
      return collection;
    }
    PType<ET[]> arrayType = PType.arrayOf(elementType());
    try {
      ET[] array = arrayType.readVal(input, valTerminators);
      collection = createEmpty();
      coll = asCol(collection);
      for (ET elt : array) {
        coll.add(elt);
      }
      return collection;
    } catch (ReadDelayedVal e) {
      final DelayedVal<? extends ET[]> arrayDV = e.<ET[]>delayedVal();
      final DelayedVal<CT> dv = new DelayedVal<CT>(arrayDV.desc) {
        @Override
        public CT force() throws IllegalValueException {
          CT collection = createEmpty();
          ET[] array = arrayDV.force();
          for (ET elt : array) {
            collection.add(elt);
          }
          return collection;
        }
      };
      throw new ReadDelayedVal(dv);
    }
  }
  
  @Override
  public ConfigParam<CT> configParam(RunContext context, String name) {
    return new CollectionCP<>(context, name, this, p1Type);
  }

  /*
  public static <P1, T extends Collection<P1>> CollType<P1,T> of(Sig<T> sig, final GenericType<? super T> generic, final Type<P1> p1Type) {
    final TypeToken token = generic.generic.bind(p1Type.typeToken);
    Factory<T> factory = new Factory<T>() {
      @Override
      public Type<T> create() {
        return new CollType<>(generic, token, p1Type);
      }
    };
    return (CollType<P1,T>)Type.of(token, factory);  

  }
  */

  @SuppressWarnings("rawtypes")
  public static <GT extends Collection, B1> Generic<GT,B1> generic(final GenericTypeToken gtoken, final PType<B1> bound) {
    GenericCache.Factory<GT> factory = new GenericCache.Factory<GT>() {
      @Override
      public Generic<GT, B1> create() {
        return new Generic<GT,B1>(gtoken);
      }
    };
    return (Generic<GT, B1>)GenericType.cache.lookup(gtoken, factory);  
  }

}