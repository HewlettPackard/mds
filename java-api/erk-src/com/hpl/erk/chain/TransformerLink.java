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

package com.hpl.erk.chain;

import com.hpl.erk.func.NullaryFunc;
import com.hpl.erk.func.SourceExhausted;
import com.hpl.erk.func.UnaryFunc;

public class TransformerLink<Head, In, Out> extends Link<Head, In, Out> {
  protected final NullaryFunc<? extends UnaryFunc<? super In, ? extends Out>> creator; 

  protected TransformerLink(Chain<Head, ? extends In> pred,
      NullaryFunc<? extends UnaryFunc<? super In, ? extends Out>> creator) 
  {
    super(pred);
    this.creator = creator;
  }

  @Override
  public int expectedSize() {
    return pred.expectedSize();
  }

  @Override
  public RandomAccessSource<Out> randomAccess() {
    final RandomAccessSource<? extends In> raIn = pred.randomAccess();
    if (raIn == null) {
      return null;
    }

    return new RandomAccessSource<Out>() {
      protected final UnaryFunc<? super In, ? extends Out> transformer = creator.call();
      @Override
      public int size() {
        return raIn.size();
      }

      @Override
      public Out get(int i) {
        return transformer.call(raIn.get(i));
      }};
  }

  @Override
  public Flow pipeInto(final Receiver<? super Out> sink) {
    return pred.pipeInto(new ChainedReceiver<In,Out>(sink) {
      protected final UnaryFunc<? super In, ? extends Out> transformer = creator.call();
      @Override
      public boolean receive(In val) {
        return sink.receive(transformer.call(val));
      }});
  }


  @Override
  public Context createContext() {
    return new Context() {
      protected final UnaryFunc<? super In, ? extends Out> transformer = creator.call();

      @Override
      public Out produce() throws SourceExhausted {
        In elt = source.produce();
        return transformer.call(elt);
      }
    };
  }
 
}
