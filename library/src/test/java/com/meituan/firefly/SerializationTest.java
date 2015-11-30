package com.meituan.firefly;

import com.meituan.firefly.testfirefly.ComplicatedStruct;
import com.meituan.firefly.testfirefly.MixStruct;
import com.meituan.firefly.testfirefly.OrderEnum;
import com.meituan.firefly.testfirefly.OrderedStruct;
import org.assertj.core.api.Condition;
import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class SerializationTest {
    @Test
    public void shouldSupportSerialization() throws Exception {
        ComplicatedStruct complicatedStruct = new ComplicatedStruct();
        assertThat(complicatedStruct.a).isEqualTo(12345);
        assertThat(complicatedStruct.b).isEqualTo(100l);
        assertThat(complicatedStruct.c).isEqualTo(99.99);
        complicatedStruct.a = 54321;
        complicatedStruct.shortSet = new HashSet<>(Arrays.asList((short) 10, (short) 20));
        complicatedStruct.intSet = new HashSet<>(Arrays.asList(30, 40));
        complicatedStruct.shortList = Arrays.asList((short) 100, (short) 101);
        MixStruct mixStruct1 = new MixStruct();
        mixStruct1.id = 1;
        mixStruct1.uid = 2;
        complicatedStruct.mixStructlist = Arrays.asList(mixStruct1);
        OrderedStruct orderedStruct1 = new OrderedStruct();
        orderedStruct1.id = 1;
        Map orderedStructMap = new HashMap<>();
        orderedStructMap.put((short) 1, orderedStruct1);
        complicatedStruct.orderedStructMap = orderedStructMap;
        MixStruct mixStruct2 = new MixStruct();
        mixStruct2.id = 1;
        mixStruct2.uid = 3;
        Map mixStructMap = new HashMap<>();
        mixStructMap.put((short) 1, mixStruct2);
        complicatedStruct.mixStructMap = mixStructMap;
        complicatedStruct.orderEnum = OrderEnum.Mix;
        complicatedStruct.bin = new byte[]{1, 2, 3};

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream outputStream = new ObjectOutputStream(byteArrayOutputStream);
        outputStream.writeObject(complicatedStruct);
        ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(byteArrayOutputStream.toByteArray()));
        ComplicatedStruct deserializedComplicatedStruct = (ComplicatedStruct) inputStream.readObject();

        assertThat(deserializedComplicatedStruct).isNotNull();
        assertThat(deserializedComplicatedStruct.a).isEqualTo(54321);
        assertThat(deserializedComplicatedStruct.b).isEqualTo(100l);
        assertThat(deserializedComplicatedStruct.c).isEqualTo(99.99);
        assertThat(deserializedComplicatedStruct.shortSet).containsOnly((short) 10, (short) 20);
        assertThat(deserializedComplicatedStruct.intSet).containsOnly(30, 40);
        assertThat(deserializedComplicatedStruct.shortList).containsOnly((short) 100, (short) 101);
        assertThat(deserializedComplicatedStruct.mixStructlist).hasSize(1).hasOnlyElementsOfType(MixStruct.class);
        assertThat(deserializedComplicatedStruct.orderedStructMap).hasSize(1).containsOnlyKeys((short) 1);
        assertThat(deserializedComplicatedStruct.orderedStructMap.get((short) 1)).isExactlyInstanceOf(OrderedStruct.class).is(new Condition<OrderedStruct>() {
            @Override
            public boolean matches(OrderedStruct value) {
                return value.id == 1;
            }
        });
        assertThat(deserializedComplicatedStruct.mixStructMap).hasSize(1).containsOnlyKeys((short) 1);
        assertThat(deserializedComplicatedStruct.mixStructMap.get((short) 1)).isExactlyInstanceOf(MixStruct.class).is(new Condition<MixStruct>() {
            @Override
            public boolean matches(MixStruct value) {
                return value.id == 1 && value.uid == 3;
            }
        });
        assertThat(deserializedComplicatedStruct.orderEnum).isEqualTo(OrderEnum.Mix);
        assertThat(deserializedComplicatedStruct.bin).isEqualTo(new byte[]{1, 2, 3});
    }
}
