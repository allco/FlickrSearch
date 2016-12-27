package com.allco.flickrsearch.photolist.ioc;

import android.content.Context;
import android.content.res.Resources;
import android.support.v4.content.res.ResourcesCompat;

import com.allco.flickrsearch.R;
import com.allco.flickrsearch.utils.BitmapBorderTransformer;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

import static com.allco.flickrsearch.photolist.view.PhotoListAdapter.THUMB_SIZE;

@SuppressWarnings("WeakerAccess")
@Module
public class PhotoListModule {


    @PhotoListScope
    @Named(THUMB_SIZE)
    @Provides
    public int provideThumbSize(Resources res) {
        return res.getDimensionPixelSize(R.dimen.thumb_size);
    }

    @PhotoListScope
    @Provides
    public BitmapBorderTransformer provideBitmapBorderTransformer(Context context) {
        Resources res = context.getResources();
        int sizeThumbRoundPixels = res.getDimensionPixelSize(R.dimen.thumb_round_corner_size);
        return new BitmapBorderTransformer(1 /*border size*/, sizeThumbRoundPixels, ResourcesCompat.getColor(res, R.color.gray_light, null));
    }
}
