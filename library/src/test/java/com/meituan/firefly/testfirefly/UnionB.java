package com.meituan.firefly.testfirefly;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.meituan.firefly.annotations.*;


@Union
public class UnionB implements Serializable {
        
    @Field(id = 1, required = false, name = "os")
    public OrderedStruct os;
    
    @Field(id = 2, required = false, name = "uos")
    public UnorderedStruct uos;
    
    @Field(id = 3, required = false, name = "mos")
    public MixStruct mos;
}