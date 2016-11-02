package com.allco.flickrsearch.photolist.ioc;

import com.allco.flickrsearch.photolist.PhotoListFragment;
import com.allco.flickrsearch.photolist.PhotoListPresenter;

import dagger.Subcomponent;

@PhotoListScope
@Subcomponent(modules = {PhotoListModule.class})
public interface PhotoListComponent {
    void inject(PhotoListFragment fragment);
}
