package com.sgates.swagger

import java.lang.annotation.*

/**
 * Created with IntelliJ IDEA.
 * User: Steffen Gates
 * Date: 8/9/13
 * Time: 2:05 PM
 *
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface PropertyAttribute {
    String attribute()  default "type"
    String required()   default "false"
    String value()
}