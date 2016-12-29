package com.allco.flickrsearch.photolist.ioc;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;

import com.allco.flickrsearch.R;
import com.allco.flickrsearch.photolist.view.PhotoListFragment;
import com.allco.flickrsearch.utils.BitmapBorderTransformer;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@SuppressWarnings("WeakerAccess")
@Module
public class PhotoListModule {

    public static final String SEARCH_REQUEST = "SEARCH_REQUEST";
    public static final String THUMB_SIZE = "THUMB_SIZE";

    @NonNull
    private final PhotoListFragment photoListFragment;
    @Nullable
    private final String searchRequest;

    public PhotoListModule(@NonNull PhotoListFragment photoListFragment, @Nullable String searchRequest) {
        this.photoListFragment = photoListFragment;
        this.searchRequest = searchRequest;
    }

    @PhotoListScope
    @Provides
    public PhotoListFragment provideFragment() {
        return photoListFragment;
    }

    @PhotoListScope
    @Named(SEARCH_REQUEST)
    @Provides
    @Nullable
    public String provideSearchRequest() {
        return searchRequest;
    }

    @PhotoListScope
    @Named(THUMB_SIZE)
    @Provides
    public int provideThumbSize(@NonNull Context context) {
        return context.getResources().getDimensionPixelSize(R.dimen.thumb_size);
    }

    @PhotoListScope
    @Provides
    public BitmapBorderTransformer provideBitmapBorderTransformer(Context context) {
        Resources res = context.getResources();
        int sizeThumbRoundPixels = res.getDimensionPixelSize(R.dimen.thumb_round_corner_size);
        return new BitmapBorderTransformer(1 /*border size*/, sizeThumbRoundPixels, ResourcesCompat.getColor(res, R.color.gray_light, null));
    }
}
