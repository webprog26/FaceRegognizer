package com.dark.webprog26.opencvdemo_1.models;

/**
 * Created by webpr on 20.02.2017.
 */

public class FaceModel {

    private long mId;
    private String mDescription;
    private String mImageAddr;

    public long getId() {
        return mId;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getImageAddr() {
        return mImageAddr;
    }

    public static Builder newBuilder(){
        return new FaceModel(). new Builder();
    }

    public class Builder{

        public Builder setId(long id){
            FaceModel.this.mId = id;
            return this;
        }

        public Builder setDescription(String description){
            FaceModel.this.mDescription = description;
            return this;
        }

        public Builder setImageAddr(String imageAddr){
            FaceModel.this.mImageAddr = imageAddr;
            return this;
        }

        public FaceModel build(){
            return FaceModel.this;
        }
    }
}
