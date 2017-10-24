from collections import namedtuple
from itertools import chain
from typing import Dict, Text, Type


Bounds = namedtuple("Bounds", ["min", "max"])
_MDS = "_MDS"
_CONST = "Const"


class TypeInfo():

    def __init__(self, api: str):
        self.api = api
        self.title = self._format_title(api.title())
        self.title_const = f"{_CONST}{self.title}"

        # Python object names
        self.title_array = f"{self.title}Array"
        self.title_array_init = f"{self.title_array}_Init"
        self.title_array_cinit = f"{self.title_array}_Inplace"

        self.title_name_binding = f"{_MDS}{self.title}NameBinding"
        self.f_bind = f"bind_{api}"

        self.title_record_field = f"{_MDS}{self.title}RecordField"
        self.title_record_field_reference = f"{_MDS}{self.title}RecordFieldReference"
        self.title_const_record_field_reference = f"{_MDS}{self.title_const}RecordFieldReference"
        self.title_record_member = f"{_MDS}{self.title}RecordMember"
        self.title_const_record_member = f"{_MDS}{self.title_const}RecordMember"

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

        # To be overriden
        self.c_type = None

    def __repr__(self):
        return f'<{self.__class__.__name__}: {self.title} ({self.kind})>'

    def _format_title(self, title: Text) -> Text:
        if title.startswith('U'):
            title = self.api[:2].upper() + self.api[2:]

        return title

    @property
    def is_integral(self) -> bool:
        return isinstance(self, MDSIntegralTypeInfo)

    @property
    def is_floating(self) -> bool:
        return isinstance(self, MDSFloatingTypeInfo)

    @property
    def is_arithmetic(self) -> bool:
        return isinstance(self, (MDSIntegralTypeInfo, MDSFloatingTypeInfo))

    @property
    def is_primitive(self) -> bool:
        return self.is_arithmetic or isinstance(self, MDSBoolTypeInfo)

    @property
    def is_composite(self) -> bool:
        return not self.is_primitive

    @property
    def is_string(self) -> bool:
        return isinstance(self, MDSStringTypeInfo)

    @property
    def is_array(self) -> bool:
        return isinstance(self, MDSArrayTypeInfo)

    @property
    def is_record(self) -> bool:
        return isinstance(self, MDSRecordTypeInfo)


class MDSPrimitiveTypeInfo(TypeInfo):

    ARRAY = f"{_MDS}ArrayBase"
    PRIMITIVE = f"{_MDS}PrimitiveBase"
    CONST_ARRAY = f"{_MDS}{_CONST}ArrayBase"
    CONST_PRIMITIVE = f"{_MDS}{_CONST}PrimitiveBase"

    def __init__(self, c_type, py_type, **kwargs):
        self.c_type = c_type
        self.py_type_t = py_type
        self.py_type = py_type.__name__
        super().__init__(**kwargs)


class MDSBoolTypeInfo(MDSPrimitiveTypeInfo):
    
    def __init__(self, api: Text, c_type: Text):
        super().__init__(api=api, c_type=c_type, py_type=bool)


class MDSIntegralTypeInfo(MDSPrimitiveTypeInfo):

    ARRAY = f"{_MDS}IntArrayBase"
    PRIMITIVE = f"{_MDS}IntPrimitiveBase"
    CONST_ARRAY = f"{_MDS}{_CONST}IntArrayBase"
    CONST_PRIMITIVE = f"{_MDS}{_CONST}IntPrimitiveBase"
    MDS_INTEGRAL_BOUNDS = dict()

    def __init__(self, api: Text, c_type: Text):
        super().__init__(api=api, c_type=c_type, py_type=int)

        if not self.MDS_INTEGRAL_BOUNDS:
            for p in (8, 16, 32, 64):
                self.MDS_INTEGRAL_BOUNDS[f"int{p}_t"] = Bounds(-(2 ** (p - 1)), (2 ** (p - 1)) - 1)
                self.MDS_INTEGRAL_BOUNDS[f"uint{p}_t"] = Bounds(0, (2 ** p) - 1)

        self.bounds = self.MDS_INTEGRAL_BOUNDS[c_type]


class MDSFloatingTypeInfo(MDSPrimitiveTypeInfo):

    ARRAY = f"{_MDS}FloatArrayBase"
    PRIMITIVE = f"{_MDS}FloatPrimitiveBase"
    CONST_ARRAY = f"{_MDS}{_CONST}FloatArrayBase"
    CONST_PRIMITIVE = f"{_MDS}{_CONST}FloatPrimitiveBase"

    def __init__(self, api: Text, c_type: Text):
        super().__init__(api=api, c_type=c_type, py_type=float)


class MDSCompositeTypeInfo(TypeInfo):

    ARRAY = f"{_MDS}ArrayBase"
    CONST_ARRAY = f"{_MDS}{_CONST}ArrayBase"


class MDSRecordTypeInfo(MDSCompositeTypeInfo):

    def __getitem__(self, item: Type):
        pass


class MDSStringTypeInfo(MDSCompositeTypeInfo):
    pass


class MDSArrayTypeInfo(MDSCompositeTypeInfo):

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.kind = "mds::api::kind::ARRAY"

    def _format_title(self, title: Text) -> Text:
        return super()._format_title(title) + "Array"


class MDSRecordArrayTypeInfo(MDSArrayTypeInfo):

    def __getitem__(self, item: Type):
        pass


# Public-facing Types


class _MDSTypesBase(object):

    def __init__(self, mappings: Dict[Text, TypeInfo]):
        self.__dict__.update(mappings)
        self.__original = mappings

    def items(self):
        return self.__original.items()


class _MDSPrimitiveTypes(_MDSTypesBase):

    def __init__(self):
        super().__init__({
            "bool": MDSBoolTypeInfo("bool", "bool"),
            "byte": MDSIntegralTypeInfo("byte", "int8_t"),
            "ubyte": MDSIntegralTypeInfo("ubyte", "uint8_t"),
            "short": MDSIntegralTypeInfo("short", "int16_t"),
            "ushort": MDSIntegralTypeInfo("ushort", "uint16_t"),
            "int": MDSIntegralTypeInfo("int", "int32_t"),
            "uint": MDSIntegralTypeInfo("uint", "uint32_t"),
            "long": MDSIntegralTypeInfo("long", "int64_t"),
            "ulong": MDSIntegralTypeInfo("ulong", "uint64_t"),
            "float": MDSFloatingTypeInfo("float", "float"),
            "double": MDSFloatingTypeInfo("double", "double")
        })

class _MDSCompositeTypes(_MDSTypesBase):

    def __init__(self):
        super().__init__({
            "string": MDSStringTypeInfo("string"),
            "record": MDSRecordTypeInfo("record")
        })


class _MDSArrayTypes(_MDSTypesBase):

    def __init__(self):
        data = dict()

        for api, t_info in chain(_MDSPrimitiveTypes().items(), _MDSCompositeTypes().items()):
            data[t_info.api] = MDSArrayTypeInfo(t_info.api)

        super().__init__(data)


class _MDSTypes():
    primitives = _MDSPrimitiveTypes()
    composites = _MDSCompositeTypes()
    arrays = _MDSArrayTypes()

    def items(self):
        for k, v in chain(self.primitives.items(), self.composites.items(), self.arrays.items()):
            yield k, v

# Expose to programmers through this binding, mds.typing:
typing = _MDSTypes()
