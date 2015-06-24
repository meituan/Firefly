package com.meituan.firefly.annotations;

/**
 * Created by ponyets on 15/6/8.
 */
public class Test {
    @Func(oneway = false, value = {@Field(id = 0, required = false)})
    void test() {

    }

}
