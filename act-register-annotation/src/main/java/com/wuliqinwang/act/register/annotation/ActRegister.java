package com.wuliqinwang.act.register.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author wuli秦王
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.CLASS)
public @interface ActRegister {
    // DES: 名字
    String name() default "";
    // DES: 位置
    int position() default 0;
}
