package com.sgates.swagger

import java.lang.annotation.*

/**
 * Created with IntelliJ IDEA.
 * User: Steffen Gates
 * Date: 8/8/13
 * Time: 9:39 PM
 *
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface API {
    String swaggerDataPath()
    String description()
    Model[] models()            default []
}