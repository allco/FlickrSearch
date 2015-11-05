package com.allco.flickrsearch;

import android.content.Context;
import android.content.res.Resources;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.allco.flickrsearch.rest.RestClient;
import com.allco.flickrsearch.rest.model.FlickrSearchResultModel;
import com.allco.flickrsearch.utils.BitmapBorderTransformer;
import com.allco.flickrsearch.utils.Tools;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

import static com.allco.flickrsearch.utils.Preconditions.checkArgument;
import static com.allco.flickrsearch.utils.Preconditions.checkNotNull;
import static com.allco.flickrsearch.utils.Preconditions.checkState;

/**
 * Adapter for store and handling data received from Google.
 * The first network request will be launched when {@link #reset(String,boolean)} will be called.
 * To load additional data the next network request will be instantiated when list item of type
 * ITEM_TYPE.PROGRESS will be created, and so on.
 * ITEM_TYPE.PROGRESS is won't be created when all data will be loaded or error occurs.
 */
public class PhotoListAdapter extends BaseAdapter implements AbsListView.OnScrollListener {

	public static final int PER_PAGE_COUNT = 10;
	private static final int ITEMS_COUNT_LIMIT = 10000;
	private final String TAG = this.getClass().getSimpleName();
	// Represent current request to Google.
	// null when there is no active request at this moment.
	private Call<FlickrSearchResultModel> currentCall;
	// The source of new currentCall.
	private final RestClient restClient;
	// size of Thumbs in pixels used for optimize Picasso loadings
	private final int sizeThumbPixels;

	private final Context ctx;
	private final Listener listener;
	private final BitmapBorderTransformer sBitmapTransformer; // transformer for Picasso
	private ArrayList<FlickrSearchResultModel.Photo> listEntries; // holder for news-items
	private boolean isFinished = false; // true when all data pages is loaded or error occurred
	private String request; // The search request.
	private boolean allowCachedContent = true;
	private int lastLoadedPageNumber;


	private enum ITEM_TYPE {REGULAR, PROGRESS} // type of view item

	/**
	 * Should be implemented by clients.
	 * Deliver data loading events.
	 */
	public interface Listener {

		/**
		 * Called when next data page is received
		 *
		 * @param adapter - "this" pointer
		 */
		void onPageLoaded(PhotoListAdapter adapter);
		/**
		 * Called when any error occurs
		 *
		 * @param adapter - "this" pointer
		 */
		void onError(PhotoListAdapter adapter);
	}

	/**
	 * Factory method for NewsAdapter
	 *
	 * @param ctx      Context instance
	 * @param listener Implemented by Client instance of {@link Listener}
	 * @return created instance of NewsAdapter
	 */
	public static PhotoListAdapter createNewsAdapter(Context ctx, Listener listener) {

		// access restClient singleton - the network request handler
		RestClient restClient = RestClient.Factory.getRestClient(ctx);

		Resources res = ctx.getResources();
		int sizeThumbPixels = res.getDimensionPixelSize(R.dimen.thumb_size);
		int sizeThumbRoundPixels = res.getDimensionPixelSize(R.dimen.thumb_round_corner_size);

		@SuppressWarnings("deprecation")
		BitmapBorderTransformer bitmapTransformer = new BitmapBorderTransformer(1 /*border size*/, sizeThumbRoundPixels, res.getColor(R.color.gray_light));
		return new PhotoListAdapter(ctx, sizeThumbPixels, bitmapTransformer, restClient, listener);
	}

	private PhotoListAdapter(Context ctx, int sizeThumbPixels, BitmapBorderTransformer bitmapTransformer, RestClient restClient, Listener listener) {

		checkArgument(sizeThumbPixels > 0);
		this.ctx = checkNotNull(ctx);
		this.restClient = checkNotNull(restClient);
		this.sBitmapTransformer = bitmapTransformer;
		this.sizeThumbPixels = sizeThumbPixels;
		this.listener = listener;
	}

