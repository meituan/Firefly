package com.meituan.firefly.android.test;

import java.util.HashMap;
import java.util.Map;


public enum TestEnum {
    B(2),A(1111111111);
    private final static Map<Integer, TestEnum> map = new HashMap<Integer, TestEnum>();
    static {
        map.put(2, B);
        map.put(1111111111, A);
        }
    private final int value;
    private TestEnum (int value) {
        this.value = value;
    }

    public int getValue(){ return value; }

    public static TestEnum findByValue(int value) { return map.get(value); }
}