package com.meituan.firefly.android.test;

import android.os.Parcelable;

import com.meituan.firefly.android.StructBase;
import com.meituan.firefly.annotations.*;


public class TestStruct extends StructBase {
    public static final Parcelable.Creator CREATOR = new Creator(TestStruct.class);
        
    @Field(id = -1, required = false, name = "a")
    public Integer a= 123456789;
    
    @Field(id = -2, required = false, name = "b")
    public Boolean b;
    
    @Field(id = -3, required = false, name = "c")
    public Long c;
    
    @Field(id = -4, required = false, name = "e")
    public TestEnum e= TestEnum.B;
    
    @Field(id = -5, required = false, name = "bin")
    public byte[] bin;
    
    @Field(id = -6, required = false, name = "str")
    public String str= "abcde";
    
    @Field(id = -7, required = false, name = "ss")
    public String ss= "fghij";
}