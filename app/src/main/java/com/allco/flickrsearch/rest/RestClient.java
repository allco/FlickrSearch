package com.allco.flickrsearch.rest;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.allco.flickrsearch.utils.Utils;
import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.File;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

import retrofit.Call;
import retrofit.GsonConverterFactory;
import retrofit.Retrofit;

import static com.allco.flickrsearch.ioc.ApplicationModule.END_POINT;

@Singleton
public class RestClient {

    // size of disk cache for HTTP responses
    private static final int CACHE_MAX_SIZE = 10 * 1024 * 1024; // 10 Mb
    // cache expiration time for online mode, in seconds
    private static final int CACHE_EXPIRATION_TIME = 60 * 60; // 1 hour
    // cache expiration time for offline mode, in seconds
    private static final int CACHE_STALE_TOLERANCE = 60 * 60 * 24; // 1 day

    @NonNull
    private final Context ctx;
    @NonNull
    private final String endPoint;
    @Nullable
    private FlickerSearchService flickrSearchService;
    private boolean cacheEnabled = false;

    @Inject
    public RestClient(@NonNull Context ctx, @Named(END_POINT) @NonNull String endPoint) {
        this.ctx = ctx;
        this.endPoint = endPoint;
    }

    public Call<FlickrItemData> createCallFlickrSearch(String request, int pageNumber, int perPageCount, boolean allowCache) {

        tryInitialize();

        if (flickrSearchService == null) {
            throw new IllegalStateException();
        }

        String cacheControl = "";

        if (cacheEnabled) {
            // if Internet connection is available
            // use cache regards allowCache variable values
            if (Utils.isNetworkAvailable(ctx)) {
                cacheControl = "private, max-stale=" + (allowCache ? CACHE_EXPIRATION_TIME : 0);
            } else {
                int maxStale = (allowCache ? CACHE_STALE_TOLERANCE : 0); // tolerate 4-weeks stale
                cacheControl = "private, only-if-cached, max-stale=" + maxStale;
            }
        }

        return flickrSearchService.searchNews(request, pageNumber, perPageCount, cacheControl);
    }

    private void tryInitialize() {

        if (flickrSearchService != null) {
            return;
        }

        // tune timeouts
        final OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.setReadTimeout(30, TimeUnit.SECONDS);
        okHttpClient.setConnectTimeout(30, TimeUnit.SECONDS);

        // tune cache
        enableCache(okHttpClient);

        // create Retrofit object for farther services initialization
        Retrofit retrofit = new Retrofit.Builder()
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(endPoint)
                .client(okHttpClient)
                .build();

        // create flickr service
        flickrSearchService = retrofit.create(FlickerSearchService.class);
    }

    private void enableCache(OkHttpClient okHttpClient) {

        Cache cache = null;
        try {
            // get appropriate directory for cache
            File externalCacheDir = ctx.getExternalCacheDir();
            if (externalCacheDir == null) {
                externalCacheDir = ctx.getCacheDir();
            }

            if (externalCacheDir != null) {
                File httpCacheDirectory = new File(externalCacheDir, "responses");
                // create cache
                cache = new Cache(httpCacheDirectory, CACHE_MAX_SIZE);
            }
        } catch (Exception e) {
            Log.e("OKHttp", "Could not create http cache", e);
        }

        // install cache to underlay OkHttp client
        if (cache != null) {
            okHttpClient.setCache(cache);
            cacheEnabled = true;
        }
    }
}
