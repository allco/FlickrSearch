package com.allco.flickrsearch.utils;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;

import com.squareup.picasso.Transformation;

/**
 * Transformer implementation for {@link com.squareup.picasso.Picasso}.
 * Adds round corners and border to image.
 */
@SuppressWarnings("SameParameterValue")
public class BitmapBorderTransformer implements Transformation {
    private final int mBorderSize;
    private final int mCornerRadius;
    private final int mColor;

	/**
	 * Constructor.
	 * @param borderSize width of border in pixels
	 * @param cornerRadius radius of rounded corners in pixels
	 * @param color color of border
	 */
    public BitmapBorderTransformer(int borderSize, int cornerRadius, int color) {
        this.mBorderSize = borderSize;
        this.mCornerRadius = cornerRadius;
        this.mColor = color;
    }

	/**
	 * Called by {@link com.squareup.picasso.Picasso} every time when next bitmap loaded to transform it
	 * @param source original bitmap
	 * @return transformed bitmap
	 */
    @Override
    public Bitmap transform(Bitmap source) {
        int width = source.getWidth();
        int height = source.getHeight();

		// create new bitmap for transformation result
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);

		// create Canvas
        Canvas canvas = new Canvas(image);
		// fill image with transparent black color
        canvas.drawARGB(0, 0, 0, 0);

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        Rect rect = new Rect(0, 0, width, height);

		// omit corners
        if (this.mCornerRadius == 0) {
            canvas.drawRect(rect, paint);
        } else {
			// or prepare round corners
            canvas.drawRoundRect(new RectF(rect),
                    this.mCornerRadius, this.mCornerRadius, paint);
        }

		// copy original bitmap to prepared Canvas
        paint.setXfermode(new PorterDuffXfermode((PorterDuff.Mode.SRC_IN)));
        canvas.drawBitmap(source, rect, rect, paint);

		// draw borders if needed
        if (this.mBorderSize != 0) {
            paint.setXfermode(null);
            paint.setAntiAlias(true);
            paint.setColor(this.mColor);
            paint.setStrokeWidth(this.mBorderSize);
            paint.setStyle(Paint.Style.STROKE);
            canvas.drawRoundRect(new RectF(rect), this.mCornerRadius, this.mCornerRadius, paint);
        }

		// recycle original bitmap
        if (source != image) source.recycle();

        return image;
    }

	/**
	 * Returns a unique key for the transformation, used for caching purposes.
	 */
    @Override
    public String key() {
        return String.format("bitmapBorder(bs=%d, cr=%d, cl=%d)", this.mBorderSize, this.mCornerRadius, this.mColor);
    }
}



