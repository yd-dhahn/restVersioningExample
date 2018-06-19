package com.mspapant.example.restVersion.conf.annotation;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

@Documented
@Component
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Versionable {

    /**
     * Should be part of part prefix
     * @return
     */
    String apiContext() default "";

    /**
     * Part of Path indicating Version id /v1/method,/v12/method
     * @return
     */
    String versionContext() default  "v";

}
