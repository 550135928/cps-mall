package com.xm.comment.annotation;

import java.lang.annotation.*;

/**
 * 获取当前登录的用户信息
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface LoginUser {
    /**
     * 默认从request header中取出userId
     * @return
     */
    String value() default "UserId";

    boolean necessary() default true;
}
