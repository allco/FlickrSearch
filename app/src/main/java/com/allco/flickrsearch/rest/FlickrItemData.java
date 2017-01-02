package com.allco.flickrsearch.rest;

import java.util.List;

/**
 * POJO for using by Gson to parse JSON network response.
 * Example of JSON can be found here: https://api.flickr.com/services/rest/?method=flickr.photos.search&api_key=588459f7180bc9126823d9a2e6ba6637&text=android&format=json&nojsoncallback=1
 */

public class FlickrItemData {

    private PhotosContainer photos;

    public PhotosContainer getPhotos() {
        return photos;
    }

    public void setPhotos(final PhotosContainer photos) {
        this.photos = photos;
    }

	static class PhotosContainer {
        private int total;
        private List<Entry> photo;

        public int getTotal() {
            return total;
        }

        public void setTotal(final int total) {
            this.total = total;
        }

        public List<Entry> getPhoto() {
            return photo;
        }

        public void setPhoto(final List<Entry> photo) {
            this.photo = photo;
        }
    }

	public static class Entry {
        private String id;
        private String secret;
        private String server;
        private String farm;
        private String title;

		public String getTitle() {

			return title;
		}

		public String getImageUrl() {

			return String.format("https://farm%s.staticflickr.com/%s/%s_%s.jpg", farm, server, id, secret);
		}

        public String getId() {
            return id;
        }

        public String getSecret() {
            return secret;
        }

        public String getServer() {
            return server;
        }

        public String getFarm() {
            return farm;
        }

        public void setId(final String id) {
            this.id = id;
        }

        public void setSecret(final String secret) {
            this.secret = secret;
        }

        public void setServer(final String server) {
            this.server = server;
        }

        public void setFarm(final String farm) {
            this.farm = farm;
        }

        public void setTitle(final String title) {
            this.title = title;
        }
    }

	public List<Entry> getEntries() {

		return photos == null ? null : photos.photo;
	}

	public int getTotal() {
		return photos == null ? 0 : photos.total;
	}
}
