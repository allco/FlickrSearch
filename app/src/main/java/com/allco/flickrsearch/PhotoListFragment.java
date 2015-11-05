package com.allco.flickrsearch;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;

import com.nhaarman.listviewanimations.appearance.simple.SwingBottomInAnimationAdapter;

import static com.allco.flickrsearch.utils.Preconditions.checkNotNull;

/**
 * Shows the result of search at Flickr with given request string.
 */
public class PhotoListFragment extends ListFragment implements PhotoListAdapter.Listener, IdlingProvider {

	private final static String ARG_SEARCH_REQ = "ARG_SEARCH_REQ";
	private String searchRequest;

	private PhotoListAdapter listAdapter; // if null, it is mean that fragment is not created yet or already destroyed
	private MenuItem menuItemRefresh;

	/**
	 * Factory method for PhotoListFragment
	 *
	 * @param searchRequest - a search request for Flickr
	 * @return constructed and prepared PhotoListFragment
	 */
	public static PhotoListFragment newInstance(String searchRequest) {

		Bundle arg = new Bundle();
		arg.putString(ARG_SEARCH_REQ, searchRequest);
		PhotoListFragment fr = new PhotoListFragment();
		fr.setArguments(arg);
		return fr;
	}

	/**
	 * Attach to list view once the view hierarchy has been created.
	 */
	@Override
	public void onViewCreated(final View view, Bundle savedInstanceState) {

		super.onViewCreated(view, savedInstanceState);

		setHasOptionsMenu(true);

		Bundle arguments = getArguments();
		searchRequest = arguments == null ? "" : arguments.getString(ARG_SEARCH_REQ);

		// reset searchRequest at MainActivity
		tryResetMainActivity();


		// if empty searchRequest
		if (TextUtils.isEmpty(searchRequest)) {
			showMessage(R.string.please_enter_text_search, false);
			return;
		}

		//create and setup adapter
		listAdapter = PhotoListAdapter.newInstance(getActivity(), this);
		setListAdapter(listAdapter.reset(searchRequest, true));
		setListShownNoAnimation(false);
	}

	/**
	 * Try to setup search request to searchView of MainActivity
	 */
	private void tryResetMainActivity() {

		Activity activity = getActivity();
		if (activity instanceof MainActivity) {
			((MainActivity) activity).setSearchRequest(searchRequest);
		}
	}

	/**
	 * Install given Adapter to underlined ListView
	 *
	 * @param adapter Adapter implementation
	 */
	@Override
	public void setListAdapter(ListAdapter adapter) {

		ListView listView = checkNotNull(getListView());

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

		super.setListAdapter(adapter);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.menu_photolist, menu);
		menuItemRefresh = menu.findItem(R.id.action_refresh);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		int id = item.getItemId();

		// if refresh button pressed
		if (id == R.id.action_refresh) {
			refresh();
			return true;

		}

		return super.onOptionsItemSelected(item);
	}

	/**
	 * @return true if current fragment is destroyed or not yet created
	 */
	private boolean isFragmentDestroyed() {

		return listAdapter == null;
	}

	/**
	 * Implementation of PhotoListAdapter.Listener.
	 * Called by PhotoListAdapter when next piece of result is loaded.
	 *
	 * @param adapter an instance of PhotoListAdapter that called this method
	 */
	@Override
	public void onPageLoaded(PhotoListAdapter adapter) {

		if (isFragmentDestroyed()) return; // if fragment is destroyed
		if (adapter.isFinished() && adapter.getCount() < 1) {
			showMessage(R.string.nothing_found_try_other_request, false);
		}
		else {
			if (menuItemRefresh != null) menuItemRefresh.setVisible(true);
			setListShown(true);
		}
	}

	/**
	 * Implementation of PhotoListAdapter.Listener.
	 * Called by PhotoListAdapter when any error occurred.
	 *
	 * @param adapter an instance of PhotoListAdapter that called this method
	 */
	@Override
	public void onError(PhotoListAdapter adapter) {

		if (isFragmentDestroyed()) return;
		if (adapter.getCount() <= 0) {
			showMessage(R.string.error_occurred, true);
		}
	}

	/**
	 * Detach from list view.
	 */
	@Override
	public void onDestroyView() {

		listAdapter = null;
		super.onDestroyView();
	}

	/**
	 * This method will be called when an item in the list is selected.
	 *
	 * @param l        The ListView where the click happened
	 * @param v        The view that was clicked within the ListView
	 * @param position The position of the view in the list
	 * @param id       The row id of the item that was clicked
	 */
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {

		super.onListItemClick(l, v, position, id);

		if (isFragmentDestroyed()) return;

		PhotoViewerActivity.start(getActivity(),
								  listAdapter.getItemTitle(position),
								  listAdapter.getItemPhotoUrl(position)
								 );
	}

	/**
	 * Shows text banner and hide ListView
	 *
	 *
	 * @param resId message resource Id for banner
	 */
	private void showMessage(int resId, boolean showRefresh) {

		if (menuItemRefresh != null && menuItemRefresh.isVisible() != showRefresh) {
			menuItemRefresh.setVisible(showRefresh);
		}

		setListAdapter(null);
		setEmptyText(getString(resId));
		setListShownNoAnimation(true);
	}

	public String getSearchRequest() {

		return searchRequest;
	}

	/**
	 * Refresh content.
	 * Cache using will be forbidden
	 */
	public void refresh() {

		if (listAdapter != null) {
			setListShown(false);
			listAdapter.reset(searchRequest, false);
			tryResetMainActivity();
		}
	}

	/**
	 * Implementation of IdlingProvider's method.
	 *
	 * @return true if some async processes in progress at this moment
	 */
	@Override public boolean isIdleNow() {

		return listAdapter == null || listAdapter.isIdleNow();
	}
}
