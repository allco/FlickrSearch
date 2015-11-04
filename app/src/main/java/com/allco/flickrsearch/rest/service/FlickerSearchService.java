package com.allco.flickrsearch.rest.service;

import com.allco.flickrsearch.rest.model.FlickrSearchResultModel;

import retrofit.Call;
import retrofit.http.GET;
import retrofit.http.Header;
import retrofit.http.Query;

/**
 * Retrofit service interface representing Google News service
 */
@SuppressWarnings("SpellCheckingInspection")
public interface FlickerSearchService {

    String API_KEY = "588459f7180bc9126823d9a2e6ba6637";

    @GET("/services/rest/?method=flickr.photos.search&format=json&nojsoncallback=1&api_key=" + API_KEY)
    Call<FlickrSearchResultModel> searchNews(@Query("text") String request,
                                             @Query("page") int pageNumber,
                                             @Query("per_page") int perPageCount,
                                             @Header("Cache-Control") String cacheCtrlHeader);
}