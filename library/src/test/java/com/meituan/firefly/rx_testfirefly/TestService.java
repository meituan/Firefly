package com.meituan.firefly.rx_testfirefly;

import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.thrift.TException;
import rx.Observable;
import com.meituan.firefly.annotations.*;


public interface TestService {
    
    @Func(oneway = true, value = { })
    public Observable<Void> notify(@Field(id = 1, required = true, name = "id") Integer id) throws TException;
    
    @Func(oneway = false, value = { @Field(id = 1, required = false, name = "testException")})
    public Observable<UnionB> get(@Field(id = 1, required = false, name = "id") Integer id) throws TestException, TException;
    
    @Func(oneway = false, value = { @Field(id = 1, required = false, name = "testException")})
    public Observable<UnionB> emptyArgMethod() throws TestException, TException;
    
    @Func(oneway = false, value = { })
    public Observable<List<UnionB>> getList(@Field(id = 1, required = false, name = "ids") List<Short> ids) throws TException;
    
    @Func(oneway = false, value = { @Field(id = 1, required = false, name = "testException")})
    public Observable<Void> notifyWithoutOneway(@Field(id = 1, required = true, name = "id") Integer id) throws TestException, TException;
}
