package com.meituan.firefly.adapters;

import com.meituan.firefly.Thrift;
import com.meituan.firefly.TypeAdapter;
import com.meituan.firefly.Types;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TType;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

public class EnumTypeAdapterFactory implements TypeAdapter.TypeAdapterFactory {
    @Override
    public TypeAdapter create(Type type, Thrift thrift) {
        Class<?> rawType = Types.getRawType(type);
        if (!rawType.isEnum()) {
            return null;
        }
        return new EnumTypeAdapter(rawType);
    }

    static class EnumTypeAdapter implements TypeAdapter {
        private final Method getValueMethod;
        private final Method findByValueMehtod;

        EnumTypeAdapter(Class<?> rawType) {
            try {
                getValueMethod = rawType.getMethod("getValue");
                findByValueMehtod = rawType.getMethod("findByValue", int.class);
            } catch (NoSuchMethodException e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public void write(Object o, TProtocol protocol) throws TException {
            try {
                protocol.writeI32((Integer) getValueMethod.invoke(o));
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public Object read(TProtocol protocol) throws TException {
            int value = protocol.readI32();
            try {
                return findByValueMehtod.invoke(null, value);
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new IllegalStateException(e);
            }
        }

        @Override
        public byte getTType() {
            return TType.I32;
        }
    }
}
