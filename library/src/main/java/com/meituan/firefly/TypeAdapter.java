package com.meituan.firefly;

import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.protocol.TType;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;

/**
 * Write an object of specified type into protocol, or read an object of specified type from protocol.
 */
public interface TypeAdapter<T> {
    /**
     * Write an object of type T into protocol
     *
     * @param t        the object to write
     * @param protocol protocol to write into
     */
    void write(T t, TProtocol protocol) throws TException;

    /**
     * Read an object of type T from protocol
     *
     * @param protocol the protocol to read from
     */
    T read(TProtocol protocol) throws TException;

    /**
     * @return Thrift {@link TType} of the specified type
     */
    byte getTType();

    interface TypeAdapterFactory {
        TypeAdapter create(Type type, Thrift thrift);
    }

    TypeAdapter<Boolean> BOOLEAN_TYPE_ADAPTER = new TypeAdapter<Boolean>() {
        @Override
        public void write(Boolean aBoolean, TProtocol protocol) throws TException {
            protocol.writeBool(aBoolean);
        }

        @Override
        public Boolean read(TProtocol protocol) throws TException {
            return protocol.readBool();
        }

        @Override
        public byte getTType() {
            return TType.BOOL;
        }
    };
    TypeAdapter<Byte> BYTE_TYPE_ADAPTER = new TypeAdapter<Byte>() {
        @Override
        public void write(Byte aByte, TProtocol protocol) throws TException {
            protocol.writeByte(aByte);
        }

        @Override
        public Byte read(TProtocol protocol) throws TException {
            return protocol.readByte();
        }

        @Override
        public byte getTType() {
            return TType.BYTE;
        }
    };
    TypeAdapter<Short> SHORT_TYPE_ADAPTER = new TypeAdapter<Short>() {
        @Override
        public void write(Short aShort, TProtocol protocol) throws TException {
            protocol.writeI16(aShort);
        }

        @Override
        public Short read(TProtocol protocol) throws TException {
            return protocol.readI16();
        }

        @Override
        public byte getTType() {
            return TType.I16;
        }
    };
    TypeAdapter<Integer> INTEGER_TYPE_ADAPTER = new TypeAdapter<Integer>() {
        @Override
        public void write(Integer integer, TProtocol protocol) throws TException {
            protocol.writeI32(integer);
        }

        @Override
        public Integer read(TProtocol protocol) throws TException {
            return protocol.readI32();
        }

        @Override
        public byte getTType() {
            return TType.I32;
        }
    };
    TypeAdapter<Long> LONG_TYPE_ADAPTER = new TypeAdapter<Long>() {
        @Override
        public void write(Long aLong, TProtocol protocol) throws TException {
            protocol.writeI64(aLong);
        }

        @Override
        public Long read(TProtocol protocol) throws TException {
            return protocol.readI64();
        }

        @Override
        public byte getTType() {
            return TType.I64;
        }
    };
    TypeAdapter<Double> DOUBLE_TYPE_ADAPTER = new TypeAdapter<Double>() {
        @Override
        public void write(Double aDouble, TProtocol protocol) throws TException {
            protocol.writeDouble(aDouble);
        }

        @Override
        public Double read(TProtocol protocol) throws TException {
            return protocol.readDouble();
        }

        @Override
        public byte getTType() {
            return TType.DOUBLE;
        }
    };
    TypeAdapter<String> STRING_TYPE_ADAPTER = new TypeAdapter<String>() {
        @Override
        public void write(String s, TProtocol protocol) throws TException {
            protocol.writeString(s);
        }

        @Override
        public String read(TProtocol protocol) throws TException {
            return protocol.readString();
        }

        @Override
        public byte getTType() {
            return TType.STRING;
        }
    };
    TypeAdapter<ByteBuffer> BYTE_BUFFER_TYPE_ADAPTER = new TypeAdapter<ByteBuffer>() {
        @Override
        public void write(ByteBuffer byteBuffer, TProtocol protocol) throws TException {
            protocol.writeBinary(byteBuffer);
        }

        @Override
        public ByteBuffer read(TProtocol protocol) throws TException {
            return protocol.readBinary();
        }

        @Override
        public byte getTType() {
            return TType.STRING;
        }
    };


}
