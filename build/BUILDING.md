===============================================================================
      Multi Process Garbage Collector
      Copyright © 2016 Hewlett Packard Enterprise Development Company LP.
===============================================================================

# Automated Build Process

This project uses an *automated build process*.  The files `defs.mk`
and `build.mk` in the `build` directory are project-independent and
define this process.  You should never have to modify them.  This
document describes the process in a project-independent way so that it
can be included in all projects that use this process.

Currently, it is assumed that the project builds a single static
library and, optionally, a set of binaries and that all of the source
is in C++.

## Directory Structure.

Assume that the project is named *proj* and the root of its source
directory is `git/proj`.  It is assumed that the following subdirectories exist:

* `src`: Contains source code for the library.  This may contain
  subdirectories, but only a single level.
* `include`: Contains header files needed by users of the library.
  This may contain subdirectories.
* `tools`: Contains subdirectories containing source code for binaries
  (one subdir per binary).  Some or all of these binaries may be
  specified to be exported by `make install`.
* `tests`: Contains subdirectories containing source code for binaries
  used for testing (one subdir per binary).  These binaries will not
  be exported by `make install`.
* `build`: Contains makefiles defining how the project is built.
* `doc` : Will contain generated documentation.

The `build` directory contains *config dir* subdirectories for various
build configurations.  For instance, you may have an `intel-debug`
config dir used to build a debuggable library for an Intel platform
and an `intel-opt` config dir for building an optimized library for
the same platform.  The actual `make` commands are run within one of
the config dirs.  They will, when necessary, build several
subdirectories (`libs`, `objs`, `tools`, `dependencies`) which can
safely be deleted (and will be deleted by running `make clean`).

## Basic Makefile Structure

Besides the provided project-independent makefiles, the project should
provide a project-specific makefile, canonically called `project.mk`,
in the `build` directory.  This should have the following structure:

``` Makefile
include ../defs.mk

# project-specific customization

include ../build.mk

```

