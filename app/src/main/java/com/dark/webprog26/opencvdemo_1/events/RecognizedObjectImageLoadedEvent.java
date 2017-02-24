package com.dark.webprog26.opencvdemo_1.events;

import android.graphics.Bitmap;

/**
 * Created by webpr on 22.02.2017.
 */

public class RecognizedObjectImageLoadedEvent {
    //When image has been loaded in background thread it should be visible to the user
    private final Bitmap mRecognizedObjectBitmap;

    public RecognizedObjectImageLoadedEvent(Bitmap mRecognizedObjectBitmap) {
        this.mRecognizedObjectBitmap = mRecognizedObjectBitmap;
    }

    public Bitmap getRecognizedObjectBitmap() {
        return mRecognizedObjectBitmap;
    }
}
