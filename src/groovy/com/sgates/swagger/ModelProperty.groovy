package com.sgates.swagger

import java.lang.annotation.*

/**
 * Created with IntelliJ IDEA.
 * User: Steffen Gates
 * Date: 8/9/13
 * Time: 1:12 PM
 *
 */

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ModelProperty {
    String propertyName()
    PropertyAttribute[] attributes()
    String notes() default ""
}