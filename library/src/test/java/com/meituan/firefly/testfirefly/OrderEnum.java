package com.meituan.firefly.testfirefly;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


public enum OrderEnum {
    Order(1),UnOrder(2),Mix(3);
    private final static Map<Integer, OrderEnum> map = new HashMap<Integer, OrderEnum>();
    static {
        map.put(1, Order);
        map.put(2, UnOrder);
        map.put(3, Mix);
        }
    private final int value;
    private OrderEnum (int value) {
        this.value = value;
    }

    public int getValue(){ return value; }

    public static OrderEnum findByValue(int value) { return map.get(value); }
}