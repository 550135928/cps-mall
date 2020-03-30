package com.xm.comment.annotation;

import java.lang.annotation.*;

/**
 * 获取当前登录的用户信息
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface PlatformType {

    /**
     * 是否必须存在
     * @return
     */
    boolean necessary() default true;

}
