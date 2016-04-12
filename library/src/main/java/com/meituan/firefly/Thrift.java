package com.meituan.firefly;

import com.meituan.firefly.adapters.*;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TProtocol;
import rx.Scheduler;

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
public final class Thrift {
    public static final Thrift instance = new Thrift();
    private boolean hasDefaultValue = false;
    private final Map<Method, FunctionCall> functionCallMap = new HashMap<>();
    private final Map<Type, TypeAdapter> typeAdapterMap = new HashMap<>();
    private final List<TypeAdapter.TypeAdapterFactory> typeAdapterFactories = new ArrayList<>();
    private final ThreadLocal<Map<Type, FutureTypeAdapter<?>>> calls = new ThreadLocal<>();

    private Thrift() {
        typeAdapterMap.put(Boolean.class, BOOLEAN_TYPE_ADAPTER);
        typeAdapterMap.put(Byte.class, BYTE_TYPE_ADAPTER);
        typeAdapterMap.put(Short.class, SHORT_TYPE_ADAPTER);
        typeAdapterMap.put(Integer.class, INTEGER_TYPE_ADAPTER);
        typeAdapterMap.put(Long.class, LONG_TYPE_ADAPTER);
        typeAdapterMap.put(Double.class, DOUBLE_TYPE_ADAPTER);
        typeAdapterMap.put(String.class, STRING_TYPE_ADAPTER);
        typeAdapterMap.put(ByteBuffer.class, BYTE_BUFFER_TYPE_ADAPTER);
        typeAdapterMap.put(byte[].class, BYTE_ARRAY_TYPE_ADAPTER);

        typeAdapterFactories.add(new SetTypeAdapterFactory());
        typeAdapterFactories.add(new ListTypeAdapterFactory());
        typeAdapterFactories.add(new MapTypeAdapterFactory());
        typeAdapterFactories.add(new EnumTypeAdapterFactory());
        typeAdapterFactories.add(new StructTypeAdapterFactory());
    }

    public void setDefaultValue(boolean defaultValue) {
        this.hasDefaultValue = defaultValue;
    }

    public boolean hasDefaultValue() {
        return hasDefaultValue;
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
        //void/Void check
        if (Void.class.equals(type) || void.class.equals(type)) {
            return null;
        }
        final Type canonicalizeType = Types.canonicalize(type);
        TypeAdapter typeAdapter = typeAdapterMap.get(canonicalizeType);
        if (typeAdapter == null) {
            synchronized (typeAdapterMap) {
                typeAdapter = typeAdapterMap.get(canonicalizeType);
                if (typeAdapter == null) {
                    Map<Type, FutureTypeAdapter<?>> threadCalls = calls.get();
                    boolean requiresThreadLocalCleanup = false;
                    if (threadCalls == null) {
                        threadCalls = new HashMap<>();
                        calls.set(threadCalls);
                        requiresThreadLocalCleanup = true;
                    }
                    FutureTypeAdapter ongoingCall = threadCalls.get(canonicalizeType);
                    if (ongoingCall != null) {
                        return ongoingCall;
                    }

                    try {
                        FutureTypeAdapter call = new FutureTypeAdapter();
                        threadCalls.put(canonicalizeType, call);

                        typeAdapter = createTypeAdapter(canonicalizeType);
                        call.setDelegate(typeAdapter);
                        typeAdapterMap.put(canonicalizeType, typeAdapter);
                    } finally {
                        threadCalls.remove(canonicalizeType);
                        if (requiresThreadLocalCleanup) {
                            calls.remove();
                        }
                    }
                }
            }
        }
        return typeAdapter;
    }

    TypeAdapter createTypeAdapter(Type type) {
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
            this(protocolFactory, null, interceptors);
        }

        public Client(TProtocolFactory protocolFactory, final Scheduler subscribScheduler, Interceptor[] interceptors) {
            this.protocolFactory = protocolFactory;
            Processor processor = new Processor() {
                @Override
                public Object process(Method method, Object[] args, TProtocol protocol, int seqId) throws Throwable {
                    return Thrift.this.getFunctionCall(method).apply(args, protocol, seqId, subscribScheduler);
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
            if (method.getDeclaringClass() == Object.class) {
                return method.invoke(this, args);
            }
            return processor.process(method, args, protocolFactory.get(method, args), ++seqid);
        }
    }

    /**
     * Create a dynamic proxy of a specified interface that generated by firefly generator from a service in thrift's idl.
     *
     * @param service         a interface generated from a service in thrift's idl
     * @param protocolFactory a factory return protocols used in every method call
     * @param interceptors    interceptors works as a chain, interceptor in front will be executed downstream
     * @return an instance implements the service interface
     */
    public <T> T create(Class<T> service, TProtocolFactory protocolFactory, Interceptor... interceptors) {

        return create(service, protocolFactory, null, interceptors);
    }

    /**
     * Create a dynamic proxy of a specified interface that generated by firefly generator from a service in thrift's idl.
     *
     * @param service           a interface generated from a service in thrift's idl
     * @param protocolFactory   a factory return protocols used in every method call
     * @param subscribScheduler a scheduler on which method call will be run,recommend create a new scheduler in the a backgroud thread
     * @param interceptors      interceptors works as a chain, interceptor in front will be executed downstream
     * @return an instance implements the service interface
     */
    public <T> T create(Class<T> service, TProtocolFactory protocolFactory, Scheduler subscribScheduler, Interceptor... interceptors) {
        if (!service.isInterface()) {
            throw new IllegalArgumentException("service should be interface");
        }
        return (T) Proxy.newProxyInstance(service.getClassLoader(), new Class<?>[]{service}, new Client(protocolFactory, subscribScheduler, interceptors));
    }

    static class FutureTypeAdapter<T> implements TypeAdapter<T> {
        TypeAdapter<T> delegate;

        void setDelegate(TypeAdapter<T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public void write(T t, TProtocol protocol) throws TException {
            if (delegate == null) {
                throw new IllegalStateException();
            }
            delegate.write(t, protocol);
        }

        @Override
        public T read(TProtocol protocol) throws TException {
            if (delegate == null) {
                throw new IllegalStateException();
            }
            return delegate.read(protocol);
        }

        @Override
        public byte getTType() {
            if (delegate == null) {
                throw new IllegalStateException();
            }
            return delegate.getTType();
        }
    }
}
