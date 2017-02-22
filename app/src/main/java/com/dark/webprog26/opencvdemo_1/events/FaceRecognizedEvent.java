package com.dark.webprog26.opencvdemo_1.events;

/**
 * Created by webpr on 21.02.2017.
 */

public class FaceRecognizedEvent {

    private final boolean isRecognized;
    private final String mTag;
    private final String mImageAddr;

    public FaceRecognizedEvent(boolean isRecognized, String tag, String imageAddr) {
        this.isRecognized = isRecognized;
        this.mTag = tag;
        this.mImageAddr = imageAddr;
    }

    public boolean isRecognized() {
        return isRecognized;
    }

    public String getTag() {
        return mTag;
    }

    public String getImageAddr() {
        return mImageAddr;
    }
}
