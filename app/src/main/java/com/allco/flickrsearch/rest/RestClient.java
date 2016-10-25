package com.allco.flickrsearch.rest;

import com.allco.flickrsearch.rest.model.FlickrModel;

import retrofit.Call;

public interface RestClient {
    String END_POINT = "END_POINT";

    /**
     * Create flickr search request represented by {@link Call} object
     *
     * @param request      a search request
     * @param pageNumber   number of desired page
     * @param perPageCount maximum for amount of entries at one page
     * @param allowCache   if {@code false} then using of network will be forced
     * @return {@link Call} object that represent the request
     */
    Call<FlickrModel> createCallFlickrSearch(String request, int pageNumber, int perPageCount, boolean allowCache);
}
