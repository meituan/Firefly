package com.meituan.firefly.adapters;

import com.meituan.firefly.Thrift;
import com.meituan.firefly.TypeAdapter;
import com.meituan.firefly.Types;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TMap;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TType;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class MapTypeAdapterFactory implements TypeAdapter.TypeAdapterFactory {

    @Override
    public TypeAdapter create(Type type, Thrift thrift) {
        Class<?> rawType = Types.getRawType(type);
        if (!Map.class.isAssignableFrom(rawType)) {
            return null;
        }
        if (!(type instanceof ParameterizedType)) {
            throw new IllegalArgumentException("map field must be parameterized");
        }
        Type[] parameterTypes = ((ParameterizedType) type).getActualTypeArguments();
        Type keyType = parameterTypes[0];
        Type valueType = parameterTypes[1];
        TypeAdapter keyTypeAdapter = thrift.getAdapter(keyType);
        TypeAdapter valueTypeAdapter = thrift.getAdapter(valueType);
        return new MapTypeAdapter(keyTypeAdapter, valueTypeAdapter);
    }

    static class MapTypeAdapter implements TypeAdapter<Map<?, ?>> {
        private final TypeAdapter keyTypeAdapter;
        private final TypeAdapter valueTypeAdapter;

        MapTypeAdapter(TypeAdapter keyTypeAdapter, TypeAdapter valueTypeAdapter) {
            this.keyTypeAdapter = keyTypeAdapter;
            this.valueTypeAdapter = valueTypeAdapter;
        }

        @Override
        public void write(Map<?, ?> map, TProtocol protocol) throws TException {
            protocol.writeMapBegin(new TMap(keyTypeAdapter.getTType(), valueTypeAdapter.getTType(), map.size()));
            for (Map.Entry entry : map.entrySet()) {
                keyTypeAdapter.write(entry.getKey(), protocol);
                valueTypeAdapter.write(entry.getValue(), protocol);
            }
            protocol.writeMapEnd();
        }

        @Override
        public Map<?, ?> read(TProtocol protocol) throws TException {
            TMap tmap = protocol.readMapBegin();
            HashMap hashMap = new HashMap(tmap.size);
            for (int i = 0, n = tmap.size; i < n; i++) {
                Object key = keyTypeAdapter.read(protocol);
                Object value = valueTypeAdapter.read(protocol);
                hashMap.put(key, value);
            }
            protocol.readMapEnd();
            return hashMap;
        }

        @Override
        public byte getTType() {
            return TType.MAP;
        }
    }
}
