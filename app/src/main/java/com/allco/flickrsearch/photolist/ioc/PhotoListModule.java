package com.allco.flickrsearch.photolist.ioc;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;

import com.allco.flickrsearch.R;
import com.allco.flickrsearch.photolist.PhotoListModel;
import com.allco.flickrsearch.photolist.PhotoListPresenter;
import com.allco.flickrsearch.photolist.view.PhotoListAdapter;
import com.allco.flickrsearch.photolist.view.PhotoListFragment;
import com.allco.flickrsearch.rest.RestClient;
import com.allco.flickrsearch.utils.BitmapBorderTransformer;
import com.allco.flickrsearch.utils.ImageLoader;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@SuppressWarnings("WeakerAccess")
@Module
public class PhotoListModule {

    public static final String SEARCH_REQUEST = "SEARCH_REQUEST";

    @NonNull
    private final PhotoListFragment photoListFragment;
    private PhotoListPresenter.Listener listener;
    @Nullable
    private final String searchRequest;

    public PhotoListModule(@NonNull PhotoListFragment photoListFragment, @NonNull PhotoListPresenter.Listener listener, @Nullable String searchRequest) {
        this.photoListFragment = photoListFragment;
        this.searchRequest = searchRequest;
        this.listener = listener;
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

    @NonNull
    @PhotoListScope
    @Provides
    public ImageLoader provideImageLoader(@NonNull Context context) {
        Resources res = context.getResources();
        BitmapBorderTransformer transformer = new BitmapBorderTransformer(
                res.getDimensionPixelSize(R.dimen.thumb_border),
                res.getDimensionPixelSize(R.dimen.thumb_round_corner_size),
                ResourcesCompat.getColor(res, R.color.gray_light, null));
        return new ImageLoader(context, transformer, res.getDimensionPixelSize(R.dimen.thumb_size));
    }

    @NonNull
    @PhotoListScope
    @Provides
    public PhotoListModel providePhotoListModel(@NonNull RestClient restClient) {
        return new PhotoListModel(restClient);
    }

    @NonNull
    @PhotoListScope
    @Provides
    public PhotoListAdapter providePhotoListAdapter(@NonNull Context context, @NonNull ImageLoader imageLoader) {
        return new PhotoListAdapter(context, imageLoader);
    }

    @PhotoListScope
    @Provides
    public PhotoListPresenter.Listener providePresenterListener() {
        return listener;
    }
}