	/**
	 * Erase previous state and initialize new data loading.
	 *
	 * @param request search request
	 * @return "this" pointer
	 */
	public PhotoListAdapter reset(String request, boolean allowCachedContent) {

		boolean notifyRequired = listEntries != null;

		this.allowCachedContent = allowCachedContent;
		this.listEntries = null;
		this.request = request;
		this.isFinished = false;

		if (notifyRequired) {
			notifyDataSetChanged();
		}

		tryStartDataRequest(true);
		return this;
	}

	/**
	 * Tries to start next network request if previous request is done.
	 *
	 * @param forceRestart if true current active request (if it exists) will be canceled and new one will be started
	 */
	private void tryStartDataRequest(boolean forceRestart) {

		if (isFinished()) return;
		if (currentCall != null) {
			if (!forceRestart) return;
			//currentCall.cancel();
			currentCall = null;
		}

		if (forceRestart){
			lastLoadedPageNumber = 0;
		}

		lastLoadedPageNumber++;

		// create new Call by restClient
		currentCall = restClient.createCallFlickrSearch(request, lastLoadedPageNumber, PER_PAGE_COUNT, allowCachedContent);
		// start request
		currentCall.enqueue
				(new Callback<FlickrSearchResultModel>() {
					final Call<FlickrSearchResultModel> call = currentCall;

					/**
					 * Called when request is successfully finished.
					 * @param response result of network request
					 * @param retrofit instance of Retrofit which was used to done this request
					 */
					@Override
					public void onResponse(Response<FlickrSearchResultModel> response, Retrofit retrofit) {

						if (call != currentCall) return;
						// if we got not HTTP_OK then report of error and exit
						if (response.code() != 200) onFailure(null);
						else {
							// get data
							FlickrSearchResultModel body = response.body();
							// deliver data to further handling
							onEntriesReceived(body);
						}
					}

					/**
					 * Called when any error occurred
					 * @param t contain information regarding the error
					 */
					@Override
					public void onFailure(Throwable t) {

						if (call != currentCall) return;
						// deliver error event to further handling
						Log.e(TAG, "onFailure() called with: ", t);

						onEntriesError();
					}
				});
	}

	/**
	 * Called when network data loading error occurred
	 */
	private void onEntriesError() {

		currentCall = null;
		isFinished = true;
		// deliver error event to Client
		if (listener != null) listener.onError(PhotoListAdapter.this);
		notifyDataSetChanged();
	}

	/**
	 * Called when next piece of network data is loaded
	 *
	 * @param body piece of data
	 */
	private void onEntriesReceived(FlickrSearchResultModel body) {

		List<FlickrSearchResultModel.Photo> entries = body == null ? null : body.getEntries();

		currentCall = null;
		// if no data, complete loading
		if (entries == null || entries.isEmpty()) {
			isFinished = true;
		} else {
			// if there is data - add data to inner storage
			if (listEntries == null) listEntries = new ArrayList<>();
			listEntries.addAll(entries);
			if (listEntries.size() >= ITEMS_COUNT_LIMIT || listEntries.size() >= body.getTotal()) {
				isFinished = true;
			}
		}

		// notify client
		if (listener != null) listener.onPageLoaded(PhotoListAdapter.this);
		notifyDataSetChanged();
	}

	/**
	 * @param position interested items position
	 * @return The item's url if it exist at given position
	 *//*
	public String getItemUrl(int position) {

		Object item = getItem(position);
		if (!(item instanceof FlickrSearchResultModel.Photo)) return null;
		return ((FlickrSearchResultModel.Photo) item).;
	}*/

	public String getItemPhotoUrl(int position) {
		Object item = getItem(position);
		if (!(item instanceof FlickrSearchResultModel.Photo)) return null;
		return ((FlickrSearchResultModel.Photo) item).getImageUrl();
	}

	public String getItemTitle(int position) {
		Object item = getItem(position);
		if (!(item instanceof FlickrSearchResultModel.Photo)) return null;
		return ((FlickrSearchResultModel.Photo) item).getTitle();
	}



	/**
	 * the View holder pattern implementation
	 */
	static class Holder {

