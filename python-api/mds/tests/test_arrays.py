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
import unittest

from functools import reduce
from random import shuffle

import mds
from mds.managed import *

class TestMDSArrays(unittest.TestCase):

    def setUp(self):
        self.heterogeneous_list = [1, 4.3, True, None, dict()]
        self.unsupported_list = [dict() for x in range(100)]

    def test_can_create_homogeneous_list(self):
        # TODO: Need to test all types in MDSTypes.identifiers
        list_len = 100
        
        for t in mds.typing.mappings:
            x = globals()[t.title_array](list_len)
            #py_t = MDSTypes.python_equivalent(ident)
            #self.assertTrue(t.dtype, MDSTypes[ident])
            #self.assertEqual(len(t), list_len)

            #for elem in x:
            #    self.assertTrue(type(elem), py_t)

    def test_cannot_create_heterogeneous_list(self):
        pass

    def test_cannot_mix_types_in_existing_list(self):
        pass

    def test_default_values(self):
        """

        * Python equivalency respected
        * Bounds respected (what to do for over/underflow?)
        """
        pass

    def test_list_bounds(self):
        pass

    def test_numeric_overflow_behavior(self):
        pass

    def test_numeric_underflow_behavior(self):
        pass

if __name__ == '__main__':
    unittest.main()
