package com.allco.flickrsearch.photolist;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.allco.flickrsearch.R;
import com.allco.flickrsearch.ioc.IoC;
import com.allco.flickrsearch.photolist.ioc.PhotoListScope;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;

import java.lang.ref.WeakReference;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.allco.flickrsearch.photolist.PhotoListFragment.ARG_SEARCH_REQ;

@PhotoListScope
public class PhotoListPresenter {

    private final Context context;

    private String searchRequest;
    private PhotoListAdapter listAdapter;
    private MenuItem menuItemRefresh;
    private ListView listView;
    private PhotoListFragment fragment;

    public interface Listener {
        void onRequestChanged(String searchRequest);
    }

    @Inject
    public PhotoListPresenter(Context context, PhotoListAdapter adapter) {
        this.context = context;
        this.listAdapter = adapter;
    }

    void init(@NonNull PhotoListFragment fragment) {
        this.fragment = fragment;
        listView = fragment.getListView();
        Bundle arguments = fragment.getArguments();
        searchRequest = arguments == null ? "" : arguments.getString(ARG_SEARCH_REQ);

        // reset searchRequest at MainActivity
        tryResetMainActivity();

        // if empty searchRequest
        if (TextUtils.isEmpty(searchRequest)) {
            showMessage(R.string.please_enter_text_search, false);
            return;
        }

        listView.setOnItemClickListener(
                (parent, view, position, id) ->
                        PhotoViewerActivity.start(listView.getContext(),
                                listAdapter.getItemTitle(position),
                                listAdapter.getItemPhotoUrl(position)
                        ));

        //create and setup adapter
        listAdapter.setListener(createAdapterListener(new WeakReference<>(this)));
        setListAdapter(listAdapter.reset(searchRequest, true));
        fragment.setListShownNoAnimation(false);
    }

    void destroy() {
    }

    private void tryResetMainActivity() {
        Activity activity = fragment.getActivity();
        if (activity instanceof PhotoListPresenter.Listener) {
            ((PhotoListPresenter.Listener) activity).onRequestChanged(searchRequest);
        }
    }

    private void setListAdapter(ListAdapter adapter) {

        // if adapter handles scrolling
        if (adapter instanceof AbsListView.OnScrollListener) {
            listView.setOnScrollListener((AbsListView.OnScrollListener) adapter);
        }

        // add item appearing animation
        if (adapter instanceof BaseAdapter) {
            SwingBottomInAnimationAdapter animationAdapter = new SwingBottomInAnimationAdapter((BaseAdapter) adapter);
            animationAdapter.setAbsListView(listView);
            adapter = animationAdapter;
        }

        fragment.setListAdapter(adapter);
    }

    private static PhotoListAdapter.Listener createAdapterListener(WeakReference<PhotoListPresenter> refPresenter) {

        return new PhotoListAdapter.Listener() {

            @Override
            public void onPageLoaded(PhotoListAdapter adapter) {
                PhotoListPresenter presenter = refPresenter.get();
                if (presenter == null) {
                    return;
                }
                presenter.onPageLoaded(adapter);
            }

            @Override
            public void onError(PhotoListAdapter adapter) {
                PhotoListPresenter presenter = refPresenter.get();
                if (presenter == null) {
                    return;
                }
                presenter.onError(adapter);
            }
        };
    }

    private void onPageLoaded(PhotoListAdapter adapter) {
        if (adapter.isFinished() && adapter.getCount() < 1) {
            showMessage(R.string.nothing_found_try_other_request, false);
        } else {
            if (menuItemRefresh != null) {
                menuItemRefresh.setVisible(true);
            }
            fragment.setListShown(true);
        }
    }

    private void onError(PhotoListAdapter adapter) {
        if (adapter.getCount() <= 0) {
            showMessage(R.string.error_occurred, true);
        }
    }

    private void showMessage(int resId, boolean showRefresh) {

        if (menuItemRefresh != null && menuItemRefresh.isVisible() != showRefresh) {
            menuItemRefresh.setVisible(showRefresh);
        }

        setListAdapter(null);
        fragment.setEmptyText(context.getString(resId));
        fragment.setListShownNoAnimation(true);
    }

    String getSearchRequest() {
        return searchRequest;
    }

    boolean isIdleNow() {
        return listAdapter == null || listAdapter.isIdleNow();
    }

    void inflateMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_photolist, menu);
        menuItemRefresh = menu.findItem(R.id.action_refresh);
        menuItemRefresh.setOnMenuItemClickListener(item -> {
            refresh();
            return true;
        });
    }

    private void refresh() {
        if (listAdapter != null) {
            fragment.setListShown(false);
            listAdapter.reset(searchRequest, false);
            tryResetMainActivity();
        }
    }
}
