package com.meituan.firefly.test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.meituan.firefly.annotations.*;


public class TestException extends Exception{
    
    @Field(id = -1, required = false) public String message;
}