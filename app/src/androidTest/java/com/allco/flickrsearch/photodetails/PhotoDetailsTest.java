package com.allco.flickrsearch.photodetails;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.ImageView;
import android.widget.TextView;

import com.allco.flickrsearch.R;
import com.allco.flickrsearch.ioc.ApplicationComponent;
import com.allco.flickrsearch.ioc.ApplicationModule;
import com.allco.flickrsearch.ioc.IoC;
import com.allco.flickrsearch.utils.ImageLoader;
import com.flextrade.jfixture.annotations.Fixture;
import com.flextrade.jfixture.rules.FixtureRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Spy;

import javax.inject.Named;

import it.cosenonjaviste.daggermock.DaggerMockRule;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@RunWith(AndroidJUnit4.class)
public class PhotoDetailsTest {
    public static final String IGNORE_IT = "ignore_it";
    @Rule
    public final ActivityTestRule<PhotoDetailsActivity> activityRule = new ActivityTestRule<>(PhotoDetailsActivity.class, false, false);

    @Spy
    Context context = InstrumentationRegistry.getInstrumentation().getTargetContext().getApplicationContext();

    @Rule
    public MethodRule mockitoRule = new DaggerMockRule<>(ApplicationComponent.class, new ApplicationModule(context))
            .set(applicationComponent -> IoC.getInstance().setApplicationComponent(applicationComponent));

    @Mock
    ImageLoader imageLoader;

    @Rule
    public FixtureRule fixtureRule = FixtureRule.initFixtures();

    @Fixture
    @Named(IGNORE_IT)
    String imageTitle;

    @Fixture
    @Named(IGNORE_IT)
    String imageUrl;

    @Before
    public void setUp() throws Exception {
        activityRule.launchActivity(PhotoDetailsActivity.createIntent(context, imageTitle, imageUrl));
    }

    @After
    public void tearDown() throws Exception {
        activityRule.getActivity().finish();
    }

    @Test
    public void test_valid_title_shown_valid_imageUrl_loaded() {
        onView(allOf(isAssignableFrom(TextView.class), withId(R.id.tv_image_viewer_title))).
                check(matches(isDisplayed())).check(matches(withText(imageTitle)));
        verify(imageLoader).loadImage(any(ImageView.class), eq(imageUrl));
    }
}