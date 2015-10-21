package com.meituan.firefly.rx_testfirefly;

import java.util.List;
import java.util.Map;
import java.util.Set;
import com.meituan.firefly.annotations.*;


public class TestException extends Exception{
    
    @Field(id = -1, required = false, name = "message") public String message;
}