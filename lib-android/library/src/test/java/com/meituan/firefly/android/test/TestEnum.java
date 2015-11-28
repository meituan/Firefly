package com.meituan.firefly.android.test;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public enum TestEnum {
    A(1),B(2);
    private final static Map<Integer, TestEnum> map = new HashMap<Integer, TestEnum>();
    static {
        map.put(1, A);
        map.put(2, B);
        }
    private final int value;
    private TestEnum (int value) {
        this.value = value;
    }

    public int getValue(){ return value; }

    public static TestEnum findByValue(int value) { return map.get(value); }
}