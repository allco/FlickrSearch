package com.allco.flickrsearch;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkArgument;
import static android.support.test.espresso.core.deps.guava.base.Preconditions.checkNotNull;

/**
 * Activity that implements Entry viewer
 */
public class PhotoViewerActivity extends AppCompatActivity {

	private static final java.lang.String ARG_IMAGE_URL = "ARG_IMAGE_URL";
	private static final java.lang.String ARG_IMAGE_TITLE = "ARG_IMAGE_TITLE";

	/**
	 * Starts PhotoViewerActivity. All arguments are required
	 *
	 *
	 * @param ctx Context
	 * @param title title of Entry
	 * @param url photos URL of Entry
	 */
	public static void start(Context ctx, String title, String url) {

		// all arguments are required
		checkArgument(ctx != null);
		checkArgument(url != null);
		checkArgument(title != null);

		Intent intent = new Intent(ctx, PhotoViewerActivity.class);
		intent.putExtra(ARG_IMAGE_URL, url);
		intent.putExtra(ARG_IMAGE_TITLE, title);

		if (ctx != null) {
			ctx.startActivity(intent);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photo_viewer);

		// set up toolbar
		final Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		if (toolbar != null) setSupportActionBar(toolbar);

		// enable 'Up button' at toolbar
		ActionBar actionBar = getSupportActionBar();
		if (actionBar != null) {
			actionBar.setDisplayHomeAsUpEnabled(true);
		}

		Intent intent = checkNotNull(getIntent());
		String url = intent.getStringExtra(ARG_IMAGE_URL);
		String title = intent.getStringExtra(ARG_IMAGE_TITLE);

		ImageView ivImage = (ImageView) findViewById(R.id.iv_image_viewer_image);
		TextView tvTitle = (TextView) findViewById(R.id.tv_image_viewer_title);

		// setup title
		tvTitle.setText(title);

		// if Entry's photo URL is not empty, load image
		if (!TextUtils.isEmpty(url)) {

			DisplayMetrics displayMetrics = getApplicationContext().getResources().getDisplayMetrics();
			int szImage = displayMetrics.widthPixels;

			Picasso.with(this)
				   .load(url)
					.resize(szImage, szImage) // fit image to square
					.centerInside()
					.into(ivImage);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		// if 'Up button' is pressed
		if (item.getItemId() == android.R.id.home)
		{
			this.onBackPressed();
			return true;
		}

		return super.onOptionsItemSelected(item);
	}
}
