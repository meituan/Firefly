package com.meituan.firefly.android;

import android.os.Parcel;
import android.os.Parcelable;

import com.meituan.firefly.Thrift;
import com.meituan.firefly.TypeAdapter;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TIOStreamTransport;
import org.apache.thrift.transport.TTransport;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.lang.reflect.Array;

/**
 * Super class for all generated structures supports Android Parcelable.
 */
public abstract class StructBase implements Parcelable, Serializable {
    public static class Creator<T extends StructBase> implements Parcelable.Creator<T> {
        private final Class<T> tClass;

        public Creator(Class<T> tClass) {
            this.tClass = tClass;
        }

        @Override
        public T createFromParcel(Parcel source) {
            byte[] bytes = source.createByteArray();
            TTransport transport = new TIOStreamTransport(new ByteArrayInputStream(bytes));
            TProtocol protocol = new TBinaryProtocol(transport);
            TypeAdapter<T> typeAdapter = Thrift.instance.getAdapter(tClass);
            try {
                return typeAdapter.read(protocol);
            } catch (TException e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public T[] newArray(int size) {
            return (T[]) Array.newInstance(tClass, size);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        TTransport transport = new TIOStreamTransport(stream);
        TProtocol protocol = new TBinaryProtocol(transport);
        TypeAdapter typeAdapter = Thrift.instance.getAdapter(this.getClass());
        try {
            typeAdapter.write(this, protocol);
        } catch (TException e) {
            throw new RuntimeException(e);
        }
        dest.writeByteArray(stream.toByteArray());
    }
}
