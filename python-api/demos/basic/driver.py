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

import argparse
import sys

from tests import ALL_TESTS

PROD_FILE="products.csv"

parser = argparse.ArgumentParser(description="")
parser.add_argument(
    "-p",
    "--products",
    action="store_const",
    help="The name of the products file",
    default=PROD_FILE
)
parser.add_argument(
    "-t",
    "--test",
    action="store_const",
    type=int,
    default=1,
    help="Which test to run (default: 1)"
)


if __name__ == '__main__':
    args = parser.parse_args()

    # TODO: Pretty sure this isn't scoped correctly
    if args.products:
        PROD_FILE = args.products

    if args.test < len(ALL_TESTS):
        ALL_TESTS[args.test - 1]()
    else:
        print("Unknown test '{}'".format(args.test), file=sys.stderr)