package com.allco.flickrsearch.photolist;

import android.view.Menu;
import android.view.MenuInflater;

public interface PhotoListPresenter extends IdlingProvider {

    void refresh();
    interface Listener {
        void onRequestChanged(String request);
    }

    void init(PhotoListFragment fragment);
    void destroy();
    void inflateMenu(Menu menu, MenuInflater inflater);
    String getSearchRequest();
    boolean isIdleNow();

}
