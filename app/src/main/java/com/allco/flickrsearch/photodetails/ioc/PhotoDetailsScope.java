package com.allco.flickrsearch.photodetails.ioc;

import java.lang.annotation.Retention;

import javax.inject.Scope;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@SuppressWarnings("WeakerAccess")
@Retention(RUNTIME)
@Scope
public @interface PhotoDetailsScope {
}
