package com.meituan.firefly.testfirefly;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.meituan.firefly.annotations.*;


public class ComplicatedStruct {
    
    @Field(id = -1, required = false) public Integer a= 12345;
    
    @Field(id = -2, required = false) public Long b= 100l;
    
    @Field(id = -3, required = false) public Double c= 99.99;
    
    @Field(id = -4, required = false) public Set<Short> shortSet= new java.util.HashSet(java.util.Arrays.asList((short) 1, (short) 2, (short) 3));
    
    @Field(id = -5, required = false) public Set<Integer> intSet= new java.util.HashSet(java.util.Arrays.asList(4, 5, 6));
    
    @Field(id = -6, required = false) public List<MixStruct> mixStructlist;
    
    @Field(id = -7, required = false) public List<Short> shortList;
    
    @Field(id = -8, required = false) public Map<Short, OrderedStruct> orderedStructMap;
    
    @Field(id = -9, required = false) public Map<Short, MixStruct> mixStructMap;
    
    @Field(id = -10, required = false) public OrderEnum orderEnum;
}