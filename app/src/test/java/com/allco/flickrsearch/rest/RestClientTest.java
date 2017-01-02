package com.allco.flickrsearch.rest;

import com.allco.flickrsearch.AssetHelper;
import com.allco.flickrsearch.FlickrsearchApplication;
import com.allco.flickrsearch.ioc.ApplicationComponent;
import com.allco.flickrsearch.ioc.ApplicationModule;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.After;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.MethodRule;

import java.io.IOException;
import java.util.List;

import javax.inject.Named;

import it.cosenonjaviste.daggermock.DaggerMockRule;
import retrofit.Call;
import retrofit.Response;

import static com.allco.flickrsearch.ioc.ApplicationModule.END_POINT;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Mockito.mock;

public class RestClientTest {

    private MockWebServer server;

    @Named(END_POINT)
    private String endPoint;

    @Rule
    public MethodRule mockitoRule;

    private RestClient restClient;

    public RestClientTest() {

        try {
            String mockedResponse = AssetHelper.readAssetFile("mocked_response.json");
            assertNotNull(mockedResponse);
            assertFalse(mockedResponse.isEmpty());

            server = new MockWebServer();
            server.enqueue(new MockResponse().setBody(mockedResponse).setResponseCode(200));

            server.start();

            endPoint = server.url("/").toString();

            mockitoRule = new DaggerMockRule<>(ApplicationComponent.class, new ApplicationModule(mock(FlickrsearchApplication.class)))
                    .set(applicationComponent -> restClient = applicationComponent.restClient());
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
     * The Test will be passed if:
     * 1. {@link RestClient} is created.
     * 2. Request executed
     * 3. Data received and successfully parsed
     *
     * @throws Exception
     */

    @Test
    public void test_flickr_service() throws Exception {

        // create restClient
        assertThat("restClinet is created", restClient, notNullValue());

        // create a flickr request
        Call<FlickrItemData> request = restClient.createCallFlickrSearch("request", 0, 0, false);
        // perform the request
        Response<FlickrItemData> response = request.execute();

        // response should be valid
        assertThat("response != null", response, notNullValue());
        assertThat("response is 200", response.code(), is(200));

        // response should contain valid body
        FlickrItemData model = response.body();
        assertThat("model != null", model, notNullValue());

        // body should contain valid list of entries
        List<FlickrItemData.Entry> entries = model.getEntries();
        assertThat("entries != null", entries, notNullValue());
        assertThat("entries.size()", entries, IsCollectionWithSize.hasSize(3));

        // each entry should be valid too
        checkEntry(entries, 0);
        checkEntry(entries, 1);
        checkEntry(entries, 2);
    }

    /**
     * Check validity of entry at position {@code index}
     *
     * @param entries list entries
     * @param index   position of entry for check
     */
    private void checkEntry(List<FlickrItemData.Entry> entries, int index) {
        FlickrItemData.Entry entry = entries.get(index);
        assertThat("entry != null " + index, entry, notNullValue());
        assertThat("entry.imageUrl " + index, entry.getImageUrl(), not(isEmptyOrNullString()));
        assertThat("entry.title " + index, entry.getTitle(), not(isEmptyOrNullString()));
    }
}