package com.allco.flickrsearch.photolist;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.allco.flickrsearch.R;
import com.allco.flickrsearch.photodetails.PhotoDetailsActivity;
import com.allco.flickrsearch.photolist.ioc.PhotoListScope;
import com.allco.flickrsearch.photolist.view.PhotoListAdapter;
import com.allco.flickrsearch.photolist.view.PhotoListFragment;
import com.allco.flickrsearch.photolist.view.PhotoListItemData;
import com.allco.flickrsearch.rest.FlickrItemData;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;

import java.util.List;

import javax.inject.Inject;

import static com.allco.flickrsearch.photolist.view.PhotoListFragment.ARG_SEARCH_REQ;

@PhotoListScope
public class PhotoListPresenter {

    @NonNull
    private final Context context;
    @NonNull
    private PhotoListAdapter listAdapter;
    @NonNull
    private PhotoListModel photoListModel;

    private String searchRequest;
    private PhotoListFragment fragment;
    private MenuItem menuItemRefresh;
    private ListView listView;

    public interface Listener {
        void onRequestChanged(String searchRequest);
    }

    @Inject
    public PhotoListPresenter(@NonNull Context context, @NonNull PhotoListAdapter adapter, @NonNull PhotoListModel photoListModel) {
        this.context = context;
        this.listAdapter = adapter;
        this.photoListModel = photoListModel;
    }

    public void init(@NonNull PhotoListFragment fragment) {
        this.fragment = fragment;
        listView = fragment.getListView();

        Bundle arguments = fragment.getArguments();
        searchRequest = arguments == null ? "" : arguments.getString(ARG_SEARCH_REQ);

        // if empty searchRequest
        if (TextUtils.isEmpty(searchRequest)) {
            showMessage(R.string.please_enter_text_search, false);
            return;
        }

        listView.setOnItemClickListener(
                (parent, view, position, id) -> {
                    PhotoListItemData photoData = listAdapter.getItem(position);
                    PhotoDetailsActivity.start(listView.getContext(), photoData.getTitle(), photoData.getImageUrl());
                }
        );

        refresh(true);
    }

    public void onResume() {
        // reset searchRequest at MainActivity
        tryResetMainActivity();
    }

    public void destroy() {
        photoListModel.destroy();
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

    private void showMessage(int resId, boolean showRefresh) {

        if (menuItemRefresh != null && menuItemRefresh.isVisible() != showRefresh) {
            menuItemRefresh.setVisible(showRefresh);
        }

        setListAdapter(null);
        fragment.setEmptyText(context.getString(resId));
        fragment.setListShownNoAnimation(true);
    }

    public String getSearchRequest() {
        return searchRequest;
    }

    public void inflateMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_photolist, menu);
        menuItemRefresh = menu.findItem(R.id.action_refresh);
        menuItemRefresh.setOnMenuItemClickListener(item -> {
            refresh(false);
            return true;
        });
    }

    private void refresh(boolean allowCache) {
        tryResetMainActivity();
        photoListModel.reset(searchRequest, allowCache);
        Runnable loadNextPage =
                () -> photoListModel.tryLoadNextPage(false,
                        this::onPageLoaded,
                        () -> showMessage(R.string.error_occurred, true));

        // link adapter with model
        listAdapter.reset(() -> !photoListModel.isFinished(), loadNextPage);

        setListAdapter(listAdapter);
        fragment.setListShown(false);

        loadNextPage.run();
    }

    private void onPageLoaded(@Nullable final List<FlickrItemData.Entry> entries) {
        if (entries == null || entries.isEmpty()) {
            if (photoListModel.isFinished() && listAdapter.getCount() < 1) {
                showMessage(R.string.nothing_found_try_other_request, false);
            }
        } else {
            listAdapter.addData(PhotoListItemData.map(entries));

            if (menuItemRefresh != null) {
                menuItemRefresh.setVisible(true);
            }

            fragment.setListShownNoAnimation(true);
        }
    }
}
