package com.meituan.firefly;

import org.apache.thrift.protocol.TProtocol;

import java.lang.reflect.Method;

/**
 * Created by ponyets on 15/6/30.
 */
public interface Interceptor {
    Object intercept(Method method, Object[] args, TProtocol protocol, int seqId, Processor processor) throws Throwable;
}
