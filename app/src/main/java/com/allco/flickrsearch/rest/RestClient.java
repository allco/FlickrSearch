package com.allco.flickrsearch.rest;

import android.content.Context;
import android.util.Log;

import com.allco.flickrsearch.rest.model.FlickrModel;
import com.allco.flickrsearch.rest.service.FlickerSearchService;
import com.allco.flickrsearch.utils.Tools;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.util.concurrent.TimeUnit;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

/**
 * Represent the REST client
 */
public class RestClient {
	static private final String BASE_URL_FLICKR = "https://api.flickr.com";

	// size of disk cache for HTTP responses
	private static final int CACHE_MAX_SIZE = 10 * 1024 * 1024; // 10 Mb
	// cache expiration time for online mode, in seconds
	private static final int CACHE_EXPIRATION_TIME = 60 * 60; // 1 hour
	// cache expiration time for offline mode, in seconds
	private static final int CACHE_STALE_TOLERANCE = 60 * 60 * 24; // 1 day

	private FlickerSearchService flickrSearchService;
	private Context ctx;

	/**
	 * Factory for RestClient.
	 * It uses singleton by default.
	 * Or creates every time new RestClient if {@link #enableTestCase(String)} called (used by Test).
	 */
	public static class Factory {
		// alternative base Url - used by Tests
		private static String testAlternateBaseUrl = null;

		/**
		 * Singleton implementation
		 */
		private static class Singleton {
			static private final RestClient sInstance = new RestClient();
		}

		/**
		 * Called only from Tests to force create new instance of RestClient
		 * for every {@link #getRestClient(Context)} call, with given {@code baseUrl}.
		 *
		 * @param alternateBaseUrl alternative baseUrl
		 */
		public static void enableTestCase(String alternateBaseUrl) {

			testAlternateBaseUrl = alternateBaseUrl;
		}

		/**
		 * @return new created or stored RestClient (depends of {@link #enableTestCase(String)} call)
		 */
		public static RestClient getRestClient(Context ctx) {

			if (testAlternateBaseUrl != null) {
				RestClient restClient = new RestClient();
				restClient.tryInitialize(ctx, testAlternateBaseUrl);
				return restClient;
			} else {
				Singleton.sInstance.tryInitialize(ctx, BASE_URL_FLICKR);
				return Singleton.sInstance;
			}
		}

	}

	/**
	 * Initializer for RestClient
	 *
	 * @param ctx Context
	 * @param baseUrl url that will be used as end point
	 */
	private void tryInitialize(Context ctx, String baseUrl) {

		if (flickrSearchService != null) return;

		this.ctx = ctx;

		// tune timeouts
		final OkHttpClient okHttpClient = new OkHttpClient();
		okHttpClient.setReadTimeout(30, TimeUnit.SECONDS);
		okHttpClient.setConnectTimeout(30, TimeUnit.SECONDS);

		// tune cache
		enableCache(okHttpClient);

		// create Retrofit object for farther services initialization
		Retrofit retrofit = new Retrofit.Builder()
				.addConverterFactory(GsonConverterFactory.create())
				.baseUrl(baseUrl)
				.client(okHttpClient)
				.build();

		// create flickr service
		flickrSearchService = retrofit.create(FlickerSearchService.class);

	}

	/**
	 * Enable cache at underlay OkHttp client.
	 * If {@link #ctx} is {@code null} the cache won't be enabled
	 *
	 * @param okHttpClient
	 */
	private void enableCache(OkHttpClient okHttpClient) {

		if (ctx == null) return;

		Cache cache = null;
		try {
			// get appropriate directory for cache
			File externalCacheDir = ctx.getExternalCacheDir();
			if (externalCacheDir == null) {
				externalCacheDir = ctx.getCacheDir();
			}
			File httpCacheDirectory = new File(externalCacheDir, "responses");
			// create cache
			cache = new Cache(httpCacheDirectory, CACHE_MAX_SIZE);
		} catch (Exception e) {
			Log.e("OKHttp", "Could not create http cache", e);
		}

		// install cache to underlay OkHttp client
		if (cache != null) {
			okHttpClient.setCache(cache);
		}
	}

	/**
	 * Create flickr search request represented by {@link Call} object
	 *
	 * @param request a search request
	 * @param pageNumber number of desired page
	 * @param perPageCount maximum for amount of entries at one page
	 * @param allowCache if {@code false} then using of network will be forced
	 * @return {@link Call} object that represent the request
	 */
	public Call<FlickrModel> createCallFlickrSearch(String request, int pageNumber, int perPageCount, boolean allowCache) {

		String cacheControl = "";
		// if cache is fundamentally activated
		if (ctx != null ) {
			// if Internet connection is available
			// use cache regards allowCache variable values
			if (Tools.isNetworkAvailable(ctx)) {
				cacheControl = "private, max-stale=" + (allowCache ? CACHE_EXPIRATION_TIME : 0);
			} else {
				int maxStale = (allowCache ? CACHE_STALE_TOLERANCE : 0); // tolerate 4-weeks stale
				cacheControl = "private, only-if-cached, max-stale=" + maxStale;
			}
		}

		return flickrSearchService.searchNews(request, pageNumber, perPageCount, cacheControl);
	}

}
