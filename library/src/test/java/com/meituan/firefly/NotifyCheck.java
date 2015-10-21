package com.meituan.firefly;

import com.meituan.firefly.testthrift.TestException;
import com.meituan.firefly.testthrift.UnionB;
import org.apache.thrift.TException;

import java.util.List;

class NotifyCheck implements com.meituan.firefly.testthrift.TestService.Iface {
    final int idToCheck;
    boolean notified;

    public NotifyCheck(int idToCheck) {
        this.idToCheck = idToCheck;
    }

    @Override
    public void notify(int id) throws TException {
        if (id == idToCheck) {
            notified = true;
        }
    }

    @Override
    public UnionB get(int id) throws TException {
        return null;
    }

    @Override
    public List<UnionB> getList(List<Short> ids) throws TException {
        return null;
    }

    @Override
    public UnionB obserableMethod(int id) throws TestException, TException {
        return null;
    }
}
