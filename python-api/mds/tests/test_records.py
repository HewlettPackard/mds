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

import unittest

import mds
from mds.managed import Record

class SimpleRecord(Record):

    _ident = "schema_SimpleRecords"

    def __init__(self):
        super().__init__()
        self._register_field(mds.typing.bool, "is_active", True)
        self._create()

class LessSimpleRecord(Record):

    _ident = "schema_LessSimpleRecord"

    def __init__(self):
        super().__init__()
        self._register_field(mds.typing.bool, "is_active", True)
        self._register_field(mds.typing.float, "numerator")
        self._register_field(mds.typing.double, "denominator")
        self._create()

class ComplexRecord(Record):

    _ident = "schema_ComplexRecord"

    def __init__(self):
        super().__init__()
        self._register_field(mds.typing.bool, "is_active", True)
        self._register_field(mds.typing.float, "numerator")
        self._register_field(mds.typing.double, "denominator")
        self._create()

class NoIdentRecord(Record):

    def __init__(self):
        super().__init__()
        self._register_field(mds.typing.bool, "is_active", True)
        self._create()

class TestRecords(unittest.TestCase):

    RECORDS = [SimpleRecord, LessSimpleRecord, ComplexRecord]

    def test_cannot_make_without_ident(self):
        self.assertRaises(TypeError, lambda: NoIdentRecord())

    def __create_and_test(self, cls):
        record = cls()
        self.assertIs(type(record), cls)
        self.assertIsInstance(record, Record)
        self.assertEqual(cls._ident, record.ident)
        return record

    def test_can_make_simple(self):
        record = self.__create_and_test(SimpleRecord)
        field = "is_active"

        self.assertIn(field, record.type_declaration.fields())
        self.assertEqual(record[field], True)

    @unittest.skip("Debugging")
    def test_can_make_less_simple(self):
        self.__create_and_test(LessSimpleRecord)

    @unittest.skip("Debugging")
    def test_can_make_complex(self):
        self.__create_and_test(ComplexRecord)

    def test_can_bind_to_namespace(self):
        pass

    def test_can_retrieve_from_namespace(self):
        pass

    def test_forwarding_resolution(self):
        pass


if __name__ == '__main__':
    unittest.main()
