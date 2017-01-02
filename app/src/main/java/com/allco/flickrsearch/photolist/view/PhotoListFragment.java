package com.allco.flickrsearch.photolist.view;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ListFragment;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.ListView;

import com.allco.flickrsearch.ioc.IoC;
import com.allco.flickrsearch.photolist.PhotoListPresenter;
import com.allco.flickrsearch.photolist.ioc.PhotoListModule;

import javax.inject.Inject;

/**
 * Shows the result of search at Flickr with given request string.
 */
public class PhotoListFragment extends ListFragment {

    private final static String ARG_SEARCH_REQ = "ARG_SEARCH_REQ";

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
        PhotoListPresenter.Listener listener = (PhotoListPresenter.Listener) getActivity();
        IoC.getInstance().getApplicationComponent().photoListComponent(new PhotoListModule(this, listener, getSearchRequest())).inject(this);
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);

        ListView listView = getListView();
        // lets rise an exception as early as possible in case of fatal errors
        if (listView == null) {
            throw new IllegalStateException("ListView should not be null");
        }

        presenter.attach(listView);
        presenter.startRequesting();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        presenter.inflateMenu(menu, inflater);
    }

    @Override
    public void onResume() {
        super.onResume();
        presenter.onResume();
    }

    @Override
    public void onDestroyView() {
        presenter.destroy();
        presenter = null;
        super.onDestroyView();
    }

    @Nullable
    public String getSearchRequest() {
        Bundle arguments = getArguments();
        return arguments == null ? "" : arguments.getString(ARG_SEARCH_REQ);
    }
}
