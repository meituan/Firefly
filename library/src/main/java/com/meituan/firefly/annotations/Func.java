package com.meituan.firefly.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by ponyets on 15/6/8.
 */
@Retention(RUNTIME)
@Target(METHOD)
public @interface Func {
    boolean oneway() default false;

    Field[] value() default {};
}
