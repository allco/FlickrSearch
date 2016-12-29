package com.allco.flickrsearch.photolist.ioc;

import android.content.Context;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.widget.ListView;

import com.allco.flickrsearch.R;
import com.allco.flickrsearch.photodetails.PhotoDetailsActivity;
import com.allco.flickrsearch.photolist.view.PhotoListAdapter;
import com.allco.flickrsearch.photolist.view.PhotoListFragment;
import com.allco.flickrsearch.photolist.view.PhotoListItemData;
import com.allco.flickrsearch.utils.BitmapBorderTransformer;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@SuppressWarnings("WeakerAccess")
@Module
public class PhotoListModule {

    public static final String SEARCH_REQUEST = "SEARCH_REQUEST";
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
    @Provides
    public PhotoListAdapter providePhotoListAdapter(Context context) {

        ListView listView = photoListFragment.getListView();
        // lets rise an exception as early as possible in case of fatal errors
        if (listView == null) {
            throw new IllegalStateException("ListView should not be null");
        }

        Resources res = context.getResources();
        int sizeThumbRoundPixels = res.getDimensionPixelSize(R.dimen.thumb_round_corner_size);
        BitmapBorderTransformer transformer = new BitmapBorderTransformer(1 /*border size*/, sizeThumbRoundPixels, ResourcesCompat.getColor(res, R.color.gray_light, null));
        PhotoListAdapter photoListAdapter = new PhotoListAdapter(context, transformer, res.getDimensionPixelSize(R.dimen.thumb_size));

        SwingBottomInAnimationAdapter animatedAdapter = new SwingBottomInAnimationAdapter(photoListAdapter);
        animatedAdapter.setAbsListView(listView);

        listView.setOnScrollListener(photoListAdapter);

        listView.setOnItemClickListener(
                (parent, view, position, id) -> {
                    PhotoListItemData photoData = photoListAdapter.getItem(position);
                    PhotoDetailsActivity.start(listView.getContext(), photoData.getTitle(), photoData.getImageUrl());
                }
        );

        listView.setAdapter(animatedAdapter);

        return photoListAdapter;
    }
}
