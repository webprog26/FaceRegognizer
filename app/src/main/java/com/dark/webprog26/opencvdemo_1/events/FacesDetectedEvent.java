package com.dark.webprog26.opencvdemo_1.events;

import org.opencv.core.Mat;

import java.util.List;

/**
 * Created by webpr on 15.02.2017.
 */

public class FacesDetectedEvent {
    //when detected some faces add them to the List<Mat>
    private final List<Mat> detectedFacesMats;

    public FacesDetectedEvent(List<Mat> detectedFacesMats) {
        this.detectedFacesMats = detectedFacesMats;
    }

    public List<Mat> getDetectedFacesMats(){
        return detectedFacesMats;
    }
}
