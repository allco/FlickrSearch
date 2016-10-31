package com.allco.flickrsearch.photolist.ioc;

import com.allco.flickrsearch.photolist.PhotoListPresenter;
import com.allco.flickrsearch.photolist.PhotoListPresenterImpl;

import dagger.Module;
import dagger.Provides;


@SuppressWarnings("WeakerAccess")
@Module
public class PhotoListModule {

    @PhotoListScope
    @Provides
    public PhotoListPresenter providePhotoListPresenter() {
        return new PhotoListPresenterImpl();
    }

}
