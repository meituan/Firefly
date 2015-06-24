package com.meituan.firefly.adapters;

import com.meituan.firefly.Thrift;
import com.meituan.firefly.TypeAdapter;
import com.meituan.firefly.Types;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TList;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TType;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ponyets on 15/6/18.
 */
public class ListTypeAdapterFactory implements TypeAdapter.TypeAdapterFactory {
    @Override
    public TypeAdapter create(Type type, Thrift thrift) {
        Class<?> rawType = Types.getRawType(type);
        if (!List.class.isAssignableFrom(rawType)) {
            return null;
        }

        if (!(type instanceof ParameterizedType)) {
            throw new IllegalArgumentException("list field must be parameterized");
        }
        Type valueType = ((ParameterizedType) type).getActualTypeArguments()[0];
        TypeAdapter valueTypeAdapter = thrift.getAdapter(valueType);
        return new ListTypeAdapter(valueTypeAdapter);
    }

    static class ListTypeAdapter implements TypeAdapter<List> {
        private final TypeAdapter valueTypeAdapter;

        ListTypeAdapter(TypeAdapter valueTypeAdapter) {
            this.valueTypeAdapter = valueTypeAdapter;
        }

        @Override
        public void write(List list, TProtocol protocol) throws TException {
            protocol.writeListBegin(new TList(valueTypeAdapter.getTType(), list.size()));
            for (Object o : list) {
                valueTypeAdapter.write(o, protocol);
            }
            protocol.writeListEnd();
        }

        @Override
        public List read(TProtocol protocol) throws TException {
            TList tlist = protocol.readListBegin();
            ArrayList arrayList = new ArrayList(tlist.size);
            for (int i = 0, n = tlist.size; i < n; i++) {
                arrayList.add(valueTypeAdapter.read(protocol));
            }
            protocol.readListEnd();
            return arrayList;
        }

        @Override
        public byte getTType() {
            return TType.LIST;
        }
    }
}
