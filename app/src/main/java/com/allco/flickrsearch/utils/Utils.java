package com.allco.flickrsearch.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.text.Html;

/**
 * Container for utils
 */
public class Utils {

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
	public static boolean isNetworkAvailable(@NonNull Context ctx) {
		ConnectivityManager cm = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
		return cm != null && cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
	}

}
