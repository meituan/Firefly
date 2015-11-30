package com.meituan.firefly.android;

import android.os.Parcel;

import com.meituan.firefly.android.test.TestEnum;
import com.meituan.firefly.android.test.TestStruct;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class ParcelableTest {
    @Test
    public void shouldSupportParcel() throws Exception {
        Parcel parcel = Parcel.obtain();
        TestStruct struct = new TestStruct();
        struct.a = 321;
        struct.e = TestEnum.A;
        struct.bin = new byte[]{123};
        struct.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);
        TestStruct readStruct = (TestStruct) TestStruct.CREATOR.createFromParcel(parcel);
        Assert.assertEquals(struct.a, readStruct.a);
        Assert.assertEquals(struct.e, readStruct.e);
        Assert.assertArrayEquals(struct.bin, readStruct.bin);
    }

    @Test
    public void shouldSupportParcelArray() throws Exception {
        Parcel parcel = Parcel.obtain();
        TestStruct struct = new TestStruct();
        struct.a = 321;
        struct.e = TestEnum.A;
        struct.bin = new byte[]{123};
        parcel.writeParcelableArray(new TestStruct[]{new TestStruct(), struct}, 0);
        parcel.setDataPosition(0);
        TestStruct readStruct = (TestStruct) parcel.readParcelableArray(getClass().getClassLoader())[1];
        Assert.assertEquals(struct.a, readStruct.a);
        Assert.assertEquals(struct.e, readStruct.e);
        Assert.assertArrayEquals(struct.bin, readStruct.bin);
    }
}
