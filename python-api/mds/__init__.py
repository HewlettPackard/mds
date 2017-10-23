from collections import namedtuple

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

Taxonomy = namedtuple("Taxonomy", ["primitive", "array", "const_primitive", "const_array"])
Bounds = namedtuple("Bounds", ["min", "max"])

class TypeInfo():
    # These bases will be written manually
    MDS_ARRAY_BASE = "MDSArrayBase"
    MDS_ARRAY_INTEGRAL = "MDSIntArrayBase"
    MDS_ARRAY_FLOATING = "MDSFloatArrayBase"
    MDS_CONST_ARRAY_BASE = "MDSConstArrayBase"
    MDS_CONST_ARRAY_INTEGRAL = "MDSConstIntArrayBase"
    MDS_CONST_ARRAY_FLOATING = "MDSConstFloatArrayBase"

    MDS_PRIM_BASE = "MDSPrimitiveBase"
    MDS_PRIM_INTEGRAL = "MDSIntPrimitiveBase"
    MDS_PRIM_FLOATING = "MDSFloatPrimitiveBase"
    MDS_CONST_PRIM_BASE = "MDSConstPrimitiveBase"
    MDS_CONST_PRIM_INTEGRAL = "MDSConstIntPrimitiveBase"
    MDS_CONST_PRIM_FLOATING = "MDSConstFloatPrimitiveBase"

    MDS_BASE, MDS_INTEGRAL, MDS_FLOATING = range(3)

    MDS_TAXONOMY = {
        MDS_BASE: Taxonomy(MDS_PRIM_BASE, MDS_ARRAY_BASE, MDS_CONST_PRIM_BASE, MDS_CONST_ARRAY_BASE),
        MDS_INTEGRAL: Taxonomy(MDS_PRIM_INTEGRAL, MDS_ARRAY_INTEGRAL, MDS_CONST_PRIM_INTEGRAL, MDS_CONST_ARRAY_INTEGRAL),
        MDS_FLOATING: Taxonomy(MDS_PRIM_FLOATING, MDS_ARRAY_FLOATING, MDS_CONST_PRIM_FLOATING, MDS_CONST_ARRAY_FLOATING)
    }

    MDS_INTEGRAL_BOUNDS = dict()

    def __repr__(self):
        return f'<MDS Type: {self.title} ({self.kind})>'

    def __init__(self, api: str, c_type: str, taxonomy: Taxonomy, py_type: type, dtype_extra=None):
        self.api = api
        self.title = api.title()

        if self.title.startswith('U'):
            self.title = api[:2].upper() + api[2:]

        self.title_const = f"Const{self.title}"

        # Python object names
        self.title_array = f"{self.title}Array"
        self.title_array_init = f"{self.title_array}_Init"
        self.title_array_cinit = f"{self.title_array}_Inplace"

        self.title_name_binding = f"MDS{self.title}NameBinding"
        self.f_bind = f"bind_{api}"

        self.title_record_field = f"MDS{self.title}RecordField"
        self.title_record_field_reference = f"MDS{self.title}RecordFieldReference"
        self.title_const_record_field_reference = f"MDS{self.title_const}RecordFieldReference"
        self.title_record_member = f"MDS{self.title}RecordMember"
        self.title_const_record_member = f"MDS{self.title_const}RecordMember"
        
        self.c_type = c_type
        self.py_type_t = py_type
        self.py_type = py_type.__name__
        self.taxonomy = taxonomy
        self.dtype_extra = dtype_extra

        # MDS core aliases (masking templated types)
        self.primitive = f"h_m{api}_t"
        self.array = f"h_array_{api}_t"
        self.managed_value = f"mv_{api}"
        self.managed_array = f"h_marray_{api}_t"
        self.f_managed_type_handle = f"managed_{api}_type_handle"
        self.record_field = f"h_rfield_{api}_t"

        # MDS core aliases (masking const templated types)
        self.const_primitive = f"h_const_m{api}_t"
        self.const_array = f"h_const_array_{api}_t"
        self.const_managed_value = f"mv_const_{api}"
        self.const_managed_array = f"h_const_marray_{api}_t"
        self.f_const_managed_type_handle = f"const_managed_{api}_type_handle"
        self.const_record_field = f"h_const_rfield_{api}_t"

        self.kind = "mds::api::kind::{}".format(api.upper())
        self.f_create_array = f"create_{api}_marray"
        self.f_create_const_array = f"create_const_{api}_marray"
        self.f_bind = f"bind_{api}"
        self.f_bind_array = f"bind_{api}_array"
        self.f_lookup = f"lookup_{api}"
        self.f_lookup_array = f"lookup_{api}_array"
        self.f_to_core_val = f"{api}_to_core_val"

        self.primitive_parent = self.MDS_TAXONOMY[taxonomy].primitive
        self.array_parent = self.MDS_TAXONOMY[taxonomy].array
        self.const_primitive_parent = self.MDS_TAXONOMY[taxonomy].const_primitive
        self.const_array_parent = self.MDS_TAXONOMY[taxonomy].const_array

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
    def is_arithmetic(self):
        return self.taxonomy in [self.MDS_INTEGRAL, self.MDS_FLOATING]


class __MDSTypes(object):
    
    def __init__(self, *args, **kwargs):
        self.__repr = {
            "bool": TypeInfo("bool", "bool", TypeInfo.MDS_BASE, bool, "True"),
            "byte": TypeInfo("byte", "int8_t", TypeInfo.MDS_INTEGRAL, int),
            "ubyte": TypeInfo("ubyte", "uint8_t", TypeInfo.MDS_INTEGRAL, int),
            "short": TypeInfo("short", "int16_t", TypeInfo.MDS_INTEGRAL, int),
            "ushort": TypeInfo("ushort", "uint16_t", TypeInfo.MDS_INTEGRAL, int),
            "int": TypeInfo("int", "int32_t", TypeInfo.MDS_INTEGRAL, int),
            "uint": TypeInfo("uint", "uint32_t", TypeInfo.MDS_INTEGRAL, int),
            "long": TypeInfo("long", "int64_t", TypeInfo.MDS_INTEGRAL, int),
            "ulong": TypeInfo("ulong", "uint64_t", TypeInfo.MDS_INTEGRAL, int),
            "float": TypeInfo("float", "float", TypeInfo.MDS_FLOATING, float),
            "double": TypeInfo("double", "double", TypeInfo.MDS_FLOATING, float)
        }
        self.__available = set(self.__repr.keys())
        super().__init__(*args, **kwargs)

    def __getattr__(self, key):
        if key in self.__repr:
            return self.__repr[key]

        raise AttributeError(f'Unknown type `{key}`')

    def __getitem__(self, key):
        return self.__repr[key]

    @property
    def available(self):
        return self.__available

    @property
    def mappings(self):
        return self.__repr.values()

# Expose to programmers through this binding, mds.typing:
typing = __MDSTypes()
