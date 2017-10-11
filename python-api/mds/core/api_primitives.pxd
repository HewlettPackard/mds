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

from libc.stdint cimport *
from libcpp cimport bool

from mds.core.api_records cimport *


cdef extern from "mds_core_api.h" namespace "mds::api" nogil:
    # TODO Not sure these guys are correct, or are the same as below...
    # NOTE string at least could just go into the injection
    cdef cppclass mv_string "mv_wrapper<mds::api::kind::STRING>":
        pass

    cdef cppclass mv_array "mv_wrapper<mds::api::kind::ARRAY>":
        pass

    cdef cppclass mv_record "mv_wrapper<mds::api::kind::RECORD>":
        pass

# START INJECTION | tmpl_primitive_wrapper

    # BEGIN bool

    cdef cppclass mv_bool "mds::api::api_type<mds::api::kind::BOOL>":
        mv_bool()
        mv_bool(bool)

    cdef cppclass h_mbool_t "mds::api::managed_type_handle<mds::api::kind::BOOL>":
        # TODO:
        # Throws incompatible_type_ex if the field exists but is of the wrong type
        # Throws unmodifiable_record_type_ex if the field doesn't exist, create_if_absent
        # is true, and the record type is fully created.
        h_rfield_bool_t field_in(record_type_handle&, interned_string_handle&, bool) except+

    cdef bool to_core_val "mds::api::to_core_val<mds::api::kind::BOOL>" (const mv_bool&)
    cdef h_mbool_t managed_bool_type_handle "mds::api::managed_type_handle<mds::api::kind::BOOL>"()

    # BEGIN byte

    cdef cppclass mv_byte "mds::api::api_type<mds::api::kind::BYTE>":
        mv_byte()
        mv_byte(int8_t)

    cdef cppclass h_mbyte_t "mds::api::managed_type_handle<mds::api::kind::BYTE>":
        # TODO:
        # Throws incompatible_type_ex if the field exists but is of the wrong type
        # Throws unmodifiable_record_type_ex if the field doesn't exist, create_if_absent
        # is true, and the record type is fully created.
        h_rfield_byte_t field_in(record_type_handle&, interned_string_handle&, bool) except+

    cdef int8_t to_core_val "mds::api::to_core_val<mds::api::kind::BYTE>" (const mv_byte&)
    cdef h_mbyte_t managed_byte_type_handle "mds::api::managed_type_handle<mds::api::kind::BYTE>"()

    # BEGIN ubyte

    cdef cppclass mv_ubyte "mds::api::api_type<mds::api::kind::UBYTE>":
        mv_ubyte()
        mv_ubyte(uint8_t)

    cdef cppclass h_mubyte_t "mds::api::managed_type_handle<mds::api::kind::UBYTE>":
        # TODO:
        # Throws incompatible_type_ex if the field exists but is of the wrong type
        # Throws unmodifiable_record_type_ex if the field doesn't exist, create_if_absent
        # is true, and the record type is fully created.
        h_rfield_ubyte_t field_in(record_type_handle&, interned_string_handle&, bool) except+

    cdef uint8_t to_core_val "mds::api::to_core_val<mds::api::kind::UBYTE>" (const mv_ubyte&)
    cdef h_mubyte_t managed_ubyte_type_handle "mds::api::managed_type_handle<mds::api::kind::UBYTE>"()

    # BEGIN short

    cdef cppclass mv_short "mds::api::api_type<mds::api::kind::SHORT>":
        mv_short()
        mv_short(int16_t)

    cdef cppclass h_mshort_t "mds::api::managed_type_handle<mds::api::kind::SHORT>":
        # TODO:
        # Throws incompatible_type_ex if the field exists but is of the wrong type
        # Throws unmodifiable_record_type_ex if the field doesn't exist, create_if_absent
        # is true, and the record type is fully created.
        h_rfield_short_t field_in(record_type_handle&, interned_string_handle&, bool) except+

    cdef int16_t to_core_val "mds::api::to_core_val<mds::api::kind::SHORT>" (const mv_short&)
    cdef h_mshort_t managed_short_type_handle "mds::api::managed_type_handle<mds::api::kind::SHORT>"()

    # BEGIN ushort

    cdef cppclass mv_ushort "mds::api::api_type<mds::api::kind::USHORT>":
        mv_ushort()
        mv_ushort(uint16_t)

    cdef cppclass h_mushort_t "mds::api::managed_type_handle<mds::api::kind::USHORT>":
        # TODO:
        # Throws incompatible_type_ex if the field exists but is of the wrong type
        # Throws unmodifiable_record_type_ex if the field doesn't exist, create_if_absent
        # is true, and the record type is fully created.
        h_rfield_ushort_t field_in(record_type_handle&, interned_string_handle&, bool) except+

    cdef uint16_t to_core_val "mds::api::to_core_val<mds::api::kind::USHORT>" (const mv_ushort&)
    cdef h_mushort_t managed_ushort_type_handle "mds::api::managed_type_handle<mds::api::kind::USHORT>"()

    # BEGIN int

    cdef cppclass mv_int "mds::api::api_type<mds::api::kind::INT>":
        mv_int()
        mv_int(int32_t)

    cdef cppclass h_mint_t "mds::api::managed_type_handle<mds::api::kind::INT>":
        # TODO:
        # Throws incompatible_type_ex if the field exists but is of the wrong type
        # Throws unmodifiable_record_type_ex if the field doesn't exist, create_if_absent
        # is true, and the record type is fully created.
        h_rfield_int_t field_in(record_type_handle&, interned_string_handle&, bool) except+

    cdef int32_t to_core_val "mds::api::to_core_val<mds::api::kind::INT>" (const mv_int&)
    cdef h_mint_t managed_int_type_handle "mds::api::managed_type_handle<mds::api::kind::INT>"()

    # BEGIN uint

    cdef cppclass mv_uint "mds::api::api_type<mds::api::kind::UINT>":
        mv_uint()
        mv_uint(uint32_t)

    cdef cppclass h_muint_t "mds::api::managed_type_handle<mds::api::kind::UINT>":
        # TODO:
        # Throws incompatible_type_ex if the field exists but is of the wrong type
        # Throws unmodifiable_record_type_ex if the field doesn't exist, create_if_absent
        # is true, and the record type is fully created.
        h_rfield_uint_t field_in(record_type_handle&, interned_string_handle&, bool) except+

    cdef uint32_t to_core_val "mds::api::to_core_val<mds::api::kind::UINT>" (const mv_uint&)
    cdef h_muint_t managed_uint_type_handle "mds::api::managed_type_handle<mds::api::kind::UINT>"()

    # BEGIN long

    cdef cppclass mv_long "mds::api::api_type<mds::api::kind::LONG>":
        mv_long()
        mv_long(int64_t)

    cdef cppclass h_mlong_t "mds::api::managed_type_handle<mds::api::kind::LONG>":
        # TODO:
        # Throws incompatible_type_ex if the field exists but is of the wrong type
        # Throws unmodifiable_record_type_ex if the field doesn't exist, create_if_absent
        # is true, and the record type is fully created.
        h_rfield_long_t field_in(record_type_handle&, interned_string_handle&, bool) except+

    cdef int64_t to_core_val "mds::api::to_core_val<mds::api::kind::LONG>" (const mv_long&)
    cdef h_mlong_t managed_long_type_handle "mds::api::managed_type_handle<mds::api::kind::LONG>"()

    # BEGIN ulong

    cdef cppclass mv_ulong "mds::api::api_type<mds::api::kind::ULONG>":
        mv_ulong()
        mv_ulong(uint64_t)

    cdef cppclass h_mulong_t "mds::api::managed_type_handle<mds::api::kind::ULONG>":
        # TODO:
        # Throws incompatible_type_ex if the field exists but is of the wrong type
        # Throws unmodifiable_record_type_ex if the field doesn't exist, create_if_absent
        # is true, and the record type is fully created.
        h_rfield_ulong_t field_in(record_type_handle&, interned_string_handle&, bool) except+

    cdef uint64_t to_core_val "mds::api::to_core_val<mds::api::kind::ULONG>" (const mv_ulong&)
    cdef h_mulong_t managed_ulong_type_handle "mds::api::managed_type_handle<mds::api::kind::ULONG>"()

    # BEGIN float

    cdef cppclass mv_float "mds::api::api_type<mds::api::kind::FLOAT>":
        mv_float()
        mv_float(float)

    cdef cppclass h_mfloat_t "mds::api::managed_type_handle<mds::api::kind::FLOAT>":
        # TODO:
        # Throws incompatible_type_ex if the field exists but is of the wrong type
        # Throws unmodifiable_record_type_ex if the field doesn't exist, create_if_absent
        # is true, and the record type is fully created.
        h_rfield_float_t field_in(record_type_handle&, interned_string_handle&, bool) except+

    cdef float to_core_val "mds::api::to_core_val<mds::api::kind::FLOAT>" (const mv_float&)
    cdef h_mfloat_t managed_float_type_handle "mds::api::managed_type_handle<mds::api::kind::FLOAT>"()

    # BEGIN double

    cdef cppclass mv_double "mds::api::api_type<mds::api::kind::DOUBLE>":
        mv_double()
        mv_double(double)

    cdef cppclass h_mdouble_t "mds::api::managed_type_handle<mds::api::kind::DOUBLE>":
        # TODO:
        # Throws incompatible_type_ex if the field exists but is of the wrong type
        # Throws unmodifiable_record_type_ex if the field doesn't exist, create_if_absent
        # is true, and the record type is fully created.
        h_rfield_double_t field_in(record_type_handle&, interned_string_handle&, bool) except+

    cdef double to_core_val "mds::api::to_core_val<mds::api::kind::DOUBLE>" (const mv_double&)
    cdef h_mdouble_t managed_double_type_handle "mds::api::managed_type_handle<mds::api::kind::DOUBLE>"()

# END INJECTION

