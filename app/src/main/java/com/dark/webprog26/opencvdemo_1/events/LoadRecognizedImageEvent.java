package com.dark.webprog26.opencvdemo_1.events;

/**
 * Created by webpr on 22.02.2017.
 */

public class LoadRecognizedImageEvent {

    private final String mImageAddr;


    public LoadRecognizedImageEvent(String imageAddr) {
        this.mImageAddr = imageAddr;
    }

    public String getImageAddr() {
        return mImageAddr;
    }
}
