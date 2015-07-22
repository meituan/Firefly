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
 * {@link Thrift} is the facade of Firefly library, provides static method for creating
 * dynamic thrift client instances of services.
 * <p>For example:</p>
 * <pre>
 *     service ExampleService{
 *         string hello(string who);
 *     }
 * </pre>
 * The thrift idl mentioned, will generate a interface ExampleService can be used like:
 * <pre>
 *     Thrift thrift = new Thrift();
 *     ProtocolFactory factory = new SimpleProtocolFactory(){
 *         public TProtocol get(){
 *             TTransport transport = new OkhttpTransport("http://api.example.com", new OkhttpClient());
 *             return new TBinaryProtocol(transport);
 *         }
 *     }
 *     ExampleService exampleClient = thrift.create(ExampleService.class, factory);
 *     exampleClient.hello("Sam");
 * </pre>
 * When calling exampleClient's hello method, firefly will make a rpc call to the remote service deployed on host
 * api.example.com using http, then return the remote service's result (maybe "hello sam!") as method result.
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
        TProtocol get(Method method, Object[] args);
    }

    public static abstract class SimpleTProtocolFactory implements TProtocolFactory {
        @Override
        public TProtocol get(Method method, Object[] args) {
            return get();
        }

        public abstract TProtocol get();
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

    private static class Chain implements Processor {
        private final Processor pre;
        private final Interceptor interceptor;

        public Chain(Processor pre, Interceptor interceptor) {
            this.pre = pre;
            this.interceptor = interceptor;
        }

        @Override
        public Object process(Method method, Object[] args, TProtocol protocol, int seqId) throws Throwable {
            return interceptor.intercept(method, args, protocol, seqId, pre);
        }
    }

    private class Client implements InvocationHandler {
        private final TProtocolFactory protocolFactory;
        private int seqid;
        private final Processor processor;


        public Client(TProtocolFactory protocolFactory, Interceptor[] interceptors) {
            this.protocolFactory = protocolFactory;
            Processor processor = new Processor() {
                @Override
                public Object process(Method method, Object[] args, TProtocol protocol, int seqId) throws Throwable {
                    return getFunctionCall(method).apply(args, protocol, seqId);
                }
            };
            if (interceptors != null && interceptors.length > 0) {
                for (Interceptor interceptor : interceptors) {
                    processor = new Chain(processor, interceptor);
                }
            }
            this.processor = processor;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return processor.process(method, args, protocolFactory.get(method, args), ++seqid);
        }
    }

    /**
     * Create a dynamic proxy of a specified interface that generated by firefly generator from a service in thrift's idl.
     *
     * @param service         a interface generated from a service in thrift's idl
     * @param protocolFactory a factory return protocols used in every method call
     * @param interceptors    interceptors works as a chain, interceptor in front will be executed downstream
     * @param <T>
     * @return an instance implements the service interface
     */
    public <T> T create(Class<T> service, TProtocolFactory protocolFactory, Interceptor... interceptors) {
        if (!service.isInterface()) {
            throw new IllegalArgumentException("service should be interface");
        }
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service}, new Client(protocolFactory, interceptors));
    }
}
