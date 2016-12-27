package com.allco.flickrsearch;

import android.app.Application;

import com.allco.flickrsearch.ioc.IoC;

public class FlickrsearchApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        IoC.getInstance().initDependencyGraph(this);
    }
}
