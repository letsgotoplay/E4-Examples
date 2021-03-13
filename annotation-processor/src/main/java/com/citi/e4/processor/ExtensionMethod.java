package com.citi.e4.processor;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

/**
 * Marks a class as an extension. Extensions are written to a extensions file at
 * {@link Extensions#LOCATION} during the compilation and can be read at runtime
 * with {@link Extensions#getExtensions()}.
 */
@Documented
@Target(ElementType.METHOD)
public @interface ExtensionMethod {
	int value() default 0;
}
