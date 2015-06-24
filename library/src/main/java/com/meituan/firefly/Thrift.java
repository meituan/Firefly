package com.meituan.firefly;

import com.meituan.firefly.adapters.*;
import org.apache.thrift.protocol.TProtocol;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.meituan.firefly.TypeAdapter.*;

/**
 * Created by ponyets on 15/6/16.
 */
public class Thrift {
    private final Map<Method, FunctionCall> functionCallMap = new HashMap<>();
    private final Map<Type, TypeAdapter> typeAdapterMap = new HashMap<>();
    private final List<TypeAdapter.TypeAdapterFactory> typeAdapterFactories = new ArrayList<>();

    {
        typeAdapterMap.put(Boolean.class, BOOLEAN_TYPE_ADAPTER);
        typeAdapterMap.put(Byte.class, BYTE_TYPE_ADAPTER);
        typeAdapterMap.put(Short.class, SHORT_TYPE_ADAPTER);
        typeAdapterMap.put(Integer.class, INTEGER_TYPE_ADAPTER);
        typeAdapterMap.put(Long.class, LONG_TYPE_ADAPTER);
        typeAdapterMap.put(Double.class, DOUBLE_TYPE_ADAPTER);
        typeAdapterMap.put(String.class, STRING_TYPE_ADAPTER);
        typeAdapterMap.put(ByteBuffer.class, BYTE_BUFFER_TYPE_ADAPTER);

        typeAdapterFactories.add(new SetTypeAdapterFactory());
        typeAdapterFactories.add(new ListTypeAdapterFactory());
        typeAdapterFactories.add(new MapTypeAdapterFactory());
        typeAdapterFactories.add(new EnumTypeAdapterFactory());
        typeAdapterFactories.add(new StructTypeAdapterFactory());
    }

    public interface TProtocolFactory {
        TProtocol get();
    }

    public TypeAdapter getAdapter(Type type) {
        final Type canonicalizeType = Types.canonicalize(type);
        TypeAdapter typeAdapter = typeAdapterMap.get(canonicalizeType);
        if (typeAdapter == null) {
            synchronized (typeAdapterMap) {
                typeAdapter = typeAdapterMap.get(canonicalizeType);
                if (typeAdapter == null) {
                    typeAdapter = createConverter(canonicalizeType);
                    typeAdapterMap.put(canonicalizeType, typeAdapter);
                }
            }
        }
        return typeAdapter;
    }

    TypeAdapter createConverter(Type type) {
        for (TypeAdapterFactory factory : typeAdapterFactories) {
            TypeAdapter typeAdapter = factory.create(type, this);
            if (typeAdapter != null) {
                return typeAdapter;
            }
        }
        throw new IllegalArgumentException("Unsupport Type : " + type.toString());
    }

    private FunctionCall getFunctionCall(Method method) {
        FunctionCall functionCall = functionCallMap.get(method);
        if (functionCall == null) {
            synchronized (functionCallMap) {
                functionCall = functionCallMap.get(method);
                if (functionCall == null) {
                    functionCall = createFunctionCall(method);
                    functionCallMap.put(method, functionCall);
                }
            }
        }
        return functionCall;
    }

    private FunctionCall createFunctionCall(Method method) {
        return new FunctionCall(method, this);
    }

    private class Client implements InvocationHandler {
        private final TProtocolFactory protocolFactory;
        private int seqid;

        public Client(TProtocolFactory protocolFactory) {
            this.protocolFactory = protocolFactory;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return getFunctionCall(method).apply(args, protocolFactory.get(), ++seqid);
        }
    }

    public <T> T create(Class<T> service, TProtocolFactory protocolFactory) {
        if (!service.isInterface()) {
            throw new IllegalArgumentException("service should be interface");
        }
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service}, new Client(protocolFactory));
    }
}
