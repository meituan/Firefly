package com.meituan.firefly.adapters;

import com.meituan.firefly.Thrift;
import com.meituan.firefly.TypeAdapter;
import com.meituan.firefly.Types;
import com.meituan.firefly.annotations.Union;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.*;

import java.lang.reflect.Field;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StructTypeAdapterFactory implements TypeAdapter.TypeAdapterFactory {
    @Override
    public TypeAdapter create(Type type, Thrift thrift) {
        Class<?> rawType = Types.getRawType(type);
        return new StructTypeAdapter(rawType, thrift);
    }

    static class StructTypeAdapter implements TypeAdapter {
        static class FieldAdapter {
            final short id;
            final boolean required;
            final Field field;
            final TypeAdapter adapter;

            public FieldAdapter(short id, boolean required, Field field, TypeAdapter adapter) {
                this.id = id;
                this.required = required;
                this.field = field;
                this.adapter = adapter;
            }

            TField getTTField(){
                return new TField(field.getName(), adapter.getTType(), id);
            }
        }

        private final Map<Short, FieldAdapter> fieldAdapterMap;
        private final List<FieldAdapter> fieldAdapterList;
        private final Class<?> rawType;
        private final TStruct tStruct;
        final boolean isUnion;

        StructTypeAdapter(Class<?> rawType, Thrift thrift) {
            this.rawType = rawType;
            Field[] fields = rawType.getFields();
            fieldAdapterMap = new HashMap<>(fields.length);
            fieldAdapterList = new ArrayList<>(fields.length);
            tStruct = new TStruct(rawType.getSimpleName());
            for (Field field : fields) {
                com.meituan.firefly.annotations.Field fieldAnnotation = field.getAnnotation(com.meituan.firefly.annotations.Field.class);
                if (fieldAnnotation == null) {
                    throw new IllegalArgumentException("field " + field.getName() + " of struct " + rawType.getSimpleName() + " should be annotated with @Field");
                }
                TypeAdapter fieldTypeAdapter = thrift.getAdapter(field.getGenericType());
                FieldAdapter fieldAdapter = new FieldAdapter(fieldAnnotation.id(), fieldAnnotation.required(), field, fieldTypeAdapter);
                fieldAdapterMap.put(fieldAnnotation.id(), fieldAdapter);
                fieldAdapterList.add(fieldAdapter);
            }
            isUnion = rawType.getAnnotation(Union.class) != null;
        }

        @Override
        public void write(Object o, TProtocol protocol) throws TException {
            protocol.writeStructBegin(tStruct);
            boolean fieldSet = false;
            for (FieldAdapter fieldAdapter : fieldAdapterList) {
                Object fieldValue = null;
                try {
                    fieldValue = fieldAdapter.field.get(o);
                } catch (IllegalAccessException e) {
                    throw new IllegalStateException(e);
                }
                if (fieldValue == null) {
                    if (fieldAdapter.required) {
                        throw new TProtocolException("Required field '" + fieldAdapter.field.getName() + "' was not present! Struct: " + rawType.getSimpleName());
                    }
                } else {
                    if (fieldSet && isUnion) {
                        throw new TProtocolException("Union with more than one field! Struct: " + rawType.getSimpleName());
                    }
                    protocol.writeFieldBegin(fieldAdapter.getTTField());
                    fieldAdapter.adapter.write(fieldValue, protocol);
                    protocol.writeFieldEnd();
                    fieldSet = true;
                }
            }
            protocol.writeFieldStop();
            protocol.writeStructEnd();
        }

        @Override
        public Object read(TProtocol protocol) throws TException {
            protocol.readStructBegin();
            Object result = null;
            try {
                result = rawType.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
            boolean fieldSet = false;
            while (true) {
                TField tField = protocol.readFieldBegin();
                if (tField.type == TType.STOP) {
                    break;
                }
                if (fieldSet && isUnion) {
                    throw new TProtocolException("Union with more than one field! Struct: " + rawType.getSimpleName());
                }
                fieldSet = true;
                FieldAdapter fieldAdapter = fieldAdapterMap.get(tField.id);
                if (fieldAdapter == null || tField.type != fieldAdapter.adapter.getTType()) {
                    TProtocolUtil.skip(protocol, tField.type);
                } else {
                    Object fieldValue = fieldAdapter.adapter.read(protocol);
                    try {
                        fieldAdapter.field.set(result, fieldValue);
                    } catch (IllegalAccessException e) {
                        throw new IllegalStateException(e);
                    }
                }
                protocol.readFieldEnd();
            }
            protocol.readStructEnd();
            try {
                for (FieldAdapter fieldAdapter : fieldAdapterList) {
                    Object fieldValue = fieldAdapter.field.get(result);
                    if (fieldValue == null && fieldAdapter.required) {
                        throw new TProtocolException("Required field '" + fieldAdapter.field.getName() + "' was not present! Struct: " + rawType.getSimpleName());
                    }
                }
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
            return result;
        }

        @Override
        public byte getTType() {
            return TType.STRUCT;
        }
    }
}
