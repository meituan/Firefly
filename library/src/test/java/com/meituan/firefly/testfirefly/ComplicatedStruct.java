package com.meituan.firefly.testfirefly;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.meituan.firefly.annotations.*;


public class ComplicatedStruct implements Serializable {
        
    @Field(id = -1, required = false, name = "a")
    public Integer a= 12345;
    
    @Field(id = -2, required = false, name = "b")
    public Long b= 100l;
    
    @Field(id = -3, required = false, name = "c")
    public Double c= 99.99;
    
    @Field(id = -4, required = false, name = "shortSet")
    public Set<Short> shortSet= new java.util.HashSet(java.util.Arrays.asList((short) 1, (short) 2, (short) 3));
    
    @Field(id = -5, required = false, name = "intSet")
    public Set<Integer> intSet= new java.util.HashSet(java.util.Arrays.asList(4, 5, 6));
    
    @Field(id = -6, required = false, name = "mixStructlist")
    public List<MixStruct> mixStructlist;
    
    @Field(id = -7, required = false, name = "shortList")
    public List<Short> shortList;
    
    @Field(id = -8, required = false, name = "orderedStructMap")
    public Map<Short, OrderedStruct> orderedStructMap;
    
    @Field(id = -9, required = false, name = "mixStructMap")
    public Map<Short, MixStruct> mixStructMap;
    
    @Field(id = -10, required = false, name = "orderEnum")
    public OrderEnum orderEnum;
    
    @Field(id = -11, required = false, name = "bin")
    public byte[] bin;
}