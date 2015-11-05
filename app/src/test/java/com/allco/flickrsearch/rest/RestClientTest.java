package com.allco.flickrsearch.rest;

import com.allco.flickrsearch.rest.model.FlickrModel;
import com.squareup.okhttp.mockwebserver.MockResponse;
import com.squareup.okhttp.mockwebserver.MockWebServer;

import org.hamcrest.collection.IsCollectionWithSize;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import retrofit.Call;
import retrofit.Response;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isEmptyOrNullString;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;

public class RestClientTest {

	// JSON preset fot Tests
	private static final String JSON_PRESET =
			"{\n"+
			"\"photos\":{\n"+
				"\"page\":1,\n"+
				"\"pages\":96050,\n"+
				"\"perpage\":10,\n"+
				"\"total\":\"960492\",\n"+
				"\"photo\":[\n"+
								"{\n"+
								"\"id\":\"22408121799\",\n"+
								"\"owner\":\"133231929@N05\",\n"+
								"\"secret\":\"5a41e793fc\",\n"+
								"\"server\":\"5749\",\n"+
								"\"farm\":6,\n"+
								"\"title\":\"Next Launcher 3D Shell v3.7.3 APK Download | Android Pro Full Apk\",\n"+
								"\"ispublic\":1,\n"+
								"\"isfriend\":0,\n"+
								"\"isfamily\":0\n"+
								"},\n"+
								"{\n"+
								"\"id\":\"22811503261\",\n"+
								"\"owner\":\"137223639@N03\",\n"+
								"\"secret\":\"29ee7bc678\",\n"+
								"\"server\":\"780\",\n"+
								"\"farm\":1,\n"+
								"\"title\":\"SNOWJINKS Android APK Free Download Game\",\n"+
								"\"ispublic\":1,\n"+
								"\"isfriend\":0,\n"+
								"\"isfamily\":0\n"+
								"},\n"+
								"{\n"+
								"\"id\":\"22800281525\",\n"+
								"\"owner\":\"130367003@N05\",\n"+
								"\"secret\":\"082a40ff4a\",\n"+
								"\"server\":\"5652\",\n"+
								"\"farm\":6,\n"+
								"\"title\":\"Meizu Pro 5 Mini with Helio X20 SoC & 3GB RAM Listed Online For \\u20ac360 http:\\/\\/ift.tt\\/1kvryvk\",\n"+
								"\"ispublic\":1,\n"+
								"\"isfriend\":0,\n"+
								"\"isfamily\":0\n"+
								"}" +
							"]" +
						"}" +
			"}";



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
		server.enqueue(new MockResponse().setBody(JSON_PRESET).setResponseCode(200));
		server.start();
		RestClient.Factory.enableTestCase(server.url("/").toString());
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
	 * The Test will be passed if:
	 * 1. {@link com.allco.flickrsearch.rest.RestClient} is created.
	 * 2. Request executed
	 * 3. Data received and successfully parsed
	 *
	 * @throws Exception
	 */
	@Test
	public void test_flickr_service() throws Exception {

		System.out.println(JSON_PRESET);

		// create restClient
		RestClient restClient = RestClient.Factory.getRestClient(null);
		assertThat("restClinet is created", restClient, notNullValue());

		// create a flickr request
		Call<FlickrModel> request = restClient.createCallFlickrSearch("request", 0, 0, false);
		// perform the request
		Response<FlickrModel> response = request.execute();

		// response should be valid
		assertThat("response != null", response, notNullValue());
		assertThat("response is 200", response.code(), is(200));

		// reponse should contain valid body
		FlickrModel model = response.body();
		assertThat("model != null", model, notNullValue());

		// body should contain valid list of entries
		List<FlickrModel.Entry> entries = model.getEntries();
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
	 * @param index position of entry for check
	 */
	private void checkEntry(List<FlickrModel.Entry> entries, int index) {
		FlickrModel.Entry entry = entries.get(index);
		assertThat("entry != null " + index, entry, notNullValue());
		assertThat("entry.imageUrl "  + index, entry.getImageUrl(), not(isEmptyOrNullString()));
		assertThat("entry.title " + index, entry.getTitle(), not(isEmptyOrNullString()));
	}
}