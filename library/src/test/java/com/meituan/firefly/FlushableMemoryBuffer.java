package com.meituan.firefly;

import org.apache.thrift.transport.TMemoryBuffer;
import org.apache.thrift.transport.TTransportException;

class FlushableMemoryBuffer extends TMemoryBuffer {
    public FlushableMemoryBuffer(int size) {
        super(size);
    }

    @Override
    public void flush() throws TTransportException {
    }
}
