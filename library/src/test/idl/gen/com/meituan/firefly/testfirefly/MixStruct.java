package com.meituan.firefly.test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.meituan.firefly.annotations.*;


public class MixStruct {
    
    @Field(id = 1, required = true) public Integer id;
    
    @Field(id = -1, required = true) public Integer uid;
}