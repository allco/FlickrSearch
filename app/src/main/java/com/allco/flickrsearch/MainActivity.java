package com.allco.flickrsearch;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Main activity.
 * Handles all fragments and views.
 */
public class MainActivity extends AppCompatActivity {
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
		searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

			boolean isSubmitVisible = false; // <code>true</code> if submitButton is visible

			@Override
			public boolean onQueryTextSubmit(String query) {
				if (!isSearchRequestApplicable(query)) return true;
				showSubmitButton(false);
				searchView.clearFocus();
				// create new fragment and add it to this Activity
				replaceFragment(PhotoListFragment.newInstance(query), true);
				return true;
			}

			@Override
			public boolean onQueryTextChange(String newText) {
				// search query is applicable than submit button will appear
				showSubmitButton(isSearchRequestApplicable(newText));
				return false;
			}

			/**
			 * Show submit button
			 * @param show - true if show
			 */
			private void showSubmitButton(boolean show) {
				if (show == isSubmitVisible) return;
				isSubmitVisible = show;
				searchView.setSubmitButtonEnabled(isSubmitVisible);
			}
		});

		// clear initial focus on searchView
		searchView.post(new Runnable() {
			@Override
			public void run() {
				searchView.clearFocus();
			}
		});

		// create initial instance of NewsFragment
		PhotoListFragment photoListFragment = getCurrentNewsFragment();
		if (photoListFragment == null) {
			replaceFragment(PhotoListFragment.newInstance(null), false);
		}
	}

	/**
	 * @return current NewsFragment instance if it exists else null
	 */
	public  @Nullable
	PhotoListFragment getCurrentNewsFragment() {
		Fragment fragmentById = getSupportFragmentManager().findFragmentById(R.id.fr_list);
		if (!(fragmentById instanceof PhotoListFragment)) return null;
		return (PhotoListFragment) fragmentById;
	}

	/**
	 * @param request
	 * @return true if given request is not empty and is not equal to current request
	 * (stored at current NewsFragment)
	 */
	private boolean isSearchRequestApplicable(String request) {
		if (TextUtils.isEmpty(request)) return false;

		PhotoListFragment photoListFragment = getCurrentNewsFragment();
		String currentRequest = photoListFragment == null ? null : photoListFragment.getSearchRequest();
		return currentRequest == null || !currentRequest.equals(request);
	}

	/**
	 * Update text at searchView
	 * @param request - string that should replace the current one
	 */
	public void setSearchRequest(String request) {
		if (searchView == null) return;
		searchView.setQuery(request, false);
		searchView.clearFocus();
	}

	/**
	 * Add new fragment to Fragment manager and remove previous
	 * @param fragment a new fragment
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
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu_mian, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();

		//noinspection SimplifiableIfStatement
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
