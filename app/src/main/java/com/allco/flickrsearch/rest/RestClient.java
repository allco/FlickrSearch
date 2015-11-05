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

	public static final int CACHE_MAX_SIZE = 10 * 1024 * 1024;

	public static final int CACHE_EXPIRATION_TIME = 3600; // in seconds
	public static final int CACHE_STALE_TOLERANCE = 60 * 60 * 24 * 28;

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

	private void tryInitialize(Context ctx, String baseUrl) {

		if (flickrSearchService != null) return;

		this.ctx = ctx;

		// tune timeouts
		final OkHttpClient okHttpClient = new OkHttpClient();
		okHttpClient.setReadTimeout(30, TimeUnit.SECONDS);
		okHttpClient.setConnectTimeout(30, TimeUnit.SECONDS);

		addCache(okHttpClient);

		Retrofit retrofit = new Retrofit.Builder()
				.addConverterFactory(GsonConverterFactory.create())
				.baseUrl(baseUrl)
				.client(okHttpClient)
				.build();

		// create Calls source
		flickrSearchService = retrofit.create(FlickerSearchService.class);

	}

	private void addCache(OkHttpClient okHttpClient) {

		if (ctx == null) return;

		Cache cache = null;
		try {
			File externalCacheDir = ctx.getExternalCacheDir();
			if (externalCacheDir == null) {
				externalCacheDir = ctx.getCacheDir();
			}
			File httpCacheDirectory = new File(externalCacheDir, "responses");
			cache = new Cache(httpCacheDirectory, CACHE_MAX_SIZE);
		} catch (Exception e) {
			Log.e("OKHttp", "Could not create http cache", e);
		}

		if (cache != null) {
			okHttpClient.setCache(cache);
		}
	}



	public Call<FlickrModel> createCallFlickrSearch(String request, int pageNumber, int perPageCount, boolean allowCache) {

		String cacheControl = "";
		if (ctx != null ) {
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
