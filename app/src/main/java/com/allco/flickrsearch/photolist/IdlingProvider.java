package com.allco.flickrsearch.photolist;

/**
 * Used by tests to handle async operation
 */
@SuppressWarnings("WeakerAccess")
public interface IdlingProvider {
	boolean isIdleNow();
}
