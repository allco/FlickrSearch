package com.allco.flickrsearch;

import android.support.test.espresso.IdlingResource;

import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkNotNull;

/**
 * Helping implementation of {@link IdlingResource} used to monitor states of async processes
 * represented by {@link IdlingProvider}.
 */
public class IdlingResourceImpl implements IdlingResource {

	private final IdlingProvider provider;
	private ResourceCallback callback;

	/**
	 * Constructor
	 * @param provider represent actual state
	 */
	public IdlingResourceImpl(IdlingProvider provider)
	{
		this.provider = checkNotNull(provider);
	}


	/**
	 * Returns the name of the resources.
	 */
	@Override public String getName() {
		return provider.toString();
	}

	/**
	 * Returns {@code true} if resource is currently idle.
	 */
	@Override public boolean isIdleNow() {
		boolean idle = provider.isIdleNow();
		if (idle && callback != null) {
			callback.onTransitionToIdle();
		}
		return idle;
	}

	/**
	 * Registers the given {@link ResourceCallback}.
	 */
	@Override public void registerIdleTransitionCallback(ResourceCallback callback) {
		this.callback = callback;
	}

}
