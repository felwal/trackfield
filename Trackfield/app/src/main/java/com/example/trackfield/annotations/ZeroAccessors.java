package com.example.trackfield.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Denotes that a method is only for single-call maintenance and should not be called continiously.
 */
@Target(ElementType.METHOD)
public @interface ZeroAccessors {

}
