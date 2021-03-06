package com.citi.e4.gem;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Documented
@Target(ElementType.TYPE)
public @interface GEMValidation {

	BuildType buildType() default BuildType.GRADLE;

	String projectId();
}
