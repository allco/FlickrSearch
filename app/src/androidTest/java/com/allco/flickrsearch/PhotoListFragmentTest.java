package com.allco.flickrsearch;

import android.support.annotation.NonNull;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.allco.flickrsearch.rest.RestClientImpl;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class PhotoListFragmentTest {

	// JSON preset fot Tests
	private static final String JSON_PRESET =
			"{\n" +
			"\"photos\":{\n" +
			"\"page\":1,\n" +
			"\"pages\":96050,\n" +
			"\"perpage\":10,\n" +
			"\"total\":\"960492\",\n" +
			"\"photo\":[\n" +
			"{\n" +
			"\"id\":\"22408121799\",\n" +
			"\"owner\":\"133231929@N05\",\n" +
			"\"secret\":\"5a41e793fc\",\n" +
			"\"server\":\"5749\",\n" +
			"\"farm\":6,\n" +
			"\"title\":\"Next Launcher 3D Shell v3.7.3 APK Download | Android Pro Full Apk\",\n" +
			"\"ispublic\":1,\n" +
			"\"isfriend\":0,\n" +
			"\"isfamily\":0\n" +
			"},\n" +
			"{\n" +
			"\"id\":\"22811503261\",\n" +
			"\"owner\":\"137223639@N03\",\n" +
			"\"secret\":\"29ee7bc678\",\n" +
			"\"server\":\"780\",\n" +
			"\"farm\":1,\n" +
			"\"title\":\"SNOWJINKS Android APK Free Download Game\",\n" +
			"\"ispublic\":1,\n" +
			"\"isfriend\":0,\n" +
			"\"isfamily\":0\n" +
			"},\n" +
			"{\n" +
			"\"id\":\"22800281525\",\n" +
			"\"owner\":\"130367003@N05\",\n" +
			"\"secret\":\"082a40ff4a\",\n" +
			"\"server\":\"5652\",\n" +
			"\"farm\":6,\n" +
			"\"title\":\"Meizu Pro 5 Mini with Helio X20 SoC & 3GB RAM Listed Online For \\u20ac360 http:\\/\\/ift.tt\\/1kvryvk\",\n" +
			"\"ispublic\":1,\n" +
			"\"isfriend\":0,\n" +
			"\"isfamily\":0\n" +
			"}" +
			"]" +
			"}" +
			"}";

	// Activity JUnit Rule
	@Rule
	public final ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);

	// a local Web-server
	private MockWebServer server;

	/**
	 * Initialize environment before execution of each Test
	 *
	 * @throws IOException
	 */
	@Before
	public void setUp() throws IOException {

		server = new MockWebServer();
		server.start();
		// Enable test case mode. Ever
		RestClientImpl.Factory.enableTestCase(server.url("/").toString());
	}

	/**
	 * Cleanup after each Test
	 *
	 * @throws IOException
	 */
	@After
	public void tearOff() throws IOException {

		server.shutdown();
		server = null;
	}

	/**
	 * Test will be passed if:
	 * ListView is not visible and appropriate text message with explanation is shown
	 * because HTTP_OK wasn't received.
	 *
	 * @throws Throwable
	 */
	@Test
	public void check_error_received_from_server() throws Throwable {

		server.enqueue(new MockResponse().setBody(JSON_PRESET).setResponseCode(400));
		IdlingResource idlingResource = simulateUserInputSearchRequest();
		Espresso.registerIdlingResources(idlingResource);

		// TextView at NewsFragment with error report should be visible
		onView(allOf(isAssignableFrom(TextView.class), withText(R.string.error_occurred))).check(matches(isDisplayed()));
		// List View at NewsFragment should be invisible
		onView(isAssignableFrom(ListView.class)).check(matches(not(isDisplayed())));

		Espresso.unregisterIdlingResources(idlingResource);
	}

	/**
	 * Test will be passed if:
	 * ListView is not visible and appropriate text message with explanation is shown
	 * because an invalid JSON is received.
	 *
	 * @throws Throwable
	 */
	@Test
	public void check_empty_list_received_from_server() throws Throwable {

		server.enqueue(new MockResponse().setBody("{}").setResponseCode(200));

		// click SearchView to focus it
		IdlingResource idlingResource = simulateUserInputSearchRequest();
		Espresso.registerIdlingResources(idlingResource);

		// TextView at NewsFragment with error report should be visible
		onView(allOf(isAssignableFrom(TextView.class), withText(R.string.nothing_found_try_other_request))).check(matches(isDisplayed()));
		// List View at NewsFragment should be invisible
		onView(isAssignableFrom(ListView.class)).check(matches(not(isDisplayed())));

		Espresso.unregisterIdlingResources(idlingResource);
	}

	/**
	 * Test will be passed if:
	 * ListView is not empty because a valid JSON is received.
	 *
	 * @throws Throwable
	 */
	@Test
	public void if_json_received_listView_should_be_non_empty() throws Throwable {

		server.enqueue(new MockResponse().setBody(JSON_PRESET).setResponseCode(200));
		server.enqueue(new MockResponse().setBody("{}").setResponseCode(200));

		IdlingResource idlingResource = simulateUserInputSearchRequest();
		Espresso.registerIdlingResources(idlingResource);

		// List View at NewsFragment should be visible and have at leat one argument
		onView(isAssignableFrom(ListView.class)).check(matches(isDisplayed())).check(matches(notEmptyAdapterView()));

		Espresso.unregisterIdlingResources(idlingResource);
	}

	/**
	 * Simulate user input in searchView request "test"
	 *
	 * @return {@code IdlingResource} that should be unregistered with {@code Espresso.unregisterIdlingResources(...)}
	 * at the end of the test
	 */
	@NonNull
	private IdlingResource simulateUserInputSearchRequest() {

		// click SearchView to focus it
		onView(withId(R.id.searchView)).perform(click());
		// type test text
		onView(allOf(isDescendantOfA(withId(R.id.searchView)), isAssignableFrom(EditText.class))).perform(typeText("test"));
		// launch search process
		onView(allOf(isDescendantOfA(withId(R.id.searchView)), withId(R.id.search_go_btn))).perform(click());

		return new IdlingResourceImpl(mActivityRule.getActivity().getCurrentPhotosFragment());
	}

	/**
	 * Custom matcher for AdapterView
	 */
	public static Matcher<View> notEmptyAdapterView() {

		return new TypeSafeMatcher<View>() {
			@Override
			public void describeTo(Description description) {

				description.appendText("has not empty adapter");
			}

			@Override
			public boolean matchesSafely(View view) {

				return view instanceof AdapterView && ((AdapterView) view).getCount() > 0;
			}
		};
	}

}