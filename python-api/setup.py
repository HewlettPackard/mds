'''
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
'''

import os
import sys

from distutils.core import setup
from distutils.extension import Extension
from unittest import TestLoader, TestResult

import inject_wrappers as generate 

try:
    from Cython.Build import cythonize
    from Cython.Distutils import build_ext
except ImportError:
    print('You need Cython to compile the MDS Python API.', file=sys.stderr)
    sys.exit(1)

# TODO: Test Python version

DEBUG = True
RUN_TESTS = False
DIR_REPO = '/home/pughma/repo/'
HEADER_DIRS = [
    DIR_REPO + 'public/install/include',
    DIR_REPO + 'mds-public/python-api/mds/include'
]
ARGS = ['-std=c++14', '-MMD', '-MP', '-fmessage-length=0', '-mcx16']
LIB_DIR = DIR_REPO + 'public/install/lib'
LIBS = [f'{LIB_DIR}/lib{l}.a' for l in ['mds_core', 'mpgc', 'ruts']]
UNDEF_MACROS = []
MDS_PACKAGES = ['managed', 'containers', 'internal']

if DEBUG:
    ARGS.extend(['-Og', '-ggdb3'])
    UNDEF_MACROS.append('NDEBUG')

def check_and_build_mds_core():
    # TODO: Check the current build (if avail) and rebuild if necessary
    pass

def make_extension(package_identifier):
    local_path = package_identifier.replace('.', os.sep)

    return Extension(package_identifier,
        sources=[f'{local_path}.pyx'],
        include_dirs=HEADER_DIRS,
        language='c++',
        extra_compile_args=ARGS,
        extra_objects=LIBS,
        undef_macros=UNDEF_MACROS
    )

# Make sure everything's available and not stale
check_and_build_mds_core()

# Make sure code generation is up-to-date
generate.generate_and_inject_all_sources(dry_run=False)

# Setup our package hierarchy
extensions = [make_extension(f'mds.{e}') for e in MDS_PACKAGES]# + [cythonize(f) for f in SHARED_FILES]

setup(
    name='mds',
    packages=['mds'],
    ext_modules=cythonize(
        extensions,
        language_level=3,
    )
)

if RUN_TESTS:
    result = TestResult()
    TESTS = TestLoader().discover('mds')
    TESTS.run(result)

    print(result)

