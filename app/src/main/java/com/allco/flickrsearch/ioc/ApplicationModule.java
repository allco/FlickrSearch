package com.allco.flickrsearch.ioc;

import android.app.Application;
import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;

import com.allco.flickrsearch.rest.RestClient;

import javax.inject.Named;
import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;


@SuppressWarnings("WeakerAccess")
@Module
public class ApplicationModule {

    public static final String END_POINT = "END_POINT";
    @NonNull
    private final Context appContext;

    public ApplicationModule(@NonNull Application appContext) {this.appContext = appContext;}

    @Provides
    @Singleton
    public Context provideApplicationContext() {return appContext;}

    @Provides
    @Singleton
    public Resources provideResources(Context context) {return context.getResources();}

    @Provides
    @Singleton
    @Named(END_POINT)
    public String provideFlickrSearchEndPoint() {return "https://api.flickr.com";}
}
