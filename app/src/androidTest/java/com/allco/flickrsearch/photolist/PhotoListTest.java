package com.allco.flickrsearch.photolist;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.UiThreadTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.allco.flickrsearch.MainActivity;
import com.allco.flickrsearch.R;
import com.allco.flickrsearch.ioc.ApplicationComponent;
import com.allco.flickrsearch.ioc.ApplicationModule;
import com.allco.flickrsearch.ioc.IoC;
import com.allco.flickrsearch.rest.FlickrItemData;
import com.allco.flickrsearch.utils.ImageLoader;
import com.flextrade.jfixture.JFixture;
import com.google.gson.reflect.TypeToken;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;

import java.util.List;

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
import static com.allco.flickrsearch.R.id.searchView;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;

@RunWith(AndroidJUnit4.class)
public class PhotoListTest {

    @Rule
    public final ActivityTestRule<MainActivity> activityRule = new ActivityTestRule<>(MainActivity.class, false, false);

    @Rule
    public UiThreadTestRule uiThreadTestRule = new UiThreadTestRule();

    @Spy
    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();

    @Rule
    public MethodRule mockitoRule = new DaggerMockRule<>(ApplicationComponent.class, new ApplicationModule(context))
            .set(applicationComponent -> IoC.getInstance().setApplicationComponent(applicationComponent));

    // Prevent real network requests for loading data
    @Mock
    public ImageLoader imageLoader;
    @Mock
    public PhotoListModel photoListModel;

    private PhotoListModel.OnDataAvailableListener onDataAvailableListener;
    private PhotoListModel.OnDataErrorListener onDataErrorListener;

    @Before
    public void setUp() {
        // save some listeners as fields
        doAnswer(invocation -> {
            onDataAvailableListener = invocation.getArgument(1);
            onDataErrorListener = invocation.getArgument(2);
            return null;
        }).when(photoListModel).tryLoadNextPage(anyBoolean(), any(), any());

        // Simulate user input in searchView request "test":
        // 1. Start Activity
        activityRule.launchActivity(null);
        // 2. Click SearchView to focus it
        onView(withId(searchView)).perform(click());
        // 3. type "test" text
        onView(allOf(isDescendantOfA(withId(searchView)), isAssignableFrom(EditText.class))).perform(typeText("test"));
        // 4. launch search process
        onView(allOf(isDescendantOfA(withId(searchView)), ViewMatchers.withId(R.id.search_go_btn))).perform(click());

        assertNotNull(activityRule.getActivity().getCurrentPhotosFragment());
        assertNotNull(onDataAvailableListener);
        assertNotNull(onDataErrorListener);
    }

    @After
    public void tearOff() {
        activityRule.getActivity().finish();
    }

    /**
     * Test will be passed if:
     * ListView is not visible and appropriate text message with explanation is shown
     * because some network error is happened.
     */
    @Test
    public void check_error_received_from_server() throws Throwable {
        // Emulate the receiving of any non HTTP_OK code
        uiThreadTestRule.runOnUiThread(() -> onDataErrorListener.onDataError());

        // TextView at NewsFragment with error report should be visible
        onView(allOf(isAssignableFrom(TextView.class), withText(R.string.error_occurred))).check(matches(isDisplayed()));
        // List View at NewsFragment should be invisible
        onView(isAssignableFrom(ListView.class)).check(matches(not(isDisplayed())));
    }

    /**
     * Test will be passed if:
     * ListView is not visible and appropriate text message with explanation is shown
     * because an empty list of items is received.
     *
     * @throws Throwable
     */
    @Test
    public void check_empty_list_received_from_server() throws Throwable {
        doReturn(true).when(photoListModel).isFinished();
        // Emulate the receiving of HTTP_OK code and empty list
        uiThreadTestRule.runOnUiThread(() -> onDataAvailableListener.onDataAvailable(null));

        InstrumentationRegistry.getInstrumentation().waitForIdleSync();

        // TextView at NewsFragment with error report should be visible
        onView(allOf(isAssignableFrom(TextView.class), withText(R.string.nothing_found_try_other_request))).check(matches(isDisplayed()));
        // List View at NewsFragment should be invisible
        onView(isAssignableFrom(ListView.class)).check(matches(not(isDisplayed())));
    }

    /**
     * Test will be passed if:
     * ListView is not empty because a valid list of items is received.
     *
     * @throws Throwable
     */
    @Test
    public void if_json_received_listView_should_be_non_empty() throws Throwable {
        List<FlickrItemData.Entry> entryList = new JFixture().create(new TypeToken<List<FlickrItemData.Entry>>() {}.getType());
        // Emulate the receiving of any non HTTP_OK code
        uiThreadTestRule.runOnUiThread(() -> onDataAvailableListener.onDataAvailable(entryList));

        // List View at NewsFragment should be visible and have proper amount of items
        onView(isAssignableFrom(ListView.class)).check(matches(isDisplayed())).check(matches(adapterViewHasElements(entryList.size() + 1 /* a "loading" item */)));
    }

    /**
     * Custom matcher for AdapterView
     */
    public static Matcher<View> adapterViewHasElements(int countOfElements) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {

                description.appendText("has not empty adapter");
            }

            @Override
            public boolean matchesSafely(View view) {

                return view instanceof AdapterView && ((AdapterView) view).getCount() == countOfElements;
            }
        };
    }
}