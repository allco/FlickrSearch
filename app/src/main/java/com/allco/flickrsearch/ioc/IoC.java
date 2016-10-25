package com.allco.flickrsearch.ioc;

import android.app.Application;
import android.support.annotation.NonNull;

public final class IoC {

    private static final class Holder {
        private static final IoC INSTANCE = new IoC();
    }

    private IoC() {
    }

    public static IoC getInstance() {
        return Holder.INSTANCE;
    }

    private ApplicationComponent applicationComponent;

    @NonNull
    public ApplicationComponent getApplicationComponent() {
        return applicationComponent;
    }

    public void setApplicationComponent(@NonNull ApplicationComponent applicationComponent) {
        this.applicationComponent = applicationComponent;
    }

    public void initDependencyGraph(Application application) {
        applicationComponent = DaggerApplicationComponent.builder()
                .applicationModule(new ApplicationModule(application))
                .build();

        if (applicationComponent == null) {
            throw new IllegalStateException("DI initialization failed");
        }
    }
}
