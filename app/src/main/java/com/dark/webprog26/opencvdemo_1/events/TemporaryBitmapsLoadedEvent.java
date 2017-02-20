package com.dark.webprog26.opencvdemo_1.events;

import android.graphics.Bitmap;

import java.util.List;

/**
 * Created by webpr on 16.02.2017.
 */

public class TemporaryBitmapsLoadedEvent {
    //when LabActivity is in resume state photos from temporary directory should be visible to the user
    private final List<Bitmap> bitmaps;

    public TemporaryBitmapsLoadedEvent(List<Bitmap> bitmaps) {
        this.bitmaps = bitmaps;
    }

    public List<Bitmap> getBitmaps() {
        return bitmaps;
    }
}
