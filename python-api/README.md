# MDS Python API

Authors: Matt Pugh, Evan Kirshenbaum, Lokesh Gidra, and Susan Spence.

This MDS Python API (PAPI) extension provides the necessary bindings for to develop applications using the functionality of MDS. This includes:

* Types:
  * Primitives
  * Arrays
  * Records
* Type Constraints:
  *  `const`-ness where appropriate
  *  Numeric bounds and sanitization, using NumPy's design as a template.
* Containers:
  * Isolation Contexts
  * Tasks
  * Namespaces

## Requirements

The following are required to use the PAPI:

* Python 3.6 or greater
* GCC
* Linux
* Cython 0.27 or greater

## License

The PAPI follows the licensing of MDS itself, which is detailed in the root of this repository.

## Installation

Navigate to this directory within your terminal of choice, and simply run:

~~~bash
python setup.py build_ext -i
~~~

At the time of writing, only debug inplace builds have been tested.

## Usage

This section describes a number of common interactions with MDS, and how the are to be implemented using the Python API. As a general rule, classes which begin with the prefix `MDS` should not be instantiated by the user, as they are internal-only. It is expected that these will be obfuscated in a future release.

Interactions with MDS are handled through the `mds` module, which has three submodules:

1. `mds.containers` contains objects which help the programmer encapsulate and execute business logic:
   1.  `IsolationContext`  is a hierarchical view of the data held within MDS. A callable object may be passed through, with or without arguments, and executed within the current context, returning the result.
   2.  `Task` is a unit of business logic, the granularity of which is left to the author. Essentially, a `Task` permits the specification of redo policies based on a series of criteria in the event of a conflict given the current `IsolationContext`.
2. `mds.managed` contains a number of objects for utilizing MDS data structures. Generally, they follow the forms below, where `<T>` is a type offered by MDS, and `[Const]` being an optional qualifier:
   1. `[Const]<T>` 
   2. `[Const]<T>Array`
   3. `String` is an immutable string which is instantiated from Python's `str` type.
   4. `Record` is a proxy to a collection of fields on the MDS heap. The PAPI exposes these fields as if they were regular members of the class in question, with some caveats noted below.
   5. `Namespace` provides a hierarchical key-value store to persist MDS objects between processes. You can think of this as a `dict`, where paths can either be a `str` delimited by "/", or the path can be split into components and treated as nested `dict`s. Any object that can be placed into, or retrieved from, a `Namespace`provides two methods, which should be the main way to interface with `Namespace`, as the required type explicitly provided:
      1. `bind_to_namespace(namespace: Namespace, path: PathTypes)`
      2. `from_namespace(namespace: Namespace, path: PathTypes)`, this is a `classmethod`
3. `mds.internal` at present only contains `MemoryStats`, which provides you with a Python interface to the MPCG heap statistics, and is not required for using the `mds` module.

Dealing with types is a really import part of programming with MDS effectively. The underline core, CAPI, and JAPI are all strongly-typed and `const`-aware. The PAPI enforces these constraints, although the ideas behind them are not particularly Pythonic. The reason for is simply that, as data can be shared between processes, there is no guarantee that the recipient, nor originiator of the data fields being used are or were not strongly typed. Due to this, there are a few notes you should be aware of when using the `mds` package.

#### Primitives

The PAPI provides you with the `mds.typing` object, which maps types in a simple way to `TypeInfo` objects used throughout the extension. You should be using this system to define your types. An example of how this is used is provided in **Use a Managed Record** below.

#### Numeric Bounds & Signed Integers

* `OverflowException`
* `UnderflowException`

#### Const Values



`ConstException`

### Create a Managed Data Structure

### Use a Managed Data Structure

### Use a Managed Record

Declaring a `Record` **R** in the PAPI is simple, you need to provide two expected components, and the rest is pure Python logic:

1. A `str` ident **I** declaring the name of the type for MDS (_note_: if the intent is to share **R** amongst the PAPI, CAPI, and JAPI, then the value **I** must be the same across all implementations accessing the same heap.)
2. A static method in your class called `schema` that returns a `dict` mapping field names (as `str`) to field declarations
   1. `Record.declare_field(klass: TypeInfo)`
   2. `Record.declare_const_field(klass: TypeInfo)`

~~~python
import mds
from mds.managed import Record, String, StringArray

class Inventory(Record, ident="InventoryDemo"):

  @staticmethod
  def schema():
    return {
      "is_active": Record.declare_field(mds.typing.bool),
      "dept_name": Record.declare_const_field(String, initial_value="Returns Dept."),
      "product_names": Record.declare_field(StringArray, initial_value=["Laptop", "Phone"]),
      "paid_out": Record.declare_field(mds.typing.float),
      "paid_in": Record.declare_field(mds.typing.double)
    }

# Fields are accessed as you would expect
inv = Inventory()

for product in inv.product_names:
  print(product)
~~~



### Share a Managed Data Structure Safely

##Testing

Each component of the `mds` package has a suite of tests defined in the `mds.tests` module. By default, these are run during the invocation of `setup.py`, however can also be run by navigating to the this directory and running:

~~~shell
python -m unittest discover
~~~

## Development

As this is the first iteration of the PAPI, bugs and oversights are likely to come up. MDS encourages an active development community, and patches are welcome.