package com.meituan.firefly;

import com.meituan.firefly.testthrift.OrderedStruct;
import com.meituan.firefly.testthrift.TestException;
import com.meituan.firefly.testthrift.TestService;
import com.meituan.firefly.testthrift.TestService.Iface;
import com.meituan.firefly.testthrift.UnionB;
import org.apache.thrift.TApplicationException;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;
import org.assertj.core.api.Assertions;
import org.junit.Assert;
import org.junit.Test;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.schedulers.TestScheduler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class FunctionCallTest {

    Thrift thrift = Thrift.instance;


    @Test
    public void shouldReceiveOnewayMethod() throws Exception {
        final TTransport transport = new FlushableMemoryBuffer(4096);
        com.meituan.firefly.testfirefly.TestService testService = thrift.create(com.meituan.firefly.testfirefly.TestService.class, new Thrift.SimpleTProtocolFactory() {
            @Override
            public TProtocol get() {
                return new TBinaryProtocol(transport);
            }
        });
        NotifyCheck notifyCheck = new NotifyCheck(100);
        TestService.Processor<Iface> processor = new TestService.Processor<Iface>(notifyCheck);
        testService.notify(100);
        processor.process(new TBinaryProtocol(transport), new TBinaryProtocol(transport));
        Assert.assertTrue(notifyCheck.notified);
    }

    @Test
    public void shouldReceiveObserableWithOneway() throws Exception {
        NotifyCheck notifyCheck = new NotifyCheck(100);
        final TestService.Processor<Iface> processor = new TestService.Processor<Iface>(notifyCheck);
        final TTransport transport = new FlushableMemoryBuffer(4096) {
            boolean flushed = false;

            @Override
            public void flush() throws TTransportException {
                if (!flushed) {
                    flushed = true;
                    try {
                        processor.process(new TBinaryProtocol(this), new TBinaryProtocol(this));
                    } catch (TException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };
        com.meituan.firefly.rx_testfirefly.TestService testService = thrift.create(com.meituan.firefly.rx_testfirefly.TestService.class, new Thrift.SimpleTProtocolFactory() {
            @Override
            public TProtocol get() {
                return new TBinaryProtocol(transport);
            }
        });
        TestSubscriber testSubscriber = new TestSubscriber();
        testService.notify(100).subscribe(testSubscriber);
        Assert.assertTrue(notifyCheck.notified);

    }

    @Test
    public void voidMethodWithoutOneway() throws Exception {
        NotifyCheck notifyCheck = new NotifyCheck(100);
        final TestService.Processor<Iface> processor = new TestService.Processor<Iface>(notifyCheck);
        final TTransport transport = new FlushableMemoryBuffer(4096) {
            boolean flushed = false;

            @Override
            public void flush() throws TTransportException {
                if (!flushed) {
                    flushed = true;
                    try {
                        processor.process(new TBinaryProtocol(this), new TBinaryProtocol(this));
                    } catch (TException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };
        com.meituan.firefly.testfirefly.TestService testService = thrift.create(com.meituan.firefly.testfirefly.TestService.class, new Thrift.SimpleTProtocolFactory() {
            @Override
            public TProtocol get() {
                return new TBinaryProtocol(transport);
            }
        });
        testService.notifyWithoutOneway(100);
        Assert.assertTrue(notifyCheck.notified);
    }

    @Test
    public void voidMethodReceiveObserableWithoutOneway() throws Exception {
        NotifyCheck notifyCheck = new NotifyCheck(100);
        final TestService.Processor<Iface> processor = new TestService.Processor<Iface>(notifyCheck);
        final TTransport transport = new FlushableMemoryBuffer(4096) {
            boolean flushed = false;

            @Override
            public void flush() throws TTransportException {
                if (!flushed) {
                    flushed = true;
                    try {
                        processor.process(new TBinaryProtocol(this), new TBinaryProtocol(this));
                    } catch (TException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };
        com.meituan.firefly.rx_testfirefly.TestService testService = thrift.create(com.meituan.firefly.rx_testfirefly.TestService.class, new Thrift.SimpleTProtocolFactory() {
            @Override
            public TProtocol get() {
                return new TBinaryProtocol(transport);
            }
        });
        TestSubscriber testSubscriber = new TestSubscriber();
        testService.notifyWithoutOneway(100).subscribe(testSubscriber);
        Assert.assertTrue(notifyCheck.notified);

    }

    @Test
    public void shouldReceiveResult() throws Exception {
        TTransport transport = new FlushableMemoryBuffer(4096);
        TestService.Processor<Iface> processor = new TestService.Processor<Iface>(new Iface() {
            @Override
            public void notify(int id) throws TException {

            }

            @Override
            public UnionB get(int id) throws TestException, TException {
                return new UnionB(UnionB._Fields.OS, new com.meituan.firefly.testthrift.OrderedStruct(1));
            }

            @Override
            public UnionB emptyArgMethod() throws TestException, TException {
                return null;
            }

            @Override
            public List<UnionB> getList(List<Short> ids) throws TException {
                return null;
            }

            @Override
            public void notifyWithoutOneway(int id) throws TestException, TException {

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

    @Test
    public void shouldReceiveObserable() throws Exception {
        final TestService.Processor<Iface> processor = new TestService.Processor<Iface>(new Iface() {
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

            @Override
            public void notifyWithoutOneway(int id) throws TestException, TException {

            }

            @Override
            public UnionB emptyArgMethod() throws TestException, TException {
                return null;
            }

        });
        TTransport transport = new FlushableMemoryBuffer(4096) {
            boolean flushed = false;

            @Override
            public void flush() throws TTransportException {
                if (!flushed) {
                    flushed = true;
                    try {
                        processor.process(new TBinaryProtocol(this), new TBinaryProtocol(this));
                    } catch (TException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };
        TestScheduler testScheduler = new TestScheduler();
        FunctionCall functionCall = new FunctionCall(com.meituan.firefly.rx_testfirefly.TestService.class.getMethod("get", Integer.class), thrift);
        Observable<com.meituan.firefly.rx_testfirefly.UnionB> observable;
        try {
            observable = (Observable<com.meituan.firefly.rx_testfirefly.UnionB>) functionCall.apply(new Object[]{1}, new TBinaryProtocol(transport), 1, testScheduler);
        } catch (ClassCastException e) {
            throw new ClassCastException("return type of method is not obserable !");
        }
        Assert.assertNotNull(observable);
        observable.subscribeOn(testScheduler);
        observable.observeOn(testScheduler);
        TestSubscriber<com.meituan.firefly.rx_testfirefly.UnionB> testSubscriber = new TestSubscriber();
        observable.subscribe(testSubscriber);
        testScheduler.triggerActions();
        Assert.assertEquals(1, testSubscriber.getOnNextEvents().size());
        com.meituan.firefly.rx_testfirefly.UnionB u = testSubscriber.getOnNextEvents().get(0);
        Assert.assertNotNull(u);
        Assert.assertEquals((Integer) 1, u.os.id);
    }

    @Test
    public void emptyArgMethod() throws Exception {
        final TestService.Processor<Iface> processor = new TestService.Processor<Iface>(new Iface() {
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

            @Override
            public void notifyWithoutOneway(int id) throws TestException, TException {

            }

            @Override
            public UnionB emptyArgMethod() throws TestException, TException {
                return new UnionB(UnionB._Fields.OS, new com.meituan.firefly.testthrift.OrderedStruct(1));
            }

        });
        TTransport transport = new FlushableMemoryBuffer(4096) {
            boolean flushed = false;

            @Override
            public void flush() throws TTransportException {
                if (!flushed) {
                    flushed = true;
                    try {
                        processor.process(new TBinaryProtocol(this), new TBinaryProtocol(this));
                    } catch (TException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };
        TestScheduler testScheduler = new TestScheduler();
        FunctionCall functionCall = new FunctionCall(com.meituan.firefly.rx_testfirefly.TestService.class.getMethod("emptyArgMethod", null), thrift);
        Observable<com.meituan.firefly.rx_testfirefly.UnionB> observable;
        try {
            observable = (Observable<com.meituan.firefly.rx_testfirefly.UnionB>) functionCall.apply(null, new TBinaryProtocol(transport), 1, testScheduler);
        } catch (ClassCastException e) {
            throw new ClassCastException("return type of method is not obserable !");
        }
        Assert.assertNotNull(observable);
        observable.subscribeOn(testScheduler);
        observable.observeOn(testScheduler);
        TestSubscriber<com.meituan.firefly.rx_testfirefly.UnionB> testSubscriber = new TestSubscriber();
        observable.subscribe(testSubscriber);
        testScheduler.triggerActions();
        Assert.assertEquals(1, testSubscriber.getOnNextEvents().size());
        com.meituan.firefly.rx_testfirefly.UnionB u = testSubscriber.getOnNextEvents().get(0);
        Assert.assertNotNull(u);
        Assert.assertEquals((Integer) 1, u.os.id);
    }
    @Test
    public void emptyArgMethodReceiveObserable() throws Exception {
        final TestService.Processor<Iface> processor = new TestService.Processor<Iface>(new Iface() {
            @Override
            public void notify(int id) throws TException {

            }

            @Override
            public UnionB get(int id) throws TestException, TException {
                return null;
            }

            @Override
            public List<UnionB> getList(List<Short> ids) throws TException {
                return null;
            }

            @Override
            public void notifyWithoutOneway(int id) throws TestException, TException {

            }

            @Override
            public UnionB emptyArgMethod() throws TestException, TException {
                return  new UnionB(UnionB._Fields.OS, new com.meituan.firefly.testthrift.OrderedStruct(1));
            }

        });
        TTransport transport = new FlushableMemoryBuffer(4096) {
            boolean flushed = false;

            @Override
            public void flush() throws TTransportException {
                if (!flushed) {
                    flushed = true;
                    try {
                        processor.process(new TBinaryProtocol(this), new TBinaryProtocol(this));
                    } catch (TException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        };
        TestScheduler testScheduler = new TestScheduler();
        FunctionCall functionCall = new FunctionCall(com.meituan.firefly.rx_testfirefly.TestService.class.getMethod("emptyArgMethod", null), thrift);
        Observable<com.meituan.firefly.rx_testfirefly.UnionB> observable;
        try {
            observable = (Observable<com.meituan.firefly.rx_testfirefly.UnionB>) functionCall.apply(null, new TBinaryProtocol(transport), 1, testScheduler);
        } catch (ClassCastException e) {
            throw new ClassCastException("return type of method is not obserable !");
        }
        Assert.assertNotNull(observable);
        observable.subscribeOn(testScheduler);
        observable.observeOn(testScheduler);
        TestSubscriber<com.meituan.firefly.rx_testfirefly.UnionB> testSubscriber = new TestSubscriber();
        observable.subscribe(testSubscriber);
        testScheduler.triggerActions();
        Assert.assertEquals(1, testSubscriber.getOnNextEvents().size());
        com.meituan.firefly.rx_testfirefly.UnionB u = testSubscriber.getOnNextEvents().get(0);
        Assert.assertNotNull(u);
        Assert.assertEquals((Integer) 1, u.os.id);
    }
    @Test(expected = com.meituan.firefly.testfirefly.TestException.class)
    public void shouldReceiveException() throws Exception {
        TTransport transport = new FlushableMemoryBuffer(4096);
        TestService.Processor<Iface> processor = new TestService.Processor<Iface>(new Iface() {
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

            @Override
            public void notifyWithoutOneway(int id) throws TestException, TException {

            }

            @Override
            public UnionB emptyArgMethod() throws TestException, TException {
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
        TestService.Processor<Iface> processor = new TestService.Processor<Iface>(new Iface() {
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

            @Override
            public void notifyWithoutOneway(int id) throws TestException, TException {

            }

            @Override
            public UnionB emptyArgMethod() throws TestException, TException {
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
        TestService.Processor<Iface> processor = new TestService.Processor<Iface>(new Iface() {
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

            @Override
            public void notifyWithoutOneway(int id) throws TestException, TException {

            }

            @Override
            public UnionB emptyArgMethod() throws TestException, TException {
                return null;
            }

        });
        FunctionCall functionCall = new FunctionCall(com.meituan.firefly.testfirefly.TestService.class.getMethod("getList", List.class), thrift);
        functionCall.send(new Object[]{Arrays.asList((short) 1, (short) 2, (short) 3)}, new TBinaryProtocol(transport), 1);
        processor.process(new TBinaryProtocol(transport), new TBinaryProtocol(transport));
        List<com.meituan.firefly.testfirefly.UnionB> lu = (List<com.meituan.firefly.testfirefly.UnionB>) functionCall.recv(new TBinaryProtocol(transport), 1);
        Assertions.assertThat(lu).hasSize(3).hasOnlyElementsOfType(com.meituan.firefly.testfirefly.UnionB.class);
    }

}
