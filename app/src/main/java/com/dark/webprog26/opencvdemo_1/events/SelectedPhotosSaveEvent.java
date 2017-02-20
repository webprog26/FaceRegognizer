package com.dark.webprog26.opencvdemo_1.events;

import android.graphics.Bitmap;

import java.util.List;

/**
 * Created by webpr on 17.02.2017.
 */

public class SelectedPhotosSaveEvent {
    //selected photos should be added to List<Bitmap>
    private final List<Bitmap> mBitmaps;

    public SelectedPhotosSaveEvent(List<Bitmap> mBitmaps) {
        this.mBitmaps = mBitmaps;
    }

    public List<Bitmap> getBitmaps() {
        return mBitmaps;
    }
}
