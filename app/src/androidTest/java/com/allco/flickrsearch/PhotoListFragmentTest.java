package com.allco.flickrsearch;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.allco.flickrsearch.ioc.ApplicationComponent;
import com.allco.flickrsearch.ioc.ApplicationModule;
import com.allco.flickrsearch.ioc.IoC;
import com.allco.flickrsearch.photolist.PhotoListModel;
import com.allco.flickrsearch.photolist.view.PhotoListFragment;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runner.RunWith;

import java.io.IOException;

import javax.inject.Named;

import it.cosenonjaviste.daggermock.DaggerMockRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.allco.flickrsearch.ioc.ApplicationModule.END_POINT;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;

@RunWith(AndroidJUnit4.class)
public class PhotoListFragmentTest {

    // Activity JUnit Rule
    @Rule
    public final ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class, false, false);

    @Rule
    public MethodRule mockitoRule;

    // a local Web-server
    private MockWebServer server = new MockWebServer();
    private String mockedResponseBody;

    @Named(END_POINT)
    private String endPoint;

    public PhotoListFragmentTest() {

        try {
            server.start();
            mockedResponseBody = AssetHelper.readAssetFile("mocked_response.json");
            assertNotNull(mockedResponseBody);
            assertFalse(mockedResponseBody.isEmpty());
            endPoint = server.url("/").toString();
            Context context = InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();
            mockitoRule = new DaggerMockRule<>(ApplicationComponent.class, new ApplicationModule(context))
                    .set(applicationComponent -> {
                        IoC.getInstance().setApplicationComponent(applicationComponent);
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @After
    public void tearOff() throws IOException {
        server.shutdown();
        server = null;
    }

    /**
     * Test will be passed if:
     * ListView is not visible and appropriate text message with explanation is shown
     * because HTTP_OK wasn't received.
     */
    @Test
    public void check_error_received_from_server() throws Throwable {

        server.enqueue(new MockResponse().setBody(mockedResponseBody).setResponseCode(400));
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

        server.enqueue(new MockResponse().setBody(mockedResponseBody).setResponseCode(200));
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

        // Start Activity
        activityRule.launchActivity(null);
        // click SearchView to focus it
        onView(withId(R.id.searchView)).perform(click());
        // type test text
        onView(allOf(isDescendantOfA(withId(R.id.searchView)), isAssignableFrom(EditText.class))).perform(typeText("test"));
        // launch search process
        onView(allOf(isDescendantOfA(withId(R.id.searchView)), withId(R.id.search_go_btn))).perform(click());

        PhotoListFragment photosFragment = activityRule.getActivity().getCurrentPhotosFragment();
        assertNotNull(photosFragment);
        PhotoListModel model = photosFragment.getPhotoListComponent().model();

        return new IdlingResourceImpl(model::isIdle);
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