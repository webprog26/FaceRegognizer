package com.dark.webprog26.opencvdemo_1.events;

import com.dark.webprog26.opencvdemo_1.models.FaceModel;

import java.util.List;

/**
 * Created by webpr on 21.02.2017.
 */

public class FaceModelsLoadedEvent {
    //When faces detected earlier and stored on sd card are we form filters for recognizing
    private final List<FaceModel> faceModels;

    public FaceModelsLoadedEvent(List<FaceModel> faceModels) {
        this.faceModels = faceModels;
    }

    public List<FaceModel> getFaceModels() {
        return faceModels;
    }
}
