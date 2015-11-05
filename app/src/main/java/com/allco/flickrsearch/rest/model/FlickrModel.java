package com.allco.flickrsearch.rest.model;

import java.util.List;

/**
 * POJO for using by Gson to parse JSON network response.
 * Example of JSON can be found here: https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=588459f7180bc9126823d9a2e6ba6637&text=android&format=json&nojsoncallback=1
 */

public class FlickrModel {

/*
	"photos": {
		"page": 1,
				"pages": 4163,
				"perpage": 100,
				"total": "416264",
				"photo": [
		{
			"id": "22767489222",
				"owner": "50764246@N06",
				"secret": "8783a3bfb7",
				"server": "5719",
				"farm": 6,
				"title": "2015-11-2 IDWF President Myrtle Witbooi earns Global Fairness Award",
				"ispublic": 1,
				"isfriend": 0,
				"isfamily": 0
		},
		*/

	static class PhotosContainer {
		int page;
		int pages;
		int total;
		List<Entry> photo;
	}

	public static class Entry {
		String id;
		String secret;
		String server;
		String farm;
		String title;

		public String getTitle() {

			return title;
		}

		public String getImageUrl() {

			return String.format("https://farm%s.staticflickr.com/%s/%s_%s.jpg", farm, server, id, secret);
		}

	}

	PhotosContainer photos;

	public List<Entry> getEntries() {

		return photos == null ? null : photos.photo;
	}

	public int getTotal() {
		return photos == null ? 0 : photos.total;
	}

}
