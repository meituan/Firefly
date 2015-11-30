package com.meituan.firefly.testfirefly;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.meituan.firefly.annotations.*;


public class TestException extends Exception {
        
    @Field(id = -1, required = false, name = "message")
    public String message;
}