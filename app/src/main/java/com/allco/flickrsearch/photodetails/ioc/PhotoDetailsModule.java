package com.allco.flickrsearch.photodetails.ioc;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;

import com.allco.flickrsearch.utils.ImageLoader;

import dagger.Module;
import dagger.Provides;

@Module
public class PhotoDetailsModule {

    @NonNull
    @PhotoDetailsScope
    @Provides
    public ImageLoader provideImageLoaderDetails(@NonNull Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return new ImageLoader(context, null, displayMetrics.widthPixels);
    }
}
