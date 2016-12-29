package com.allco.flickrsearch.photolist;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ListView;

import com.allco.flickrsearch.R;
import com.allco.flickrsearch.photodetails.PhotoDetailsActivity;
import com.allco.flickrsearch.photolist.ioc.PhotoListModule;
import com.allco.flickrsearch.photolist.ioc.PhotoListScope;
import com.allco.flickrsearch.photolist.view.PhotoListAdapter;
import com.allco.flickrsearch.photolist.view.PhotoListFragment;
import com.allco.flickrsearch.photolist.view.PhotoListItemData;
import com.allco.flickrsearch.rest.FlickrItemData;
import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Named;

@PhotoListScope
public class PhotoListPresenter {

    @NonNull
    private final Context context;
    @NonNull
    private PhotoListAdapter listAdapter;
    @NonNull
    private PhotoListModel photoListModel;
    @Nullable
    private String searchRequest;
    @NonNull
    private PhotoListFragment fragment;

    private MenuItem menuItemRefresh;

    public interface Listener {
        void onRequestChanged(String searchRequest);
    }

    @Inject
    public PhotoListPresenter(
            @NonNull Context context,
            @NonNull PhotoListFragment fragment,
            @NonNull PhotoListAdapter adapter,
            @NonNull PhotoListModel photoListModel,
            @Nullable @Named(PhotoListModule.SEARCH_REQUEST) String searchRequest) {
        this.context = context;
        this.fragment = fragment;
        this.listAdapter = adapter;
        this.photoListModel = photoListModel;
        this.searchRequest = searchRequest;
    }

    public void start() {

        ListView listView = fragment.getListView();
        // lets rise an exception as early as possible in case of fatal errors
        if (listView == null) {
            throw new IllegalStateException("ListView should not be null");
        }

        SwingBottomInAnimationAdapter animatedAdapter = new SwingBottomInAnimationAdapter(listAdapter);
        animatedAdapter.setAbsListView(listView);

        listView.setOnScrollListener(listAdapter);

        listView.setOnItemClickListener(
                (parent, view, position, id) -> {
                    PhotoListItemData photoData = listAdapter.getItem(position);
                    PhotoDetailsActivity.start(listView.getContext(), photoData.getTitle(), photoData.getImageUrl());
                }
        );

        listView.setAdapter(animatedAdapter);

        // if empty searchRequest
        if (TextUtils.isEmpty(searchRequest)) {
            showMessage(R.string.please_enter_text_search, false);
        } else {
            refresh(true);
        }
    }

    public void destroy() {
        photoListModel.destroy();
    }

    public void onResume() {
        // reset searchRequest at MainActivity
        tryResetMainActivity();
    }

    private void tryResetMainActivity() {
        Activity activity = fragment.getActivity();
        if (activity instanceof PhotoListPresenter.Listener) {
            ((PhotoListPresenter.Listener) activity).onRequestChanged(searchRequest);
        }
    }

    private void showMessage(int resId, boolean showRefresh) {

        if (menuItemRefresh != null && menuItemRefresh.isVisible() != showRefresh) {
            menuItemRefresh.setVisible(showRefresh);
        }

        fragment.setEmptyText(context.getString(resId));
        fragment.setListShownNoAnimation(true);
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
