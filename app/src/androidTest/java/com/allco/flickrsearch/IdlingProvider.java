package com.allco.flickrsearch;

/**
 * Used by tests to handle async operation
 */
@SuppressWarnings("WeakerAccess")
public interface IdlingProvider {
	boolean isIdleNow();
}
