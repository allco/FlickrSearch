package com.allco.flickrsearch.photodetails.ioc;

import com.allco.flickrsearch.photodetails.PhotoDetailsActivity;

import dagger.Subcomponent;

@PhotoDetailsScope
@Subcomponent(modules = {PhotoDetailsModule.class})
public interface PhotoDetailsComponent {
    void inject(PhotoDetailsActivity photoDetailsActivity);
}
