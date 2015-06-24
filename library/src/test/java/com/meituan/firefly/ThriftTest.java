package com.meituan.firefly;

import com.meituan.firefly.testfirefly.OrderedStruct;
import com.meituan.firefly.testfirefly.TestService;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TMemoryBuffer;
import org.apache.thrift.transport.TMemoryInputTransport;
import org.assertj.core.api.Condition;
import org.junit.Test;
import static org.assertj.core.api.Assertions.*;

/**
 * Created by ponyets on 15/6/23.
 */
public class ThriftTest {
    Thrift thrift = new Thrift();
    @Test
    public void shouldCreateService() {
        TestService service = thrift.create(TestService.class, new Thrift.TProtocolFactory() {
            @Override
            public TProtocol get() {
                return new TBinaryProtocol(new TMemoryInputTransport(new byte[]{}));
            }
        });
        assertThat(service).isNotNull();
    }

}
