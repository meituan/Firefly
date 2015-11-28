package com.meituan.firefly.adapters;

import com.meituan.firefly.Thrift;
import com.meituan.firefly.testfirefly.*;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.transport.TMemoryBuffer;
import org.assertj.core.api.Condition;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class StructTypeAdapterTest {
    Thrift thrift = Thrift.instance;

    @Test
    public void shouldReadStruct_thatThriftWrite() throws Exception {
        TMemoryBuffer transport = new TMemoryBuffer(1024);
        TBinaryProtocol protocol = new TBinaryProtocol(transport);
        com.meituan.firefly.testthrift.OrderedStruct thriftOrderedStruct = new com.meituan.firefly.testthrift.OrderedStruct(99);
        thriftOrderedStruct.write(protocol);

        StructTypeAdapterFactory.StructTypeAdapter structTypeAdapter = new StructTypeAdapterFactory.StructTypeAdapter(OrderedStruct.class, thrift);
        OrderedStruct fireflyOrderedStruct = (OrderedStruct) structTypeAdapter.read(protocol);
        assertThat(fireflyOrderedStruct).isNotNull();
        assertThat(fireflyOrderedStruct.id).isEqualTo(99);
    }

    @Test
    public void shouldWriteStruct_thatThriftRead() throws Exception {
        TMemoryBuffer transport = new TMemoryBuffer(1024);
        TBinaryProtocol protocol = new TBinaryProtocol(transport);
        OrderedStruct fireflyOrderedStruct = new OrderedStruct();
        fireflyOrderedStruct.id = 99;

        StructTypeAdapterFactory.StructTypeAdapter structTypeAdapter = new StructTypeAdapterFactory.StructTypeAdapter(OrderedStruct.class, thrift);
        structTypeAdapter.write(fireflyOrderedStruct, protocol);
        com.meituan.firefly.testthrift.OrderedStruct thriftOrderedStruct = new com.meituan.firefly.testthrift.OrderedStruct();
        thriftOrderedStruct.read(protocol);
        assertThat(thriftOrderedStruct.id).isEqualTo(99);
    }

    @Test
    public void shouldReadMixStruct_thatThriftWrite() throws Exception {
        TMemoryBuffer transport = new TMemoryBuffer(1024);
        TBinaryProtocol protocol = new TBinaryProtocol(transport);
        com.meituan.firefly.testthrift.MixStruct thriftMixStruct = new com.meituan.firefly.testthrift.MixStruct(1, 2);
        thriftMixStruct.write(protocol);

        StructTypeAdapterFactory.StructTypeAdapter structTypeAdapter = new StructTypeAdapterFactory.StructTypeAdapter(MixStruct.class, thrift);
        MixStruct fireflyMixStruct = (MixStruct) structTypeAdapter.read(protocol);
        assertThat(fireflyMixStruct).isNotNull();
        assertThat(fireflyMixStruct.id).isEqualTo(1);
        assertThat(fireflyMixStruct.uid).isEqualTo(2);
    }

    @Test
    public void shouldWriteMixStruct_thatThriftRead() throws Exception {
        TMemoryBuffer transport = new TMemoryBuffer(1024);
        TBinaryProtocol protocol = new TBinaryProtocol(transport);
        MixStruct fireflyMixStruct = new MixStruct();
        fireflyMixStruct.id = 1;
        fireflyMixStruct.uid = 2;

        StructTypeAdapterFactory.StructTypeAdapter structTypeAdapter = new StructTypeAdapterFactory.StructTypeAdapter(MixStruct.class, thrift);
        structTypeAdapter.write(fireflyMixStruct, protocol);
        com.meituan.firefly.testthrift.MixStruct thriftMixStruct = new com.meituan.firefly.testthrift.MixStruct();
        thriftMixStruct.read(protocol);
        assertThat(thriftMixStruct.id).isEqualTo(1);
        assertThat(thriftMixStruct.uid).isEqualTo(2);
    }

    @Test
    public void shouldReadUnion_thatThriftWrite() throws Exception {
        TMemoryBuffer transport = new TMemoryBuffer(1024);
        TBinaryProtocol protocol = new TBinaryProtocol(transport);
        com.meituan.firefly.testthrift.OrderedStruct thriftOrderedStruct = new com.meituan.firefly.testthrift.OrderedStruct(99);
        com.meituan.firefly.testthrift.UnionB thriftUnionB = new com.meituan.firefly.testthrift.UnionB(com.meituan.firefly.testthrift.UnionB._Fields.OS, thriftOrderedStruct);
        thriftUnionB.write(protocol);

        StructTypeAdapterFactory.StructTypeAdapter structTypeAdapter = new StructTypeAdapterFactory.StructTypeAdapter(UnionB.class, thrift);
        UnionB fireflyUnionB = (UnionB) structTypeAdapter.read(protocol);
        assertThat(fireflyUnionB).isNotNull();
        assertThat(fireflyUnionB.os).isNotNull();
        assertThat(fireflyUnionB.os.id).isEqualTo(99);
    }

    @Test
    public void shouldWriteUnion_thatThriftRead() throws Exception {
        TMemoryBuffer transport = new TMemoryBuffer(1024);
        TBinaryProtocol protocol = new TBinaryProtocol(transport);
        UnionB fireflyUnionB = new UnionB();
        OrderedStruct fireflyOrderedStruct = new OrderedStruct();
        fireflyOrderedStruct.id = 99;
        fireflyUnionB.os = fireflyOrderedStruct;
        StructTypeAdapterFactory.StructTypeAdapter structTypeAdapter = new StructTypeAdapterFactory.StructTypeAdapter(UnionB.class, thrift);
        structTypeAdapter.write(fireflyUnionB, protocol);

        com.meituan.firefly.testthrift.UnionB thriftUnionB = new com.meituan.firefly.testthrift.UnionB();
        thriftUnionB.read(protocol);

        assertThat(thriftUnionB.getSetField()).isEqualTo(com.meituan.firefly.testthrift.UnionB._Fields.OS);
        assertThat(thriftUnionB.getOs()).isNotNull();
        assertThat(thriftUnionB.getOs().getId()).isEqualTo(99);
    }

    @Test
    public void shouldReadComplicatedStruct_thatThriftWrite() throws Exception {
        TMemoryBuffer transport = new TMemoryBuffer(4096);
        TBinaryProtocol protocol = new TBinaryProtocol(transport);
        com.meituan.firefly.testthrift.ComplicatedStruct thriftComplicatedStruct = new com.meituan.firefly.testthrift.ComplicatedStruct();
        thriftComplicatedStruct.setShortSet(new HashSet<>(Arrays.asList((short) 10, (short) 20)));
        thriftComplicatedStruct.setIntSet(new HashSet<>(Arrays.asList(30, 40)));
        thriftComplicatedStruct.setShortList(Arrays.asList((short) 100, (short) 101));
        thriftComplicatedStruct.setMixStructlist(Arrays.asList(new com.meituan.firefly.testthrift.MixStruct(1, 2)));
        Map orderedStructMap = new HashMap<>();
        orderedStructMap.put((short) 1, new com.meituan.firefly.testthrift.OrderedStruct(1));
        thriftComplicatedStruct.setOrderedStructMap(orderedStructMap);
        Map mixStructMap = new HashMap<>();
        mixStructMap.put((short) 1, new com.meituan.firefly.testthrift.MixStruct(1, 3));
        thriftComplicatedStruct.setMixStructMap(mixStructMap);
        thriftComplicatedStruct.setOrderEnum(com.meituan.firefly.testthrift.OrderEnum.Mix);
        thriftComplicatedStruct.setBin(new byte[]{1, 2, 3});
        thriftComplicatedStruct.write(protocol);

        StructTypeAdapterFactory.StructTypeAdapter structTypeAdapter = new StructTypeAdapterFactory.StructTypeAdapter(ComplicatedStruct.class, thrift);
        ComplicatedStruct fireflyComplicatedStruct = (ComplicatedStruct) structTypeAdapter.read(protocol);
        assertThat(fireflyComplicatedStruct).isNotNull();
        assertThat(fireflyComplicatedStruct.a).isEqualTo(12345);
        assertThat(fireflyComplicatedStruct.b).isEqualTo(100l);
        assertThat(fireflyComplicatedStruct.c).isEqualTo(99.99);
        assertThat(fireflyComplicatedStruct.shortSet).containsOnly((short) 10, (short) 20);
        assertThat(fireflyComplicatedStruct.intSet).containsOnly(30, 40);
        assertThat(fireflyComplicatedStruct.shortList).containsOnly((short) 100, (short) 101);
        assertThat(fireflyComplicatedStruct.mixStructlist).hasSize(1).hasOnlyElementsOfType(MixStruct.class);
        assertThat(fireflyComplicatedStruct.orderedStructMap).hasSize(1).containsOnlyKeys((short) 1);
        assertThat(fireflyComplicatedStruct.orderedStructMap.get((short) 1)).isExactlyInstanceOf(OrderedStruct.class).is(new Condition<OrderedStruct>() {
            @Override
            public boolean matches(OrderedStruct value) {
                return value.id == 1;
            }
        });
        assertThat(fireflyComplicatedStruct.mixStructMap).hasSize(1).containsOnlyKeys((short) 1);
        assertThat(fireflyComplicatedStruct.mixStructMap.get((short) 1)).isExactlyInstanceOf(MixStruct.class).is(new Condition<MixStruct>() {
            @Override
            public boolean matches(MixStruct value) {
                return value.id == 1 && value.uid == 3;
            }
        });
        assertThat(fireflyComplicatedStruct.orderEnum).isEqualTo(OrderEnum.Mix);
        assertThat(fireflyComplicatedStruct.bin).isEqualTo(new byte[]{1, 2, 3});
    }

    @Test
    public void shouldWriteComplicatedStruct_thatThriftRead() throws Exception {
        TMemoryBuffer transport = new TMemoryBuffer(4096);
        TBinaryProtocol protocol = new TBinaryProtocol(transport);
        ComplicatedStruct fireflyComplicatedStruct = new ComplicatedStruct();
        assertThat(fireflyComplicatedStruct.a).isEqualTo(12345);
        assertThat(fireflyComplicatedStruct.b).isEqualTo(100l);
        assertThat(fireflyComplicatedStruct.c).isEqualTo(99.99);

        fireflyComplicatedStruct.shortSet = new HashSet<>(Arrays.asList((short) 10, (short) 20));
        fireflyComplicatedStruct.intSet = new HashSet<>(Arrays.asList(30, 40));
        fireflyComplicatedStruct.shortList = Arrays.asList((short) 100, (short) 101);
        MixStruct mixStruct1 = new MixStruct();
        mixStruct1.id = 1;
        mixStruct1.uid = 2;
        fireflyComplicatedStruct.mixStructlist = Arrays.asList(mixStruct1);
        OrderedStruct orderedStruct1 = new OrderedStruct();
        orderedStruct1.id = 1;
        Map orderedStructMap = new HashMap<>();
        orderedStructMap.put((short) 1, orderedStruct1);
        fireflyComplicatedStruct.orderedStructMap = orderedStructMap;
        MixStruct mixStruct2 = new MixStruct();
        mixStruct2.id = 1;
        mixStruct2.uid = 3;
        Map mixStructMap = new HashMap<>();
        mixStructMap.put((short) 1, mixStruct2);
        fireflyComplicatedStruct.mixStructMap = mixStructMap;
        fireflyComplicatedStruct.orderEnum = OrderEnum.Mix;
        fireflyComplicatedStruct.bin = new byte[]{1, 2, 3};
        StructTypeAdapterFactory.StructTypeAdapter structTypeAdapter = new StructTypeAdapterFactory.StructTypeAdapter(ComplicatedStruct.class, thrift);
        structTypeAdapter.write(fireflyComplicatedStruct, protocol);

        com.meituan.firefly.testthrift.ComplicatedStruct thriftComplicatedStruct = new com.meituan.firefly.testthrift.ComplicatedStruct();
        thriftComplicatedStruct.read(protocol);
        assertThat(thriftComplicatedStruct.a).isEqualTo(12345);
        assertThat(thriftComplicatedStruct.b).isEqualTo(100l);
        assertThat(thriftComplicatedStruct.c).isEqualTo(99.99);
        assertThat(thriftComplicatedStruct.shortSet).containsOnly((short) 10, (short) 20);
        assertThat(thriftComplicatedStruct.intSet).containsOnly(30, 40);
        assertThat(thriftComplicatedStruct.shortList).containsOnly((short) 100, (short) 101);
        assertThat(thriftComplicatedStruct.mixStructlist).hasSize(1).hasOnlyElementsOfType(com.meituan.firefly.testthrift.MixStruct.class);
        assertThat(thriftComplicatedStruct.orderedStructMap).hasSize(1).containsOnlyKeys((short) 1);
        assertThat(thriftComplicatedStruct.orderedStructMap.get((short) 1)).isExactlyInstanceOf(com.meituan.firefly.testthrift.OrderedStruct.class).is(new Condition<com.meituan.firefly.testthrift.OrderedStruct>() {
            @Override
            public boolean matches(com.meituan.firefly.testthrift.OrderedStruct value) {
                return value.id == 1;
            }
        });
        assertThat(thriftComplicatedStruct.mixStructMap).hasSize(1).containsOnlyKeys((short) 1);
        assertThat(thriftComplicatedStruct.mixStructMap.get((short) 1)).isExactlyInstanceOf(com.meituan.firefly.testthrift.MixStruct.class).is(new Condition<com.meituan.firefly.testthrift.MixStruct>() {
            @Override
            public boolean matches(com.meituan.firefly.testthrift.MixStruct value) {
                return value.id == 1 && value.uid == 3;
            }
        });
        assertThat(thriftComplicatedStruct.orderEnum).isEqualTo(com.meituan.firefly.testthrift.OrderEnum.Mix);
        assertThat(thriftComplicatedStruct.bin.array()).isEqualTo(new byte[]{1, 2, 3});
    }
}
