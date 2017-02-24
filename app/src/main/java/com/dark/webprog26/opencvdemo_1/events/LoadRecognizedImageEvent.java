package com.dark.webprog26.opencvdemo_1.events;

/**
 * Created by webpr on 22.02.2017.
 */

public class LoadRecognizedImageEvent {
    //When person has been successfully recognized, it's image file address on sd card will be used
    //to upload this image in RecognizedAcrivity
    private final String mImageAddr;

    public LoadRecognizedImageEvent(String imageAddr) {
        this.mImageAddr = imageAddr;
    }

    public String getImageAddr() {
        return mImageAddr;
    }
}
