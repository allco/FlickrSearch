package com.allco.flickrsearch.photolist.view;

import android.support.annotation.NonNull;

import com.allco.flickrsearch.rest.FlickrItemData;

import java.util.ArrayList;
import java.util.List;

public class PhotoListItemData {
    private final String imageUrl;
    private final String title;

    public PhotoListItemData(String imageUrl, String title) {
        this.imageUrl = imageUrl;
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getTitle() {
        return title;
    }

    @NonNull
    public static PhotoListItemData map(@NonNull FlickrItemData.Entry entry) {
        return new PhotoListItemData(entry.getImageUrl(), entry.getTitle());
    }

    @NonNull
    public static List<PhotoListItemData> map(@NonNull List<FlickrItemData.Entry> entryList) {
        ArrayList<PhotoListItemData> list = new ArrayList<>(entryList.size());
        for (FlickrItemData.Entry entry : entryList) {
            list.add(map(entry));
        }
        return list;
    }
}
