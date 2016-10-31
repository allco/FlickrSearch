package com.allco.flickrsearch.photolist.ioc;

import com.allco.flickrsearch.photolist.PhotoListFragment;
import com.allco.flickrsearch.photolist.PhotoListPresenterImpl;

import dagger.Subcomponent;

@PhotoListScope
@Subcomponent(modules = {PhotoListModule.class})
public interface PhotoListComponent {
    void inject(PhotoListPresenterImpl presenter);
    void inject(PhotoListFragment fragment);
}
