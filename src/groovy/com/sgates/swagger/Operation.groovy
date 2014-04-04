package com.sgates.swagger

import java.lang.annotation.*

/**
 * Created with IntelliJ IDEA.
 * User: Steffen Gates
 * Date: 8/9/13
 * Time: 10:15 AM
 *
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Operation {
    String httpMethod()             default "GET"
    String nickname()               default ""
    String notes()                  default ""
    String responseClass()
    String summary()                default ""
    String consumes()               default ""
    String produces()               default "application/json"
    String protocols()				default "http"
    Parameter[] paramaters()        default []
    ErrorCode[] errors()
}