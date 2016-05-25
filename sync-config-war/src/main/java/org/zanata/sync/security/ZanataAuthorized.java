package org.zanata.sync.security;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apache.deltaspike.security.api.authorization.SecurityBindingType;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE, ElementType.METHOD})
@Documented
@SecurityBindingType
public @interface ZanataAuthorized {
}
