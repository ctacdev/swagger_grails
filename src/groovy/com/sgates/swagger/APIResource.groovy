package com.sgates.swagger

import java.lang.annotation.*

/**
 * Created with IntelliJ IDEA.
 * User: Steffen Gates
 * Date: 8/8/13
 * Time: 10:34 PM
 *
 */

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface APIResource {
    String description()
    String path()
    Operation[] operations()
}