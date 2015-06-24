package com.meituan.firefly.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Created by ponyets on 15/6/8.
 */
@Target({FIELD, METHOD, PARAMETER})
@Retention(RUNTIME)
public @interface Field {
    boolean required() default true;

    short id();
}
