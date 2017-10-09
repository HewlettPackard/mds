# -*- coding: utf-8 -*-
"""
Managed Data Structures
Copyright © 2017 Hewlett Packard Enterprise Development Company LP.

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public License
along with this program.  If not, see <http://www.gnu.org/licenses/>.

As an exception, the copyright holders of this Library grant you permission
to (i) compile an Application with the Library, and (ii) distribute the
Application containing code generated by the Library and added to the
Application during this compilation process under terms of your choice,
provided you also meet the terms and conditions of the Application license.
"""

import sys

from collections import namedtuple

InjectionInfo = namedtuple("InjectionInfo", ["file_path", "generator_fn"])
SegmentedFile = namedtuple("SegmentedFile", ["pre", "existing", "post"])
Taxonomy = namedtuple("Taxonomy", ["primitive", "array"])
Bounds = namedtuple("Bounds", ["min", "max"])

"""
MDSArrayBase*
    `- BoolArray
    `- IntArrayBase*
        `- ByteArray
        `- UByteArray
        `- ShortArray
        `- UShortArray
        `- IntArray
        `- UIntArray
        `- LongArray
        `- ULongArray
    `- FloatArrayBase*
        `- FloatArray
        `- DoubleArray

* Should not be instantiable, contains generic methods only.
"""

def tmpl_record_field_wrapper_math(t):
    return f"""
        {t.c_type} add(const managed_record_handle&, {t.c_type})
        {t.c_type} sub(const managed_record_handle&, {t.c_type})
        {t.c_type} mul(const managed_record_handle&, {t.c_type})
        {t.c_type} div(const managed_record_handle&, {t.c_type})
"""

#TODO: Need record_fields for these:
#      STRING,
#      RECORD,
#      BINDING,
#      ARRAY,
#      NAMESPACE,

def tmpl_record_field_wrapper(t):
    EXTRA = tmpl_record_field_wrapper_math(t) if t.use_atomic_math else ""
    compiled = ""

    for prefix in ("", "const_"):
        wrapper_name = t.record_field if prefix == "" else t.const_record_field
        compiled += f"""
    # BEGIN {t.api}

    cdef cppclass {wrapper_name} "mds::api::{prefix}record_field_handle<{t.kind}>":
        {wrapper_name}()
        {wrapper_name}({wrapper_name}&)
        {t.c_type} free_read(const managed_record_handle&)
        {t.c_type} frozen_read(const managed_record_handle&)
        bool has_value(const managed_record_handle&)
        bool write_initial(const managed_record_handle&,const {t.c_type}&)
        {t.c_type} write(const managed_record_handle&, const {t.c_type}&)
        interned_string_handle name()
        {EXTRA}
        const_record_type_handle rec_type()
        #const_type_handle_for<K> field_type()
"""
    return compiled
 
def tmpl_primitive_wrapper(t):
    return f"""
    # BEGIN {t.api}

    cdef cppclass {t.managed_value} "mds::api::api_type<{t.kind}>":
        {t.managed_value}()
        {t.managed_value}({t.c_type})

    cdef cppclass {t.primitive} "mds::api::managed_type_handle<{t.kind}>":
        # TODO:
        # Throws incompatible_type_ex if the field exists but is of the wrong type
        # Throws unmodifiable_record_type_ex if the field doesn't exist, create_if_absent
        # is true, and the record type is fully created.
        {t.record_field} field_in(record_type_handle&, interned_string_handle&, bool) except+

    cdef {t.c_type} to_core_val "mds::api::to_core_val<{t.kind}>" (const {t.managed_value}&)
    cdef {t.primitive} {t.managed_type_handle} "mds::api::managed_type_handle<{t.kind}>"()
"""

def tmpl_namespace_wrapper(t):
    return f"""
        {t.c_type} lookup "lookup<{t.kind},mds::core::kind_type<{t.kind}>,false,true>"(interned_string_handle, const {t.primitive}&)
        {t.managed_array} lookup "lookup<{t.kind},false,true>"(const interned_string_handle&, const {t.array}&)
        bool bind "bind<{t.kind}>"(interned_string_handle, {t.c_type})
    """

def tmpl_array_wrapper_math(t):
    return f"""
        {t.c_type} add(const size_t&, const {t.c_type}&)
        {t.c_type} sub(const size_t&, const {t.c_type}&)
        {t.c_type} mul(const size_t&, const {t.c_type}&)
        {t.c_type} div(const size_t&, const {t.c_type}&)
"""

def tmpl_array_wrapper(t):
    EXTRA = tmpl_array_wrapper_math(t) if t.use_atomic_math else ""

    return f"""
    cdef cppclass {t.array} "mds::api::array_type_handle<{t.kind}>":
        # const_managed_type_handle<K> element_type()
        {t.managed_array} create_array(size_t)
        bool is_same_as(const {t.array}&)

    cdef cppclass {t.managed_array}:
        {t.managed_value} frozen_read(size_t)
        {t.managed_value} write(size_t, {t.managed_value})
        size_t size()
        # TODO uniform_key uuid()
        bool has_value()
        h_marray_base_t as_base()
        {EXTRA}
    {t.managed_array} {t.f_create_array}(size_t)
    """

