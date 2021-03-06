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

/* C++ code implementing native methods of Java class:
 *   com.hpl.mds.impl.ArrayField Proxy
 */

#include <jni.h>
#include "mds_core_api.h"                              // MDS Core API
#include "mds-debug.h"                            // #define dout cout
#include "mds_jni.h"                              // MDS Java API JNI common fns
#include "field_proxy.h"

using namespace mds;
using namespace mds::api;
using namespace mds::jni;
using namespace mds::jni::field_proxy;

extern "C"
{

  JNIEXPORT
  void
  JNICALL
  Java_com_hpl_mds_impl_ArrayFieldProxy_release (JNIEnv *jEnv, jclass,
						 jlong handleIndex)
  {
    exception_handler(jEnv, release<kind::ARRAY>, handleIndex);
  }

  JNIEXPORT
  jlong
  JNICALL
  Java_com_hpl_mds_impl_ArrayFieldProxy_getNameHandle (JNIEnv *jEnv, jclass,
						       jlong hIndex)
  {
    return exception_handler_wr(jEnv, get_name_handle<kind::ARRAY>, hIndex);
  }

  JNIEXPORT
  jlong
  JNICALL
  Java_com_hpl_mds_impl_ArrayFieldProxy_getRecTypeHandle (JNIEnv *jEnv, jclass,
							  jlong hIndex)
  {
    return exception_handler_wr(jEnv, get_rec_type_handle<kind::ARRAY>, hIndex);
  }

}

