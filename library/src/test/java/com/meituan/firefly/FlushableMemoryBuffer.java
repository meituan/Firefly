package com.meituan.firefly;

import org.apache.thrift.transport.TMemoryBuffer;
import org.apache.thrift.transport.TTransportException;

/**
 * Created by ponyets on 15/6/30.
 */
class FlushableMemoryBuffer extends TMemoryBuffer {
    public FlushableMemoryBuffer(int size) {
        super(size);
    }

    @Override
    public void flush() throws TTransportException {
    }
}
