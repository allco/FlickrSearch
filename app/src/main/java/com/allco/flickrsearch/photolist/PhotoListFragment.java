package com.allco.flickrsearch.photolist;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.allco.flickrsearch.ioc.IoC;

import javax.inject.Inject;

/**
 * Shows the result of search at Flickr with given request string.
 */
public class PhotoListFragment extends ListFragment implements IdlingProvider {

    public final static String ARG_SEARCH_REQ = "ARG_SEARCH_REQ";

    @Inject
    PhotoListPresenter presenter;

    @Deprecated
    public PhotoListFragment() {
    }

    public static PhotoListFragment newInstance(String searchRequest) {
        @SuppressWarnings("deprecation")
        PhotoListFragment fr = new PhotoListFragment();
        Bundle arg = new Bundle();
        arg.putString(ARG_SEARCH_REQ, searchRequest);
        fr.setArguments(arg);
        return fr;
    }

    @Override
    public void onViewCreated(final View view, Bundle savedInstanceState) {
        IoC.getInstance().getApplicationComponent().photoListComponent().inject(this);
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        presenter.init(this);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        presenter.inflateMenu(menu, inflater);
    }

    @Override
    public void onDestroyView() {
        presenter.destroy();
        presenter = null;
        super.onDestroyView();
    }

    @Override
    public boolean isIdleNow() {
        return presenter != null && presenter.isIdleNow();
    }

    @Nullable
    public String getSearchRequest() {
        return presenter == null ? null : presenter.getSearchRequest();
    }
}