def tmpl_int_primitive_bounds(t):
    return f"""
    property MIN:
        def __get__(self):
            return {t.bounds.min}

    property MAX:
        def __get__(self):
            return {t.bounds.max} 
"""

def tmpl_record_member(t):
    s = f"""
cdef class {t.title_record_member}(RecordMemberBase):

    cdef {t.record_field} _handle
 
    def __cinit__(self, type_ident, initial_value):
        self._type_ident = type_ident
        self._initial_value = initial_value            

    def read(self):
        return self._handle.frozen_read()

    def write(self, value):
        self._handle.write(value)

    cpdef declare(self, str ident, record_type_handle rt):
        assert(self._handle.is_null())
        self._handle = {t.managed_type_handle}().field_in(rt, ident, True)
        self.write_initial(self._initial_value)

    cpdef ensure_type(self):
        # managed_type<T>::ensure_complete()
        pass

cdef class {t.title_const_record_member}(ConstRecordMemberBase):

    cdef {t.const_record_field} _handle
 
    def __cinit__(self, type_ident, initial_value):
        self._type_ident = type_ident
        self._initial_value = initial_value            

    def read(self):
        return self._handle.frozen_read()

    cpdef declare(self, str ident, record_type_handle rt):
        assert(self._handle.is_null())
        self._handle = {t.managed_type_handle}().field_in(rt, ident, True)
        self.write_initial(self._initial_value)

    cpdef ensure_type(self):
        # managed_type<T>::ensure_complete()
        pass
"""

def tmpl_concrete_array(t):
    primitive_extra = ""

    if t.taxonomy == TypeInfo.MDS_INTEGRAL:
        primitive_extra = tmpl_int_primitive_bounds(t)

    s = f"""
cdef class {t.title}({t.primitive_parent}):

    cdef {t.managed_value} _value

    def __cinit__(self, value):  # TODO: Set the value in _value
        value = self._sanitize(value)

    def _to_python(self):
        return to_core_val(self._value)

    def _to_mds(self):  # TODO: This needs to update _value
        pass
    {primitive_extra}

cdef class {t.title_array}({t.array_parent}):

    cdef {t.managed_array} _handle
    _primitive = {t.title}

    def __cinit__(self, length=None):
        if length is not None:
            self._handle = {t.f_create_array}(length)
        else:  # TODO: Not sure this is the best, but will avoid segfaults
            self._handle = {t.f_create_array}(0)

    def __len__(self):
        return self._handle.size()

    def _to_python(self, index):
        return to_core_val(self._handle.frozen_read(index))

    def _to_mds(self, index, value):
        # Delegate bounds checking etc. to the primitive wrapper
        wrapped = self._primitive(value)
        self._handle.write(index, {t.managed_value}(value))
    
    def copy(self):
        cdef:
            size_t i = 0
            size_t l = len(self)
            {t.managed_array} h = {t.f_create_array}(l)

        for i in range(l):
            h.write(i, {t.managed_value}(<{t.c_type}> self[i]))

        return {t.title_array_init}(h)

    @staticmethod
    def create(length):
        return {t.title_array_init}({t.f_create_array}(length))
"""

    # Sometimes we need to be creative to coerce the correct Python type
    if t.python_type is not None:
        s += f"""
    property dtype:
        def __get__(self):
            return type({t.python_type})
"""

    s += f"""\n
cdef {t.title_array_init}({t.managed_array} handle):
    result = {t.title_array}()
    result._handle = handle
    return result\n
"""
    return s

