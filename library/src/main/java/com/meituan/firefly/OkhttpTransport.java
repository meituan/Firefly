package com.meituan.firefly;

import com.squareup.okhttp.*;
import org.apache.thrift.TByteArrayOutputStream;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

import java.io.IOException;

/**
 * Http Thrift Transport implementation, using Okhttp
 */
public class OkhttpTransport extends TTransport {
    public static final MediaType CONTENT_TYPE = MediaType.parse("application/x-thrift");
    private final String url;
    private final OkHttpClient client;
    private byte[] readBuffer;
    private final TByteArrayOutputStream writeBuffer = new TByteArrayOutputStream();
    private volatile boolean used;
    private int readBufferPosition;

    public OkhttpTransport(String url, OkHttpClient client) {
        this.url = url;
        this.client = client;
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public void open() throws TTransportException {
    }

    @Override
    public void close() {
    }

    @Override
    public int read(byte[] buf, int off, int len) throws TTransportException {
        int bytesRemaining = getBytesRemainingInBuffer();
        int amtToRead = (len > bytesRemaining ? bytesRemaining : len);
        if (amtToRead > 0) {
            System.arraycopy(readBuffer, readBufferPosition, buf, off, amtToRead);
            consumeBuffer(amtToRead);
        }
        return amtToRead;
    }

    @Override
    public void write(byte[] buf, int off, int len) throws TTransportException {
        writeBuffer.write(buf, off, len);
    }

    @Override
    public void flush() throws TTransportException {
        if (used) {
            throw new IllegalStateException("transport already used");
        }
        used = true;
        Request request = new Request.Builder().url(url).post(RequestBody.create(CONTENT_TYPE, writeBuffer.get(), 0, writeBuffer.len()))
                .addHeader("Accept", "application/x-thrift").build();
        try {
            Response response = client.newCall(request).execute();
            if (!response.isSuccessful()) {
                throw new TTransportException("response is not successful");
            }
            readBuffer = response.body().bytes();
        } catch (IOException e) {
            throw new TTransportException(e);
        }
    }

    @Override
    public byte[] getBuffer() {
        return readBuffer;
    }

    @Override
    public int getBufferPosition() {
        return readBufferPosition;
    }

    @Override
    public int getBytesRemainingInBuffer() {
        return readBuffer.length - readBufferPosition;
    }

    @Override
    public void consumeBuffer(int len) {
        readBufferPosition += len;
    }
}