The `../` is necessary because the actual `make` command will be run
in a subdirectory.  The customizations will typically involve defining
`make` variables that [will be described below](#customization), but
may also involve adding new targets.

Within each config subdir of `build`, there should be a `Makefile`
that has the following structure:

``` Makefile
# config-specific customization

include ../project.mk
```

For instance, a makefile for an optimized build may say

``` Makefile
optlevel ?= 3
debuglevel ?= 0

include ../project.mk
```

By using `?=`, the makefile allows these definitions to be overridden
on the command line.

## Make Targets

### Default build

The default `make` target is **all**, which builds the project's
(static) library and all of the tools.

### Building object files

* For each source file `<proj>/src/<dir>/foo.cpp`, there is a corresponding
  target for `objects/<dir>/foo.o`.

* For each tool source file `<proj>/tools/<tool>/foo.cpp`, there is a
  corresponding target for `objects/tools/<tool>/foo.o`
  
* For each test source file `<proj>/tests/<test>/foo.cpp`, there is a
  corresponding target for `objects/tests/<test>/foo.o`
  
### Building the library

* There is a target for `$(static_lib)`, which,
  [as described below](#customization), defaults to
  `libs/lib$(project_name).a`.
  
### Building tools and tests

* `make tools` builds all programs in the `tools` subdir.  Each is
  assumed to build an executable with the same name as the subdir of
  `tools.

* `make tests` builds all programs in the `tests` subdir.  Each is
  assumed to build an executable with the same name as the subdir of
  `tests.
  
* For each test or tool *foo*, `make foo` builds just that test or tool.

### Building included projects

* For each included project *p*, `make p_lib` builds the library for *p*.

### Building documentation

* `make private-doc` builds HTML documentation for the project by
  using `Doxygen` to extract comments from the source and header
  files, including the source and header files in used projects.
  
* `make public-doc` builds HTML documentation for the project by using
  `Doxygen` to extract comments only from header files that would be
  installed by `make install`, including header files installed from
  used projects.
  
* `make private-manual` builds a PDF file based on `make private-doc`.

* `make public-manual` builds a PDF file based on `make public-doc`.


### Installing

Install targets copy files to `$(install_dir)`, which is typically the
`install` directory that is a sibling to the project directory.  This
directory has a `bin` subdir for programs, an `include` subdir for
header files, a `lib` subdir for libraries, and a `doc` subdir for
documentation.  It is assumed that this directory may be shared
between projects.

* `make install-libs` installs libraries specified by
  `$(installed_lib_files)`.

* `make install-progs` installs tools specified by `$(installed_progs)`.

* `make install-includes` installs header files.  It does this by
  copying all `.h` files in the include directory and using `rsync` to
  synchronize all subdirs of the include directory specified by
  `$(installed_include_subdirs)` (by default, all of them).
  
* `make install` performs `make install-libs`, `make install-progs`,
  and `make install-includes`
  
* For each tool or test *foo*, `make install-foo` installs the binary for *foo*.

* For each used project *p*, `make install-p` peforms a `make install` in *p*.

* For each used project *p*, `make install-recursive-p` peforms a `make install-recursive` in *p*.

* `make install-recursive` performs `make install` and then performs
  `make-install-recursive-p` for each used project *p*.
  
* `make install-html` performs `make public-doc` and then rsyncs the
  HTML directory to `$(install_dir)/html/$(project_name)`.
  
* `make install-manual` performs `make public-manual` and then copies
  the resulting PDF file to `$(install_dir)/pdf`.

* `make install-doc` performs `make install-html` and `make install-manual`

### Cleaning

* `make clean-objs` removes the `objs` subdir.

* `make clean-lib` removes the `libs` subdir.

* `make clean-dependencies` removes the `dependencies` subdir.

* For each test or tool *foo*, `make clean-foo` removes binaries,
  dependencies, and object files for *foo*.
  
* `make clean-tools` and `make clean-tests` perform `make clean-foo`
  for each tool or test *foo*.
  
* `make clean-tools-binaries` and `make clean-tests-binaries` clear
  the `tools` or `tests` subdir, but do not remove object files.

* `make clean` is a combination of `make clean-objs`, `make
  clean-lib`, `make clean-dependencies`, `make clean-tools`, and `make
  clean-tests`.
  
Note that `make clean` *does not* go into into included projects.

* `make clean-recursive` performs `make clean` and also performs `make
  clean-recursive` in each included project.
  
* For each included project *p*, `make clean-p` performs `make
  clean-recursive` in *p*. (Yeah, this should probably be
  `make-clean-recursive-p`.)
  
* `make clean-doc` and `make clean-manual` undo the effects of `make
  public-doc`/`make private-doc` and `make
  public-manual`/`make-private-manual` respectively.  (The public and
  private variants build the same files, so only one clean command
  form is needed.)
  
* `make clean-installed-libs`, `make clean-installed-includes`, and
  `make clean-installed-progs` remove the files and dirs that would be
  installed by `make install-libs`, `make-installed-includes`, and
  `make installed-progs`.  Note that things installed by previous
  installations that would not now be installed will not be removed.
  
* `make clean-install` performs `make clean-installed-libs`, `make
  clean-installed-includes`, and `make clean-installed-progs`.
  
* `make clean-installed-doc`, `make clean-installed-html`, and `make
  clean-installed-manual` remove the files and dirs that would
  beinstalled by `make installed-doc`, `make installed-html`, and
  `make installed-manual`.

## Customization

There is a large number of variables that can be defined either in the
config `Makefile`, in `project.mk`, or on the command line or in
environment variables.  All of these have default values.

### Variables defined in `defs.mk`

The following variables are defined in defs.mk.  These may be of use
for definitions in `project.mk`.

* `$(build_dir)` is the directory containing the included defs.mk.
This should be `<proj>/build`.

* `$(project_dir)` is the parent of `$(build_dir)`.

* `$(project_name)` is the name of the project.  *Defaults to the name
  of the project directory.*

* `$(config)` is the name of the directory make was run in.

* `$(git_base_dir)` is the closest ancestor of `$(project_dir)` that
  doesn't itself look like a project directory, where "looks like a
  project directory" is currently "has a build dir in it".  This
  allows projects to be kept within other projects.
  
### Variables defined in `build.mk`

The following variables are used in `build.mk` but may be overridden.
All have default values.  For any such variable `$(var)` in a project
*proj*, if `$(proj_var)` is defined, its value is used.  Otherwise, if
`$(var)` is defined, its value is used.  Otherwise the default value
is used. 

* `$(ignore_src_dirs)` is a list of subdirectories of <proj>/src that
   should not be included in the overall library.  *Defaults to
   `unused` and `obsolete`.*

* `$(lib_name)` is the name of the library (suitable for the `-l`
  prefix).  *Defaults to `$(project_name)`.*

* `$(projects_used)` is a list of projects that this project depends
  on.  *Defaults to the empty list.* All sources will be built with
  that project's include directory on their search path.  They will be
  asked to install on `make install`.  Their libraries will be used
  when building tools.
  
#### Installation variables

* `$(install_dir)` is the base directory for *install* targets
  (`install`, `install-includes`, etc.)  *Defaults to
  `$(git_base_dir)/install`.*

* `$(install_includes_dir)` is the dir for installing include files.
  *Defaults to `$(install_dir)/include`.*

* `$(install_lib_dir)` is the dir for installing libraries.  *Defaults to
  `$(install_dir)/lib`.*

* `$(install_bin_dir)` is the dir for installing programs.  *Defaults to
  `$(install_dir)/bin`.*
  
* `$(install_doc_dir)` is the dir for installing documentation.
  *Defaults to `$(install_dir)/doc`.*

* `$(install_html_dir)` is the dir for installing HTML documentation.
  *Defaults to `$(install_doc_dir)/html`.*
  
* `$(install_pdf_dir)` is the dir for installing PDF documentation.
  *Defaults to `$(install_doc_dir)/pdf`.*
    

* `$(installed_include_subdirs)` is a list of subdirectories of
  `$(incl_dir)` that should be installed in `$(install_includes_dir)`.
  *Defaults to all of them.* This allows a project to have some
  headers that are only used internally.  Note that all top-level `.h`
  files will be installed.  These names are relative to `$(incl_dir)`.

* `$(installed_libs)` is a list of libraries to install.  *Defaults to
  `lib$(lib_name).a`.*

* `$(installed_progs)` is a list of programs to install from the `tools`
  dir.  *Defaults to the empty list.*
  
* `$(installed_manual_name)` is the name of the PDF manual.  *Defaults
  to `$(project_name)-api.pdf`.*
  
* `$(installed_manual)` is the PDF manual file, with path.  *Defaults
  to `$(instal_pdf_dir)/$(installed_manual_name)`.*

* `$(installed_html)` is the project-specific HTML directory.
  *Defaults to `$(instal_html_dir)/$(project_name)`.
  
  
#### Compilation variables

* `$(incl_dirs)` is a list of include directories used in
  compilation. *Defaults to `$(incl_dir)` and `$(P_include_dirs)` for
  each P in `$(projects_used)`.*

* `$(cpp_includes)` is a list of `-I` options to pass to the cmopiler.
  *Defaults to a `-I` for each element of `$(incl_dirs)`.*

* `$(cpp_std)` is the C++ standard used.  *Defaults to `c++14`.*  If
  this is defined, `$(std_flag)` will be defined as
  `-std=$(cpp_std)`.

* `$(cpp_defines)` is a list of `-D` and `-U` options to pass to the
  compilation.  *Defaults to the empty list.*

* `$(cpp_dep_flags)` is a list of options used for creating dependency
  files during compilation.  *Defaults to `-MMD`, `-MP`, and `-MF(call
  obj_to_dep,$@)`.*  This last puts the dependency file in the
  `dependencies` subdirectory.

* `$(extra_cpp_flags)` is a list of extra preprocessor-related
  arguments to pass to the compilation.  *Defaults to the empty list.*

* `$(CPPFLAGS)` is the list of preprocessor-related arguments passed
  to the compilation.  *Defaults to a concatenation of `$(std_flag)`,
  `$(cpp_defines)`, `$(cpp_includes)`, `$(cpp_dep_flags)`, and
  `$(extra_cpp_flags)`.*

* `$(optlevel)` is the optimization level used for compilation.
  *Defaults to `g`.*

* `$(opt_flag)` is the optimization flag (or flags) used for
  compilation.  *Defaults to `-O$(optlevel)`.*

* `$(debuglevel)` is the debug level used for compilation.  *Defaults
  to `gdb3`.*

* `$(debug_flag)` is the debug flag (or flags) used for compilation.
  *Defaults to `-g$(debuglevel)`.*

* `$(extra_cxx_flags)` is a list of extra compilation flags to use.
  *Defaults to `-Wall`, `-fmessage-length=0`, `-pthread`, and `-fPIC`.*

* `$(CXXFLAGS)` is the list of compilation-related arguments passed to
  the compilation.  *Defaults to a concatenation of `$(opt_flag)`,
  `$(debug_flag)`, and `$(extra_cxx_flags)`.*

* `$(LDFLAGS)` is the list of arguments to use when linking programs.
  *Defaults to the empty list.*

* `$(LIBS)` is the list of library arguments to use when linking
  programs.  *Defaults to `-lstdc++ -lpthread`.*
  
#### Tool-specific variables

When building a program in the `tools` or `tests` directory,
tool-specific variables can be specified to take preference.  For
example, if building a tool called *frob* in project called
*wonderlib*, `$(frob_cpp_defines)` will be used in preference to
`$(wonderlib_cpp_defines)` or `$(cpp_defines)`.  When variables have
defaults based on other variables, the priority is preserved.  For
example, `$(frob_op_flag)` will default based on `$(frob_optlevel)`.
The program-related variables that can be specified are:

    `$(cpp_std)`, `$(cpp_defines)`, `$(cpp_includes)`, `$(cpp_dep_flags)`,
    `$(extra_cpp_flags)`, `$(cpp_optlevel)`, `$(cpp_opt_flag)`,
    `$(cpp_debuglevel)`, `$(cpp_debug_flag)`, `$(extra_cxx_flags)`,
    `$(CPPFLAGS)`, `$(CXXFLAGS)`, `$(LDFLAGS)`, and `$(LIBS)`.

In addition, to additional variables can specify deltas from the
defaults used when building the library.  For a program *frob*:

* `$(frob_cpp_extra_includes)` is a list of extra `-I` arguments added
  before `$(cpp_includes)` when computing the default for
  `$(frob_cpp_includes)`.  *Defaults to the empty list.*

* `$(frob_cpp_extra_libs)` is a list of extra library arguments added
  after `$(LIBS)` when computing the default for `$(frob_LIBS)`.
  *Defaults to the empty list.*

#### Used-project-specific variables

For each project P in `$(projects_used)`, several variables are
consulted:

* `$(P_config)`: The configuration of P to use.  *Defaults to
  `$(config)`.*

* `$(P_project_dir)`: The location of the project directory for P.
  *Defaults to a child named P of either `$(project_dir)` or
  `$(git_base_dir)`.* The first is taken if it exists and has a build
  subdir.

* `$(P_build_dir)`: The directory to run make in for P.  *Defaults to
  `$(P_project_dir)/build/$(P_config)`.*

* `$(P_include_dirs)`: A list of directories to include for P.
  *Defaults to `$(P_project_dir)/include`.*

* `$(P_lib_name)`: The name of the P library to link against.
  *Defaults to `libP.a`*

* `$(P_lib_target)`: The path to the P library in the P project.
  *Defaults to `libs/$(P_lib_name)`.*

* `$(P_lib)`: The path to the P library.  *Defaults to
  `$(P_build_dir)/$(P_lib_target)`.*

#### Documentation variables

* `$(doc_dir)` is the directory for generated documentation.  *Defaults
  to `$(project_dir)/doc`.*

* `$(manual)` is the generated PDF manual.  *Defaults to
  `$(doc_dir)/$(project_name)-api.pdf`.*
  
* `$(doxygen_private_config)` is the Doxygen config file to generate
  for private documentation.  *Defaults to `private-doxygen.config`.*
  
* `$(doxygen_public_config)` is the Doxygen config file to generate
  for public documentation.  *Defaults to `public-doxygen.config`.*
  
* `$(DOXYGEN)` is the command to use to run Doxygen.  *Defaults to
  `doxygen`.*
  
* `$(doxygen_args)` is a multi-line string containing extra lines to
  put in the doxygen config file.  *Defaults to the empty string.*

All generated Doxygen config files begin with `@INCLUDE` lines for the
corresponding config files in all used projects.  They then contain

```
RECURSIVE = YES
JAVADOC_AUTOBRIEF = YES
$(doxygen_args)
OUTPUT_DIRECTORY = $(doc_dir)
```

Next comes `EXTRACT_PRIVATE = YES` or `EXTRACT_PRIVATE = NO`,
according to which config file is being generated.  Finally, `INPUT`
and `EXCLUDE` lines are used to include the appropriate source and
header files.

A useful minimal `$(doxygen_args)` might include

``` Makefile
define doxygen_args
PROJECT_NAME = WTL
PROJECT_BRIEF = "The Wonder Tool Library"
endef

```

