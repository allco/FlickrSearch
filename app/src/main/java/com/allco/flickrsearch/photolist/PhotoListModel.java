package com.allco.flickrsearch.photolist;

import android.support.annotation.MainThread;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.allco.flickrsearch.photolist.ioc.PhotoListScope;
import com.allco.flickrsearch.rest.FlickrItemData;
import com.allco.flickrsearch.rest.RestClient;

import java.util.List;

import javax.inject.Inject;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

@PhotoListScope
public class PhotoListModel {

    private static final String TAG = "PhotoListModel";

    // per page items count
    private static final int PER_PAGE_COUNT = 10;

    public boolean isIdle() {
        return currentCall == null;
    }

    public interface OnDataAvailableListener {
        void onDataAvailable(@Nullable List<FlickrItemData.Entry> entries);
    }

    public interface OnDataErrorListener {
        void onDataError();
    }

    @NonNull
    private final RestClient restClient;

    // Represent current request to Flickr.
    // null when there is no active request at this moment.
    private Call<FlickrItemData> currentCall;

    private boolean allowCachedContent = true; // if false the no cache will be used
    private int lastLoadedPageNumber = 0; // page number generator for requests
    private boolean isFinished = false; // true when all data pages is loaded or error occurred
    private String request; // The search request.

    @Inject
    public PhotoListModel(@NonNull RestClient restClient) {
        this.restClient = restClient;
    }

    public void reset(String request, boolean allowCachedContent) {

        destroy();
        lastLoadedPageNumber = 0;

        this.allowCachedContent = allowCachedContent;
        this.isFinished = false;
        this.request = request;
    }

    public boolean isFinished() {
        return isFinished;
    }

    public void destroy() {
        if (currentCall != null) {
            // the cancellation does not work still
            // https://github.com/square/retrofit/issues/1085
            // currentCall.cancel();
        }
        currentCall = null;
    }

    public void tryLoadNextPage(boolean forceRestart, OnDataAvailableListener onDataAvailableListener, OnDataErrorListener onDataErrorListener) {

        if (isFinished()) {
            return;
        }

        if (currentCall != null) {
            if (!forceRestart) {
                return;
            }
            currentCall.cancel();
            currentCall = null;
        }

        // if restart clear page number counter
        if (forceRestart) {
            lastLoadedPageNumber = 0;
        }

        // increase page number counter
        lastLoadedPageNumber++;

        // create new Call by restClient
        currentCall = restClient.createCallFlickrSearch(request, lastLoadedPageNumber, PER_PAGE_COUNT, allowCachedContent);
        // start request
        currentCall.enqueue(createRequestCallback(onDataAvailableListener, onDataErrorListener));
    }

    @NonNull
    private Callback<FlickrItemData> createRequestCallback(final OnDataAvailableListener onDataAvailableListener, final OnDataErrorListener onDataErrorListener) {

        return new Callback<FlickrItemData>() {

            private Call<FlickrItemData> call = currentCall;

            @Override
            @MainThread
            public void onResponse(final Response<FlickrItemData> response, final Retrofit retrofit) {
                // if the call is not welcome anymore
                if (currentCall != call) {
                    return;
                }

                try {
                    // if we got not HTTP_OK then report of error and exit
                    if (response.code() != 200) {
                        onFailure(null);
                    } else {
                        // get data
                        FlickrItemData body = response.body();
                        List<FlickrItemData.Entry> entries = body == null ? null : body.getEntries();

                        // if no data, complete loading
                        if (entries == null || entries.isEmpty()) {
                            isFinished = true;
                        }
                        onDataAvailableListener.onDataAvailable(entries);
                    }
                } finally {
                    // set call as finished
                    currentCall = null;
                }
            }

            @Override
            @MainThread
            public void onFailure(final Throwable t) {
                // if the call is not welcome anymore
                if (currentCall != call) {
                    return;
                }

                Log.e(TAG, "onFailure() called with: ", t);
                try {
                    isFinished = true;
                    onDataErrorListener.onDataError();
                } finally {
                    // set call as finished
                    currentCall = null;
                }
            }
        };
    }
}
