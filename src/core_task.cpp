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

/*
 * core_context.cpp
 *
 *  Created on: Nov 13, 2014
 *      Author: evank
 */

#include "core/core_task.h"
#include "core/core_context.h"

namespace mds {
  namespace core {
    task::task(gc_token &gc,
               private_ctor,
               const gc_ptr<iso_context> &c,
               const gc_ptr<task> &p)
      : exportable{gc},
        _publishable{c->is_publishable()},
        _context{c},
        _parent{p}
    
      {
        if (p != nullptr) {
          p->_subtasks.push(GC_THIS);
        }
      }

    void task::unconditionally_redo() {
        _context->unconditionally_redo(GC_THIS);
      }

    /*
     * For some reason, if this is inlined, I get an undefined symbol
     * error looking for __atomic_load_16() from JNI code.  Nope.
     * Doesn't work here, either.
    std::size_t task::start_tick() const {
        return _bounds.load().start_time();
      }
     */



  }
}