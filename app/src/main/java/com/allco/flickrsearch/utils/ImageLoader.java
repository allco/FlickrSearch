package com.allco.flickrsearch.utils;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.widget.ImageView;

import com.allco.flickrsearch.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.RequestCreator;
import com.squareup.picasso.Transformation;

public class ImageLoader {

    @NonNull
    private Context context;
    @Nullable
    private Transformation transformation;
    private int sizeThumb;

    public ImageLoader(@NonNull Context context, @Nullable Transformation transformation, final int sizeThumb) {
        this.context = context;
        this.sizeThumb = sizeThumb;
        this.transformation = transformation;
    }

    public void loadImage(@NonNull final ImageView imageView, @NonNull final String imageUrl) {
        RequestCreator requestCreator = Picasso.with(context)
                .load(imageUrl)
                .error(R.drawable.thumb_stub)
                .placeholder(R.drawable.thumb_stub)
                .resize(sizeThumb, sizeThumb);

        if (transformation != null) {
            requestCreator
                    .transform(transformation);
        }

        requestCreator.centerCrop()
                .tag(context)
                .into(imageView);
    }

    public void pause() {
        Picasso picasso = Picasso.with(context);
        picasso.pauseTag(context);
    }

    public void resume() {
        Picasso.with(context).resumeTag(context);
    }
}