class TypeInfo():
    # These bases will be written manually
    MDS_BASE_ARRAY = "MDSArrayBase"
    MDS_INTEGRAL_ARRAY = "MDSIntArrayBase"
    MDS_FLOATING_ARRAY = "MDSFloatArrayBase"

    MDS_BASE_PRIMITIVE = "MDSPrimitiveBase"
    MDS_INTEGRAL_PRIMITIVE = "MDSIntPrimitiveBase"
    MDS_FLOATING_PRIMITIVE = "MDSFloatPrimitiveBase"

    MDS_BASE = 1
    MDS_INTEGRAL = 2
    MDS_FLOATING = 3

    MDS_TAXONOMY = {
        MDS_BASE: Taxonomy(MDS_BASE_PRIMITIVE, MDS_BASE_ARRAY),
        MDS_INTEGRAL: Taxonomy(MDS_INTEGRAL_PRIMITIVE, MDS_INTEGRAL_ARRAY),
        MDS_FLOATING: Taxonomy(MDS_FLOATING_PRIMITIVE, MDS_FLOATING_ARRAY)
    }

    MDS_INTEGRAL_BOUNDS = dict()

    def __init__(self, api, c_type, taxonomy, python_type=None):
        self.api = api
        self.title = api.title()

        if self.title.startswith('U'):
            self.title = api[:2].upper() + api[2:]

        self.title_array = f"{self.title}Array"
        self.title_array_init = f"{self.title_array}_Init"
        self.title_record_member = f"{self.title}RecordMember"
        self.title_const_record_member = f"Const{self.title_record_member}"
        self.c_type = c_type
        self.taxonomy = taxonomy
        self.python_type = python_type
        self.primitive = f"h_m{api}_t"
        self.array = f"h_array_{api}_t"
        self.managed_value = f"mv_{api}"
        self.managed_array = f"h_marray_{api}_t"
        self.managed_type_handle = f"managed_{api}_type_handle"
        self.const_record_field = f"h_const_rfield_{api}_t"
        self.record_field = f"h_rfield_{api}_t"
        self.kind = "mds::api::kind::{}".format(api.upper())
        self.f_create_array = f"create_{api}_marray"
        self.f_bind = f"bind_{api}"

        self.primitive_parent = self.MDS_TAXONOMY[taxonomy].primitive
        self.array_parent = self.MDS_TAXONOMY[taxonomy].array

        if not TypeInfo.MDS_INTEGRAL_BOUNDS:
            for p in (8, 16, 32, 64):
                TypeInfo.MDS_INTEGRAL_BOUNDS[f"int{p}_t"] = Bounds(-(2 ** (p - 1)), (2 ** (p - 1)) - 1)
                TypeInfo.MDS_INTEGRAL_BOUNDS[f"uint{p}_t"] = Bounds(0, (2 ** p) - 1)

        if self.taxonomy == self.MDS_INTEGRAL:
            self.bounds = self.MDS_INTEGRAL_BOUNDS[c_type]

    @property
    def is_integral(self):
        return self.taxonomy == self.MDS_INTEGRAL

    @property
    def use_atomic_math(self):
        return self.taxonomy in [self.MDS_INTEGRAL, self.MDS_FLOATING]


# These types are the ones that this script will generate wrappers for
TYPE_DETAILS = [
    TypeInfo("bool", "bool", TypeInfo.MDS_BASE, "True"),
    TypeInfo("byte", "int8_t", TypeInfo.MDS_INTEGRAL),
    TypeInfo("ubyte", "uint8_t", TypeInfo.MDS_INTEGRAL),
    TypeInfo("short", "int16_t", TypeInfo.MDS_INTEGRAL),
    TypeInfo("ushort", "uint16_t", TypeInfo.MDS_INTEGRAL),
    TypeInfo("int", "int32_t", TypeInfo.MDS_INTEGRAL),
    TypeInfo("uint", "uint32_t", TypeInfo.MDS_INTEGRAL),
    TypeInfo("long", "int64_t", TypeInfo.MDS_INTEGRAL),
    TypeInfo("ulong", "uint64_t", TypeInfo.MDS_INTEGRAL),
    TypeInfo("float", "float", TypeInfo.MDS_FLOATING),
    TypeInfo("double", "double", TypeInfo.MDS_FLOATING)
]

def generate_specializations(generator_fn):
    output = []

    for t in TYPE_DETAILS:
        output.append(generator_fn(t))

    return output + ['\n']

def _ensure_is_list(thing):
    if not isinstance(thing, list):
        thing = [thing]

    return thing

def find_and_truncate_injected(file_path):
    with open(file_path, "r") as fp:
        lines = [l for l in fp]

    inj_start = None
    inj_end = None

    for i, line in enumerate(lines):
        if "START INJECTION" in line:
            inj_start = i + 1
        elif "END INJECTION" in line:
            inj_end = i
            break

    assert(inj_start is not None and inj_end is not None)

    return SegmentedFile(
        _ensure_is_list(lines[:inj_start]),
        _ensure_is_list(lines[inj_start:inj_end]),
        _ensure_is_list(lines[inj_end:])
    )
 
def do_injection(inject_info, dry_run=False):
    segmented = find_and_truncate_injected(inject_info.file_path)
    lines = segmented.pre
    lines.extend(generate_specializations(inject_info.generator_fn))
    lines.extend(segmented.post)

    if dry_run:
        chunks = [
            ("Previously Generated Code", segmented.existing),        
            ("New File Output", lines)
        ]
        
        for ident, body in chunks:
            ident += f" <{inject_info.file_path}>"
            separator = "=" * (len(ident) + 4)

            print(f"\n{separator}\n| {ident} |\n{separator}\n")

            for line in body:
                print(line)
    else:
        with open(inject_info.file_path, "w") as fp:
            fp.writelines(lines)

TARGETS = [
    InjectionInfo("mds/core/api_primitives.pxd", tmpl_primitive_wrapper),
    InjectionInfo("mds/core/api_arrays.pxd", tmpl_array_wrapper),
    InjectionInfo("mds/core/api_namespaces.pxd", tmpl_namespace_wrapper),
    InjectionInfo("mds/core/api_records.pxd", tmpl_record_field_wrapper),
    InjectionInfo("mds/managed.pyx", tmpl_concrete_array)
]

def generate_and_inject_all_sources(dry_run=True):
    for t in TARGETS:
        do_injection(inject_info=t, dry_run=dry_run)

if __name__ == '__main__':
    for arg in sys.argv:
        if 'dry' in arg:
            generate_and_inject_all_sources(dry_run=True)
            break
    else:
        generate_and_inject_all_sources(dry_run=False)

