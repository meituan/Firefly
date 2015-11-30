package com.meituan.firefly.android.test;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import android.os.Parcel;
import android.os.Parcelable;
import com.meituan.firefly.android.StructBase;
import com.meituan.firefly.annotations.*;


public class TestStruct extends StructBase {
    public static final Parcelable.Creator CREATOR = new Creator(TestStruct.class);
        
    @Field(id = -1, required = false, name = "a")
    public Integer a= 123;
    
    @Field(id = -2, required = false, name = "e")
    public TestEnum e= TestEnum.B;
    
    @Field(id = -3, required = false, name = "bin")
    public byte[] bin;
}