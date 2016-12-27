package com.allco.flickrsearch;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;

import com.allco.flickrsearch.photolist.PhotoListPresenter;
import com.allco.flickrsearch.photolist.view.PhotoListFragment;

/**
 * Main activity.
 * Handles all fragments with search results.
 */
public class MainActivity extends AppCompatActivity implements SearchView.OnQueryTextListener, PhotoListPresenter.Listener {

	private SearchView searchView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// set up toolbar
		final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (toolbar != null) setSupportActionBar(toolbar);

		// tune searchView
		searchView = (SearchView) findViewById(R.id.searchView);
		searchView.setOnQueryTextListener(this);

		// create initial instance of NewsFragment
		PhotoListFragment photoListFragment = getCurrentPhotosFragment();
		if (photoListFragment == null) {
			replaceFragment(PhotoListFragment.newInstance(null), false);
		}
	}

	@Override
	protected void onResume() {

		// clear initial focus on searchView
		searchView.post(() -> {
			if (searchView != null) {
				searchView.clearFocus();
			}
		});
		super.onResume();
	}

	/**
	 * Called by {@link #searchView} when text is changing
	 */
	@Override
	public boolean onQueryTextSubmit(String query) {

		if (!isSearchRequestApplicable(query)) return true;
		showSubmitButton(false);
		searchView.clearFocus();
		// create new fragment and add it to this Activity
		replaceFragment(PhotoListFragment.newInstance(query), true);
		return true;
	}

	/**
	 * Called by {@link #searchView} when an user submits a search request
	 */
	@Override
	public boolean onQueryTextChange(String newText) {
		// search query is applicable than submit button will appear
		showSubmitButton(isSearchRequestApplicable(newText));
		return false;
	}

	/**
	 * Show submit button
	 *
	 * @param show - true if show
	 */
	private void showSubmitButton(boolean show) {

		if (show == searchView.isSubmitButtonEnabled()) return;
		searchView.setSubmitButtonEnabled(show);
	}

	/**
	 * @return current NewsFragment instance if it exists else null
	 */
	public
	@Nullable
	PhotoListFragment getCurrentPhotosFragment() {

		Fragment fragmentById = getSupportFragmentManager().findFragmentById(R.id.fr_list);
		if (!(fragmentById instanceof PhotoListFragment)) return null;
		return (PhotoListFragment) fragmentById;
	}

	/**
	 * @param request a search request
	 * @return true if given request is not empty and is not equal to current request
	 * (stored at current NewsFragment)
	 */
	private boolean isSearchRequestApplicable(String request) {

		if (TextUtils.isEmpty(request)) return false;

		PhotoListFragment photoListFragment = getCurrentPhotosFragment();
		String currentRequest = photoListFragment == null ? null : photoListFragment.getSearchRequest();
		return currentRequest == null || !currentRequest.equals(request);
	}

	/**
	 * Update text at searchView
	 *
	 * @param request - string that should replace the current one
	 */
	public void setSearchRequest(String request) {

		if (searchView == null) return;
		searchView.setQuery(request, false);
		searchView.clearFocus();
	}

	/**
	 * Add new fragment to Fragment manager and remove previous
	 *
	 * @param fragment   a new fragment
	 * @param addToStack if true then transactions will be added to backStack
	 */
	private void replaceFragment(Fragment fragment, boolean addToStack) {

		if (fragment == null) return;

		FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
		fragmentTransaction
				.setCustomAnimations(R.anim.slide_in_bottom, R.anim.slide_out_top, R.anim.slide_in_top, R.anim.slide_out_bottom)
				.replace(R.id.fr_list, fragment);

		if (addToStack) fragmentTransaction.addToBackStack(null);

		fragmentTransaction.commit();
	}

    @Override
    public void onRequestChanged(String request) {
        setSearchRequest(request);
    }
}
