package com.meituan.firefly;

import org.apache.thrift.protocol.TProtocol;

import java.lang.reflect.Method;

/**
 * Created by ponyets on 15/6/30.
 */
public interface Processor {
    Object process(Method method, Object[] args, TProtocol protocol, int seqId) throws Throwable;
}
