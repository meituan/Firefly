package com.meituan.firefly.testfirefly;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.thrift.TException;

import com.meituan.firefly.annotations.*;
import rx.Observable;


public interface TestService {
    
    @Func(oneway = true, value = { })
    public void notify (@Field(id = 1, required = true, name = "id") Integer id) throws TException;
    
    @Func(oneway = false, value = { @Field(id = -1, required = false, name = "testException")})
    public UnionB get (@Field(id = -1, required = false, name = "id") Integer id) throws TestException, TException;
    
    @Func(oneway = false, value = { })
    public List<UnionB> getList (@Field(id = -1, required = false, name = "ids") List<Short> ids) throws TException;
    
    @Func(oneway = false, value = { @Field(id = -1, required = false, name = "testException")})
    public UnionB obserableMethod (@Field(id = -1, required = true, name = "id") Integer id) throws TestException, TException;
}
