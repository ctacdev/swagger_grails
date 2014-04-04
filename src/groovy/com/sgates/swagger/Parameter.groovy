package com.sgates.swagger

import java.lang.annotation.*

/**
 * Created with IntelliJ IDEA.
 * User: Steffen Gates
 * Date: 8/9/13
 * Time: 10:19 AM
 *
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Parameter {
    String name()
    String dataType()
    String description()
    boolean required() default true
    boolean allowMultiple() default false
    String allowableValues() default ""
    String paramType() default "query"
    String defaultValue() default ""
}