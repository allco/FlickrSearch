package com.allco.flickrsearch.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.text.Html;

import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkNotNull;

/**
 * Container for utils
 */
public class Tools {

	/**
	 * Remove any html tags from <code>str</code>.
	 * @param str
	 * @return non-emty string
	 */
	public static String fromHtml(String str) {
		return str == null ? "" : Html.fromHtml(str).toString();
	}

	/**
	 * @param ctx - the Context imnstance
	 * @return <code>true</code> if WiFi enabled and connected or Cellular data available
	 */
	public static boolean isNetworkAvailable(Context ctx) {
		ctx = checkNotNull(ctx);
		ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
	}

}
