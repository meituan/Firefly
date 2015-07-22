package com.meituan.firefly;

import org.apache.thrift.protocol.TProtocol;

import java.lang.reflect.Method;

/**
 * Observes, modifies, and potentially short-circuits method calls to the service interface's dynamic proxy.
 * May be used to log calls, modify method arguments, or implement cache mechanism.
 */
public interface Interceptor {
    Object intercept(Method method, Object[] args, TProtocol protocol, int seqId, Processor processor) throws Throwable;
}
