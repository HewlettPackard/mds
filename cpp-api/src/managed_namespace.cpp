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
 * managed_namespace.cpp
 *
 *  Created on: Mar 25, 2015
 *      Author: Evan
 */

#include "managed_namespace.h"

using namespace mds;
using namespace std;

namespace {

}

mds_ptr<mds_namespace>
mds_namespace::
resolve(const path &p, bool include_last) const {
  path::impl_ptr pi = p._ptr;
  auto sp = const_pointer_cast<mds_namespace>(shared_from_this());
  mds_ptr<mds_namespace> iptr = mds_ptr<mds_namespace>::__from_shared(sp);
  if (p.is_absolute()) {
    iptr = root();
  }
  for (size_t i=0; i<pi->initial_ups; i++) {
    if (iptr->is_root()) {
      throw illegal_path_ex{p};
    }
    iptr = iptr->parent();
  }
  const auto &names = pi->names;
  if (names.empty()) {
    // We need a name binding that refers to ns.
    if (include_last) {
      return iptr;
    }
    if (iptr->is_root()) {
      throw illegal_path_ex{p};
    }
    return iptr;
  }
  size_t n = names.size() - include_last ? 0 : 1;
  for (size_t i=0; i<n; i++) {
    name_binding nb{iptr, names[i]};
    iptr = nb.as_namespace();
  }
  return iptr;
}


mds_namespace::name_binding
mds_namespace::
resolve_to_binding(const path &p) const {
  mds_ptr<mds_namespace> ip = resolve(p, false);
  path::impl_ptr pi = p._ptr;
  auto names = pi->names;
  mds_string name = names.empty() ? mds_string{} : names.back();
  // an empty name when creating a binding will return a reference to the
  // namespace, which is what we want.
  return name_binding{ip, name};
}