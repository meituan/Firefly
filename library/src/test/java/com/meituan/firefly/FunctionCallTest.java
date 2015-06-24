package com.meituan.firefly;

import com.meituan.firefly.testthrift.OrderedStruct;
import com.meituan.firefly.testthrift.TestException;
import com.meituan.firefly.testthrift.TestService;
import com.meituan.firefly.testthrift.UnionB;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TMemoryBuffer;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by ponyets on 15/6/24.
 */
public class FunctionCallTest {
    static class FlushableMemoryBuffer extends TMemoryBuffer {
        public FlushableMemoryBuffer(int size) {
            super(size);
        }

        @Override
        public void flush() throws TTransportException {
        }
    }

    Thrift thrift = new Thrift();


    static class NotifyCheck implements TestService.Iface {
        final int idToCheck;
        boolean notified;

        public NotifyCheck(int idToCheck) {
            this.idToCheck = idToCheck;
        }

        @Override
        public void notify(int id) throws TException {
            if (id == idToCheck) {
                notified = true;
            }
        }

        @Override
        public UnionB get(int id) throws TException {
            return null;
        }

        @Override
        public List<UnionB> getList(List<Short> ids) throws TException {
            return null;
        }
    }

    @Test
    public void shouldReceiveOnewayMethod() throws Exception {
        TTransport transport = new FlushableMemoryBuffer(4096);
        com.meituan.firefly.testfirefly.TestService testService = thrift.create(com.meituan.firefly.testfirefly.TestService.class, new Thrift.TProtocolFactory() {
            @Override
            public TProtocol get() {
                return new TBinaryProtocol(transport);
            }
        });
        NotifyCheck notifyCheck = new NotifyCheck(100);
        TestService.Processor<TestService.Iface> processor = new TestService.Processor<>(notifyCheck);
        testService.notify(100);
        processor.process(new TBinaryProtocol(transport), new TBinaryProtocol(transport));
        Assert.assertTrue(notifyCheck.notified);
    }

    @Test
    public void shouldReceiveResult() throws Exception {
        TTransport transport = new FlushableMemoryBuffer(4096);
        TestService.Processor<TestService.Iface> processor = new TestService.Processor<>(new TestService.Iface() {
            @Override
            public void notify(int id) throws TException {

            }

            @Override
            public UnionB get(int id) throws TestException, TException {
                return new UnionB(UnionB._Fields.OS, new com.meituan.firefly.testthrift.OrderedStruct(1));
            }

            @Override
            public List<UnionB> getList(List<Short> ids) throws TException {
                return null;
            }
        });
        FunctionCall functionCall = new FunctionCall(com.meituan.firefly.testfirefly.TestService.class.getMethod("get", Integer.class), thrift);
        functionCall.send(new Object[]{1}, new TBinaryProtocol(transport), 1);
        processor.process(new TBinaryProtocol(transport), new TBinaryProtocol(transport));
        com.meituan.firefly.testfirefly.UnionB u = (com.meituan.firefly.testfirefly.UnionB) functionCall.recv(new TBinaryProtocol(transport), 1);
        Assert.assertNotNull(u);
        Assert.assertNotNull(u.os);
        Assert.assertEquals((Integer) 1, u.os.id);
    }

    @Test(expected = com.meituan.firefly.testfirefly.TestException.class)
    public void shouldReceiveException() throws Exception {
        TTransport transport = new FlushableMemoryBuffer(4096);
        TestService.Processor<TestService.Iface> processor = new TestService.Processor<>(new TestService.Iface() {
            @Override
            public void notify(int id) throws TException {

            }

            @Override
            public UnionB get(int id) throws TestException, TException {
                throw new TestException("aaa");
            }

            @Override
            public List<UnionB> getList(List<Short> ids) throws TException {
                return null;
            }
        });
        FunctionCall functionCall = new FunctionCall(com.meituan.firefly.testfirefly.TestService.class.getMethod("get", Integer.class), thrift);
        functionCall.send(new Object[]{1}, new TBinaryProtocol(transport), 1);
        processor.process(new TBinaryProtocol(transport), new TBinaryProtocol(transport));
        try {
            functionCall.recv(new TBinaryProtocol(transport), 1);
        } catch (com.meituan.firefly.testfirefly.TestException e) {
            Assert.assertEquals("aaa", e.message);
            throw e;
        }
    }

    @Test(expected = TApplicationException.class)
    public void shouldReceiveApplicationException() throws Exception {
        TTransport transport = new FlushableMemoryBuffer(4096);
        TestService.Processor<TestService.Iface> processor = new TestService.Processor<>(new TestService.Iface() {
            @Override
            public void notify(int id) throws TException {

            }

            @Override
            public UnionB get(int id) throws TestException, TException {
                throw new TException("aaa");
            }

            @Override
            public List<UnionB> getList(List<Short> ids) throws TException {
                return null;
            }
        });
        FunctionCall functionCall = new FunctionCall(com.meituan.firefly.testfirefly.TestService.class.getMethod("get", Integer.class), thrift);
        functionCall.send(new Object[]{1}, new TBinaryProtocol(transport), 1);
        processor.process(new TBinaryProtocol(transport), new TBinaryProtocol(transport));
        functionCall.recv(new TBinaryProtocol(transport), 1);
    }

    @Test
    public void complicatedMethod() throws Exception {
        TTransport transport = new FlushableMemoryBuffer(4096);
        TestService.Processor<TestService.Iface> processor = new TestService.Processor<>(new TestService.Iface() {
            @Override
            public void notify(int id) throws TException {

            }

            @Override
            public UnionB get(int id) throws TestException, TException {
                return null;
            }

            @Override
            public List<UnionB> getList(List<Short> ids) throws TException {
                ArrayList list = new ArrayList();
                for (Short id : ids) {
                    list.add(new UnionB(UnionB._Fields.OS, new OrderedStruct(id)));
                }
                return list;
            }
        });
        FunctionCall functionCall = new FunctionCall(com.meituan.firefly.testfirefly.TestService.class.getMethod("getList", List.class), thrift);
        functionCall.send(new Object[]{Arrays.asList((short) 1, (short) 2, (short) 3)}, new TBinaryProtocol(transport), 1);
        processor.process(new TBinaryProtocol(transport), new TBinaryProtocol(transport));
        List<com.meituan.firefly.testfirefly.UnionB> lu = (List<com.meituan.firefly.testfirefly.UnionB>) functionCall.recv(new TBinaryProtocol(transport), 1);
        Assertions.assertThat(lu).hasSize(3).hasOnlyElementsOfType(com.meituan.firefly.testfirefly.UnionB.class);
    }
}
