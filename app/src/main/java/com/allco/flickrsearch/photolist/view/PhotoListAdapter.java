package com.allco.flickrsearch.photolist.view;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.allco.flickrsearch.R;
import com.allco.flickrsearch.utils.ImageLoader;
import com.allco.flickrsearch.utils.Utils;

import java.lang.annotation.Retention;
import java.util.ArrayList;
import java.util.List;

import static java.lang.annotation.RetentionPolicy.SOURCE;

public class PhotoListAdapter extends BaseAdapter implements AbsListView.OnScrollListener {

    // limit fot total count of items
    private static final int ITEMS_COUNT_LIMIT = 10000;

    private static final int TYPE_REGULAR = 0;
    private static final int TYPE_SPINNER = 1;
    private static final int TYPE_COUNT = 2;
    @Nullable
    private GetMorePagesAvailable getMorePagesAvailable;
    @Nullable
    private Runnable tryRequestNextPage;

    @Retention(SOURCE)
    @IntDef({TYPE_REGULAR, TYPE_SPINNER})
    @interface TypeItem {
    }

    @NonNull
    private final Context ctx;
    @NonNull
    private ImageLoader imageLoader;

    private ArrayList<PhotoListItemData> listEntries;

    public interface GetMorePagesAvailable {
        boolean get();
    }

    public PhotoListAdapter(@NonNull Context ctx,
                            @NonNull ImageLoader imageLoader) {
        this.ctx = ctx;
        this.imageLoader = imageLoader;
    }

    public void reset(@NonNull GetMorePagesAvailable getMorePagesAvailable, @NonNull Runnable requestNextPage) {
        listEntries = null;
        this.getMorePagesAvailable = getMorePagesAvailable;
        this.tryRequestNextPage = requestNextPage;
        notifyDataSetChanged();
    }

    public void addData(List<PhotoListItemData> entries) {
        if (listEntries == null) {
            listEntries = new ArrayList<>();
        }

        listEntries.addAll(entries);
        notifyDataSetChanged();
    }

    private static class Holder {

        final ImageView icon;
        final TextView tvTitle;

        Holder(View view) {
            icon = (ImageView) view.findViewById(R.id.iv_icon);
            tvTitle = (TextView) view.findViewById(R.id.tv_title);
            view.setTag(this);
        }

        static Holder getHolder(View v) {
            return (Holder) v.getTag();
        }
    }

    private View getRegularItemView(int position, View convertView) {

        Holder h;
        // View holder pattern implementation
        if (convertView == null) {
            convertView = View.inflate(ctx, R.layout.fragment_photos_list_item, null);
            h = new Holder(convertView);
        } else {
            h = Holder.getHolder(convertView);
        }

        // data object
        PhotoListItemData entry = listEntries.get(position);
        // photo URL
        String imageUrl = entry.getImageUrl();

        // if URL exists, then load image
        if (!TextUtils.isEmpty(imageUrl)) {
            imageLoader.loadImage(h.icon, imageUrl);
            h.icon.setVisibility(View.VISIBLE);
        } else {
            // or hide imageView
            h.icon.setVisibility(View.INVISIBLE);
        }

        // HTML injection protection
        h.tvTitle.setText(Utils.fromHtml(entry.getTitle()));

        return convertView;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        // if current item is ITEM_TYPE.PROGRESS item
        if (getItemViewType(position) == TYPE_SPINNER) {
            if (convertView == null) {
                convertView = View.inflate(ctx, R.layout.fragment_photos_list_item_progress, null);
            }

            // if PROGRESS item is created (is becoming to be visible) next network request should be instantiated
            if (tryRequestNextPage != null) {
                tryRequestNextPage.run();
            }

            return convertView;
        }

        return getRegularItemView(position, convertView);
    }

    @Override
    public int getViewTypeCount() {
        return TYPE_COUNT;
    }

    @TypeItem
    @Override
    public int getItemViewType(int position) {
        if (listEntries == null || position >= listEntries.size()) {
            return TYPE_SPINNER;
        } else {
            return TYPE_REGULAR;
        }
    }

    @Override
    public int getCount() {
        if (getMorePagesAvailable == null || listEntries == null) {
            return 0;
        }
        return Math.min(ITEMS_COUNT_LIMIT, listEntries.size() + (getMorePagesAvailable.get() ? 1 /*progress item*/ : 0));
    }

    @Override
    public PhotoListItemData getItem(int position) {
        return listEntries == null ? null : listEntries.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {
        if (scrollState == SCROLL_STATE_IDLE || scrollState == SCROLL_STATE_TOUCH_SCROLL) {
            imageLoader.resume();
        } else {
            imageLoader.pause();
        }
    }

    @Override
    public void onScroll(final AbsListView view, final int firstVisibleItem, final int visibleItemCount, final int totalItemCount) {

    }
}
