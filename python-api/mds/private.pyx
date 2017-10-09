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

# =========================================================================
#  Base
# =========================================================================

cdef class MDSObject(object):

    property is_const:
        def __get__(self):
            return self._const

    property is_null:
        def __get__(self):
            pass  # TODO

    property uuid:
        def __get__(self):
            pass  # TODO
# =========================================================================
#  Managed Values
# =========================================================================

cdef class MDSPrimitiveBase(MDSObject):
    # TODO: Use the API's math operation to ensure atomicity

    def __int__(self):
        pass

    def __long__(self):
        pass

    def __float__(self):
        pass

    def _sanitize(self, value):
        return value

    def _to_python(self):
        raise NotImplementedError("Specialization required.")

    def _to_mds(self):
        raise NotImplementedError("Specialization required.")

    property python_copy:
        def __get__(self):
            raise NotImplementedError("Specialization required.")



cdef class MDSIntPrimitiveBase(MDSPrimitiveBase):

    def _sanitize(self, value):
        if isinstance(value, float):
            value = int(value)
        elif not isinstance(value, int):
            t = type(value)
            raise TypeError('Unable to parse value of type `{t}`')

        if value < self.MIN:
            raise UnderflowError(f"Can't fit {value} in container {self.dtype}")
        elif value > self.MAX:
            raise OverflowError(f"Can't fit {value} in container {self.dtype}")

        return value

    property MIN:
        def __get__(self):
            raise NotImplementedError("Integer specialization required.")

    property MAX:
        def __get__(self):
            raise NotImplementedError("Integer specialization required.")


# =========================================================================
#  Arrays
# =========================================================================

cdef class MDSArrayBase(MDSObject):
    
    def _index_bounds_check(self, index):
         # TODO: Need to handle slices. Should this be a new array?
        cdef long l = <long> len(self)

        if index >= l or index < -l:
            raise IndexError('list index out of range')

        if index < 0:
            index += l

        return index

    def _to_python(self, index):
        raise NotImplementedError('Specialization of MDSList required') 

    def _to_mds(self, index, value):
        raise NotImplementedError('Specialization of MDSList required') 

    def __getitem__(self, index):
        index = self._index_bounds_check(index)
        return self._to_python(index)

    def __setitem__(self, index, value):
        index = self._index_bounds_check(index)
        self._to_mds(index, value)

    def __iter__(self):
        return NotImplemented # TODO Implement this

    def __next__(self):
        return NotImplemented # TODO Implement this

    def __len__(self):
        raise NotImplementedError('Specialization of MDSArrayBase required') 

    def index(self, start=None, end=None):
        return NotImplemented # TODO Implement this

    def count(self, value):
        # TODO: Handle len 0, should this even be instantiable?
        # TODO: Bool still a str-literal here
        if not isinstance(value, type(self[0])):
            raise TypeError("value to be searched for must match list-type `bool`")

        cdef:
            size_t i = 0, l = len(self), c = 0

        for i in range(l):
            if self[i] == value:
                c += 1

        return c

    def sort(self):
        return NotImplemented # TODO Implement this

    def reverse(self):
        return NotImplemented # TODO Implement this

    def copy(self):
        raise NotImplementedError('Specialization of MDSArrayBase required')
 
    property dtype:
        def __get__(self):
            raise NotImplementedError('Specialization of MDSArrayBase required')


cdef class MDSIntArrayBase(MDSArrayBase):

    def _numeric_bounds_check(self, value):
        """
        TODO: This needs to check the bounds of the different int sizes in MDS.
        """
        raise NotImplementedError('Requires a type-specific instantiation')

    def __setitem__(self, index, value):
        index = self._index_bounds_check(index)
        value = self._numeric_bounds_check(value)
        self._to_mds(index, value)

    property dtype:
        def __get__(self):
            return type(1)


cdef class MDSFloatArrayBase(MDSArrayBase):

    property dtype:
        def __get__(self):
            return type(1.0)

# =========================================================================
#  Records
# =========================================================================

cdef class RecordMemberBase(MDSObject):

#   using handle_type = typename managed_type<T>::field_handle_type;
#   static value_type from_core(const core_api_type &val) {
#     return mtype().from_core(val);
#   }

#   /*
#    * When we're and array field, the API read methods only get it as the base
#    * array pointer, so we need to downcast.  This should be safe
#    */
#   template<typename U = T, typename = std::enable_if_t<is_mds_array<U>::value>>
#   static value_type from_core(const api::managed_array_base_handle &handle) {
#     using elt_type = typename mtype::elt_type;
#     core_api_type h(handle.pointer()->template downcast<elt_type::kind>(),
#                     handle.view());
#     return from_core(h);
#   }

#   static core_api_type to_core(const value_type &val) {
#     return mtype().to_core(val);
#   }
# Confusion between record_field, record_member, managed_value, managed_type?          

    def read(self):
        raise NotImplementedError('Specialization Required')

    def write(self, value):
        raise NotImplementedError('Specialization Required')

    cpdef ensure_type(self):
        raise NotImplementedError('Specialization Required')


