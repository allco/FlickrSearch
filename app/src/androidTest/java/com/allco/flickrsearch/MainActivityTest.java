package com.allco.flickrsearch;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.allco.flickrsearch.photolist.PhotoListFragment;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.core.IsNot.not;
import static org.hamcrest.core.IsNull.notNullValue;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

	@Rule
	public final ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

	/**
	 * The Test will be passed if:
	 * All initial states of controls are valid
	 *
	 * @throws Throwable
	 */
	@Test
	public void check_initialState() throws Exception {
		// MainActivity should exist
		MainActivity mainActivity = mActivityRule.getActivity();
		assertThat(mainActivity, notNullValue());

		// MainActivity should contain a visible searchView
		onView(withId(R.id.searchView)).check(matches(isDisplayed()));
		// a PhotoListFragment should be created and attached to activity
		PhotoListFragment currentNewsFragment = mainActivity.getCurrentPhotosFragment();
		assertThat(currentNewsFragment, notNullValue());

		// TextView at PhotoListFragment with prompt should be visible
		onView(allOf(isAssignableFrom(TextView.class), withText(R.string.please_enter_text_search))).check(matches(isDisplayed()));
		// List View at PhotoListFragment should be invisible
		onView(isAssignableFrom(ListView.class)).check(matches(not(isDisplayed())));
	}

	/**
	 * The Test will be passed if:
	 * The 'Go button' is visible if request typed
	 *
	 * @throws Throwable
	 */
	@Test
	public void searchViews_goButton_should_become_visible_only_if_request_typed() throws Throwable {

		// GoButton (a child of searchView) shouldn't be visible
		onView(allOf(isDescendantOfA(withId(R.id.searchView)), withId(R.id.search_go_btn))).check(matches(not(isDisplayed())));

		// click SearchView to focus it
		onView(withId(R.id.searchView)).perform(click());
		// type test text
		onView(allOf(isDescendantOfA(withId(R.id.searchView)), isAssignableFrom(EditText.class))).perform(typeText("test"));

		// GoButton should be visible now
		onView(allOf(isDescendantOfA(withId(R.id.searchView)), withId(R.id.search_go_btn))).check(matches(isDisplayed()));
	}



}