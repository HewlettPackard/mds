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

from libcpp cimport bool
from libc.stdint cimport uint64_t

from mds.core.api_isolation_contexts cimport iso_context_handle

cdef extern from "helpers.h" namespace "mds::python::tasks":
    cdef cppclass TaskWrapper:
        TaskWrapper() nogil except +

        void run(void(*)(_py_callable_wrapper), _py_callable_wrapper) except+ 

        @staticmethod
        task_handle get_current()
        void set_current(task_handle)

    void initialize_base_task() nogil

cdef extern from "helpers.h" namespace "mds::python::tasks::TaskWrapper" nogil:
    cdef cppclass Establish:
        Establish()
        Establish(const task_handle&) except +

cdef extern from "mds_core_api.h" namespace "mds::api" nogil:
    cdef cppclass task_handle:
        task_handle()
        
        iso_context_handle get_context()
        task_handle get_parent()
        task_handle push()

        @staticmethod
        task_handle pop()

        @staticmethod
        task_handle default_task()

        @staticmethod
        task_handle push_new()

        void add_dependent(const task_handle&)
        void always_redo()
        void cannot_redo()

        bool is_null()

        uint64_t hash1()
        uint64_t hash2()

