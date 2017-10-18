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

Taxonomy = namedtuple("Taxonomy", ["primitive", "array"])
Bounds = namedtuple("Bounds", ["min", "max"])

class TypeInfo():
    # These bases will be written manually
    MDS_BASE_ARRAY = "MDSArrayBase"
    MDS_INTEGRAL_ARRAY = "MDSIntArrayBase"
    MDS_FLOATING_ARRAY = "MDSFloatArrayBase"

    MDS_BASE_PRIMITIVE = "MDSPrimitiveBase"
    MDS_INTEGRAL_PRIMITIVE = "MDSIntPrimitiveBase"
    MDS_FLOATING_PRIMITIVE = "MDSFloatPrimitiveBase"

    MDS_BASE, MDS_INTEGRAL, MDS_FLOATING = range(3)

    MDS_TAXONOMY = {
        MDS_BASE: Taxonomy(MDS_BASE_PRIMITIVE, MDS_BASE_ARRAY),
        MDS_INTEGRAL: Taxonomy(MDS_INTEGRAL_PRIMITIVE, MDS_INTEGRAL_ARRAY),
        MDS_FLOATING: Taxonomy(MDS_FLOATING_PRIMITIVE, MDS_FLOATING_ARRAY)
    }

    MDS_INTEGRAL_BOUNDS = dict()

    def __repr__(self):
        return f'<MDS TypeInfo: {self.title} ({self.kind})>'

    def __init__(self, api, c_type, taxonomy, python_type=None):
        self.api = api
        self.title = api.title()

        if self.title.startswith('U'):
            self.title = api[:2].upper() + api[2:]

        # Python object names
        self.title_array = f"{self.title}Array"
        self.title_array_init = f"{self.title_array}_Init"
        self.title_array_cinit = f"{self.title_array}_Inplace"

        self.title_record_field = f"{self.title}RecordField"
        self.title_record_member = f"{self.title}RecordMember"
        self.title_const_record_field = f"Const{self.title_record_field}"
        self.title_const_record_member = f"Const{self.title_record_member}"
        
        self.c_type = c_type
        self.taxonomy = taxonomy
        self.python_type = python_type

        # MDS core aliases (masking templated types)
        self.primitive = f"h_m{api}_t"
        self.array = f"h_array_{api}_t"
        self.managed_value = f"mv_{api}"
        self.managed_array = f"h_marray_{api}_t"
        self.managed_type_handle = f"managed_{api}_type_handle"
        self.record_field = f"h_rfield_{api}_t"

        # MDS core aliases (masking const templated types)
        self.const_primitive = f"h_const_m{api}_t"
        self.const_array = f"h_const_array_{api}_t"
        self.const_managed_value = f"mv_const_{api}"
        self.const_managed_array = f"h_const_marray_{api}_t"
        self.const_managed_type_handle = f"const_managed_{api}_type_handle"
        self.const_record_field = f"h_const_rfield_{api}_t"

        self.kind = "mds::api::kind::{}".format(api.upper())
        self.f_create_array = f"create_{api}_marray"
        self.f_create_const_array = f"create_const_{api}_marray"
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
# TODO Move to mds.__init__ ?

class __MDSTypes(object):
    
    def __init__(self, *args, **kwargs):
        self.__repr = {
            "bool": TypeInfo("bool", "bool", TypeInfo.MDS_BASE, "True"),
            "byte": TypeInfo("byte", "int8_t", TypeInfo.MDS_INTEGRAL),
            "ubyte": TypeInfo("ubyte", "uint8_t", TypeInfo.MDS_INTEGRAL),
            "short": TypeInfo("short", "int16_t", TypeInfo.MDS_INTEGRAL),
            "ushort": TypeInfo("ushort", "uint16_t", TypeInfo.MDS_INTEGRAL),
            "int": TypeInfo("int", "int32_t", TypeInfo.MDS_INTEGRAL),
            "uint": TypeInfo("uint", "uint32_t", TypeInfo.MDS_INTEGRAL),
            "long": TypeInfo("long", "int64_t", TypeInfo.MDS_INTEGRAL),
            "ulong": TypeInfo("ulong", "uint64_t", TypeInfo.MDS_INTEGRAL),
            "float": TypeInfo("float", "float", TypeInfo.MDS_FLOATING),
            "double": TypeInfo("double", "double", TypeInfo.MDS_FLOATING)
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

typing = __MDSTypes()

# TODO: Not sure that the developer needs to see all the wrapper gen'd stuff in TypeInfo

#TODO: Need record_fields for these:
#      STRING,
#      RECORD,
#      BINDING,
#      ARRAY,
#      NAMESPACE,