		final ImageView icon;
		final TextView tvTitle;

		public Holder(View view) {

			icon = (ImageView) view.findViewById(R.id.iv_icon);
			tvTitle = (TextView) view.findViewById(R.id.tv_title);
			view.setTag(this);
		}

		public static Holder getHolder(View v) {

			return (Holder) v.getTag();
		}
	}

	/**
	 * Create and tune item view for ListView which represents received item from Google
	 *
	 * @param position    position of interested item
	 * @param convertView reusable view
	 * @return Created item root view
	 */
	private View getRegularItemView(int position, View convertView) {

		Holder h;
		if (convertView == null) {
			convertView = View.inflate(ctx, R.layout.fragment_photos_list_item, null);
			h = new Holder(convertView);
		} else h = Holder.getHolder(convertView);

		FlickrSearchResultModel.Photo entry = listEntries.get(position);
		String imageUrl = entry.getImageUrl();

		if (!TextUtils.isEmpty(imageUrl)) {

			Picasso.with(ctx)
					.load(imageUrl)
					.error(R.drawable.thumb_stub)
					.placeholder(R.drawable.thumb_stub)
					.resize(sizeThumbPixels, sizeThumbPixels)
					.transform(sBitmapTransformer)
					.centerCrop()
					.tag(ctx)
					.into(h.icon);

			h.icon.setVisibility(View.VISIBLE);
		} else {
			h.icon.setVisibility(View.INVISIBLE);
		}

		h.tvTitle.setText(Tools.fromHtml(entry.getTitle()));
		return convertView;
	}

	/**
	 * Get a View that displays the data at the specified position in the data set.
	 *
	 * @param position    The position of the item within the adapter's data set of the item whose view
	 *                    we want.
	 * @param convertView The old view to reuse, if possible.
	 * @param parent      The parent that this view will eventually be attached to
	 * @return A View corresponding to the data at the specified position.
	 */
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {

		// if current item is ITEM_TYPE.PROGRESS item
		if (getItemViewType(position) == ITEM_TYPE.PROGRESS.ordinal()) {
			checkState(!isFinished, "isFinished == true, and progress is visible");
			if (convertView == null)
				convertView = View.inflate(ctx, R.layout.fragment_photos_list_item_progress, null);
			// if PROGRESS item is created (is becoming to be visible) next network request should be instantiated
			tryStartDataRequest(false);
			return convertView;
		}

		return getRegularItemView(position, convertView);
	}


	/**
	 * @return true if all async tasks is completed and won't be launched until {@link #reset(String,boolean)} will be called
	 */
	public boolean isFinished() {

		return isFinished;
	}

	@Override
	public int getViewTypeCount() {

		return ITEM_TYPE.values().length;
	}

	/**
	 * Determine type of item for position.
	 * ITEM_TYPE.PROGRESS is always the last item in list
	 */
	@Override
	public int getItemViewType(int position) {

		if (listEntries == null || position >= listEntries.size()) return ITEM_TYPE.PROGRESS.ordinal();
		else return ITEM_TYPE.REGULAR.ordinal();
	}

	/**
	 * If {@link #isFinished()} return true, then return exact count of stored news-items,
	 * else returns count of stored news-items plus 1 - item (ITEM_TYPE.PROGRESS) that represent loading state.
	 */
	@Override
	public int getCount() {

		if (listEntries == null) return 0;
		int cnItems = listEntries.size();
		return cnItems + (isFinished() ? 0 : 1 /*progress item*/);
	}

	@Override
	public Object getItem(int position) {

		return listEntries == null ? null : listEntries.get(position);
	}

	@Override
	public long getItemId(int position) {

		return 0;
	}

	@Override
	public void onScrollStateChanged(AbsListView view, int scrollState) {

		final Picasso picasso = Picasso.with(ctx);
		if (scrollState == SCROLL_STATE_IDLE || scrollState == SCROLL_STATE_TOUCH_SCROLL) {
			picasso.resumeTag(ctx);
		} else {
			picasso.pauseTag(ctx);
		}
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

	}

}
