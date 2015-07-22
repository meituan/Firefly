package com.meituan.firefly.adapters;

import com.meituan.firefly.Thrift;
import com.meituan.firefly.TypeAdapter;
import com.meituan.firefly.Types;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TSet;
import org.apache.thrift.protocol.TType;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class SetTypeAdapterFactory implements TypeAdapter.TypeAdapterFactory {
    @Override
    public TypeAdapter create(Type type, Thrift thrift) {
        Class<?> rawType = Types.getRawType(type);
        if (!Set.class.isAssignableFrom(rawType)) {
            return null;
        }

        if (!(type instanceof ParameterizedType)) {
            throw new IllegalArgumentException("set field must be parameterized");
        }
        Type valueType = ((ParameterizedType) type).getActualTypeArguments()[0];
        TypeAdapter valueTypeAdapter = thrift.getAdapter(valueType);
        return new SetTypeAdapter(valueTypeAdapter);
    }

    static class SetTypeAdapter implements TypeAdapter<Set> {
        private final TypeAdapter valueTypeAdapter;

        SetTypeAdapter(TypeAdapter valueTypeAdapter) {
            this.valueTypeAdapter = valueTypeAdapter;
        }

        @Override
        public void write(Set set, TProtocol protocol) throws TException {
            protocol.writeSetBegin(new TSet(valueTypeAdapter.getTType(), set.size()));
            for (Object o : set) {
                valueTypeAdapter.write(o, protocol);
            }
            protocol.writeSetEnd();
        }

        @Override
        public Set read(TProtocol protocol) throws TException {
            TSet tset = protocol.readSetBegin();
            HashSet hashSet = new HashSet(tset.size);
            for (int i = 0, n = tset.size; i < n; i++) {
                hashSet.add(valueTypeAdapter.read(protocol));
            }
            protocol.readSetEnd();
            return hashSet;
        }

        @Override
        public byte getTType() {
            return TType.SET;
        }
    }
}