cdef class ConstRecordMemberBase(RecordMemberBase):

    def write(self, value):
        raise ConstError('Can\'t assign value to const field.')


cdef class RecordTypeDeclaration(MDSObject):

    def __cinit__(self, str ident, object parent, dict fields):
        self._field_decls = fields
        self._ident = ident
        self._declared_type = record_type_handle.declare(convert_py_to_ish(ident))

#  api::record_type_handle declare(const mds_string &name,
#                                  managed_type<mds_record>) {
#    return api::record_type_handle::declare(name.handle());
#  }
#
#  template<typename S>
#  api::record_type_handle declare(const mds_string &name, managed_type<S> s) {
#    static_assert(is_record_type<S>::value, "Super type not a record type");
#    auto sp = s.ensure_created();
#    return api::record_type_handle::declare(name.handle(), sp);
#  }    
# NOTE: Not sure where this is called, delegated to _decalre_record_type(ident)
    # def __declare(self, ident):
    #     return record_type_handle.declare(convert_py_to_ish(ident))

    def __getattr__(self, key):
        if key not in self._field_decls:
            return super().__getattr__(key)

        return self._field_decls[key].read()

    def __setattr__(self, key, value):
        if key not in self._field_decls:
            super().__setattr__(key, value)
        else:
            self._field_decls[key].write(value)

    def __declare_fields(self): # TODO: This is probably wrong, no binding to rm (placeholder only) -- check
        map(lambda rm: _declare_record_member(rm, self._ident, self._declared_type), 
            self.__field_decls.values())

    def __ensure_field_types(self):
        map(lambda rm: rm.ensure_type(), self.__field_decls.values())

    def __ensure_created(self):
        if self._created_type.is_null():
            # This needs to work in two phases.  In the first, we declare
            # the fields, and we have to be sure that this can't result
            # in a recursive call to __ensure_created() or we will have a
            # deadlock.  But we have to make sure that any referenced
            # types (including this one) are created, so we have a second
            # pass that loops through the fields and does this.  But we
            # go through the second pass after setting __created_type, and
            # we use a check on _created_type to avoid locking.
            #
            # There is a failure window in between the internal call to
            # __ensure_created() and the end of this function.  If we die
            # there, we will have a created record type that may have
            # fields whose types are uncreated record types.  We will
            # not, however, have actually created any instances of this
            # record type (since we died).  This will result in an
            # incompatible type exception, since the types won't be the
            # same nor will one forward to the other.
            #
            # TODO (upstream): Make it possible for the next process to finish the
            # job by overwriting the field type with its own.
            
            # TODO call_once semantics -- std::call_once(_created
            self.__delcare_fields()
            self._created_type = self._declared_type.ensure_created()
            _register_type(self._created_type)
            self.__ensure_field_types()

        # TODO does this need to return the c++ type, or will a bint suffice?
        # TODO return self._created_type
        return True

cdef class RecordToken(MDSObject):

# struct rc_token {
#  protected:
#   friend class mds_record;
#   mutable std::shared_ptr<mds_record> _shared_ptr;
#
#   virtual ~rc_token() {
#     recent_stack().pop();
#   }
#
#   virtual handle_type create() const = 0;
#   /*
#    * If we create the initial shared ptr as a
#    * shared_ptr<mds_record>, then when the last shared_ptr
#    * goes away and the mds_record is destroyed, if there were
#    * virtual functions added below, the wrong pointer to the
#    * memory will be wrong.  So we indirect through the rc_token,
#    * which knows the type to create the pointer to cache.
#    */
#   virtual void cache_shared(mds_record *) const = 0;
#
#   rc_token() {
#     recent_stack().push(nullptr);
#   }
#
#   static std::stack<mds_record *> &recent_stack() {
#     static thread_local std::stack<mds_record *> s;
#     return s;
#   }
# };

# template<typename R, typename = enable_if_record<R>>
# struct typed_rc_token : mds_record::rc_token {
#   mds_record::handle_type create() const override {
#     ensure_thread_initialized();
#     return managed_type<R>().ensure_created().create_record();
#   }

#   std::shared_ptr<R> cached_shared_ptr() const {
#     return std::static_pointer_cast<R>(_shared_ptr);
#   }

#   void cache_shared(mds_record *r) const override {
#     /*
#      * This is called from mds_record's ctor, so the actual
#      * concrete class hasn't been constructed yet.  I'm assuming
#      * that creating a shared_ptr to it won't involve anythnig more
#      * than doing some adjustment to the this pointer.
#      */
#     R *dc_rec = static_cast<R *>(r);
#     _shared_ptr = std::shared_ptr<R>(dc_rec);
#     recent_stack().top() = r;
#   }

#   static R *being_constructed() {
#     auto &recent = recent_stack();
#     R *as_record = static_cast<R*>(recent.top());
#     assert(recent.top() != nullptr);
#     return as_record;
#   }
# };
    pass
