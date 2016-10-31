package com.allco.flickrsearch.ioc;

import com.allco.flickrsearch.photolist.PhotoListAdapter;
import com.allco.flickrsearch.photolist.ioc.PhotoListComponent;
import com.allco.flickrsearch.rest.RestClient;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {ApplicationModule.class})
public interface ApplicationComponent {

    void inject(PhotoListAdapter photoListAdapter);
    RestClient restClient();
    PhotoListComponent photoListComponent();
}
