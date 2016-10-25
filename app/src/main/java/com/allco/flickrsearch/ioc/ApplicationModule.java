package com.allco.flickrsearch.ioc;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;

import com.allco.flickrsearch.rest.RestClient;
import com.allco.flickrsearch.rest.RestClientImpl;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

import static com.allco.flickrsearch.rest.RestClient.END_POINT;

@Module
public class ApplicationModule {

    @NonNull
    private final Context appContext;

    public ApplicationModule(@NonNull Application appContext) {this.appContext = appContext;}

    @Provides
    @Singleton
    public Context provideApplicationContext() {return appContext;}

    @Provides
    @Singleton
    @Named(END_POINT)
    public String provideFlickrSearchEndPoint() {return "https://api.flickr.com";}

    @Provides
    @Singleton
    public RestClient provideRestClient(Context ctx, @Named(END_POINT) String endPoint) {return  new RestClientImpl(ctx, endPoint);}
}
