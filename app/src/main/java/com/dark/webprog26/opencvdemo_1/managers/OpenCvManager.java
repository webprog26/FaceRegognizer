package com.dark.webprog26.opencvdemo_1.managers;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.dark.webprog26.opencvdemo_1.R;
import com.dark.webprog26.opencvdemo_1.ar.Filter;
import com.dark.webprog26.opencvdemo_1.events.FacesDetectedEvent;

import org.greenrobot.eventbus.EventBus;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by webpr on 02.03.2017.
 */

public class OpenCvManager implements CameraBridgeViewBase.CvCameraViewListener{

    private static final int NUMBER_OF_PHOTOS_TO_SAVE = 100;

    private final WeakReference<Context> mContextWeakReference;
    private CascadeClassifier cascadeClassifier;
    private Mat grayscaleImage;//
    private int absoluteFaceSize;//
    private boolean mIsDetecting;
    private List<Filter> mFilters;
    private boolean mIsRecognizing;
    private final ProgressBar mPbLoading;

    public OpenCvManager(Context context, ProgressBar pbLoading) {
        this.mContextWeakReference = new WeakReference<Context>(context);
        this.mPbLoading = pbLoading;
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        grayscaleImage = new Mat(height, width, CvType.CV_8UC4);

        // The faces will be a 20% of the height of the screen
        absoluteFaceSize = (int) (height * 0.2);
    }

    @Override
    public void onCameraViewStopped() {

    }

    @Override
    public Mat onCameraFrame(Mat inputFrame) {
        if(mIsDetecting){
            //start detect face via device camera
            if(mIsRecognizing){
                mIsRecognizing = false;
            }
            List<Mat> mats = new ArrayList<>();
            mIsDetecting = false;

            Imgproc.cvtColor(inputFrame, grayscaleImage, Imgproc.COLOR_RGBA2RGB);
            MatOfRect faces = new MatOfRect();

            if(cascadeClassifier != null){
                cascadeClassifier.detectMultiScale(grayscaleImage,
                        faces,
                        1.1, 2, 2,
                        new Size(absoluteFaceSize, absoluteFaceSize),
                        new Size());
            }

            int counter = 0;
            Rect[] facesArray = faces.toArray();
            Rect rectCrop = null;
            for (int i = 0; i < facesArray.length; i++){
                if(counter == NUMBER_OF_PHOTOS_TO_SAVE){
                    break;
                }
                //cropping the face from an image
                Imgproc.rectangle(inputFrame, facesArray[i].tl(), facesArray[i].br(), new Scalar(0, 255, 0, 255), 3);
                rectCrop = new Rect(facesArray[i].x + 3, facesArray[i].y + 3, facesArray[i].width - 6, facesArray[i].height - 6);
                Mat mat = inputFrame.submat(rectCrop);
                //add it to the list
                mats.add(mat);
                counter++;
            }

            if(mats.size() > 0){
                //saving detected images to temporary directory in background thread
                EventBus.getDefault().post(new FacesDetectedEvent(mats));
            }
        }

        if(mIsRecognizing){
            ((Activity)mContextWeakReference.get()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if(mPbLoading.getVisibility() == View.GONE){
                        mPbLoading.setVisibility(View.VISIBLE);
                    }
                }
            });

            if(mIsDetecting){
                mIsDetecting = false;
            }
            mIsRecognizing = false;
            //trying to recognize persons via preloaded filters
            for(Filter filter: mFilters){
                filter.apply(inputFrame, inputFrame);
            }
        }
        return inputFrame;
    }

    /**
     * Inits face detection via loading trained model file from assets
     * @return boolean
     */
    public boolean initializeOpenCVDependencies() {

        try {
            // Copy the resource into a temp file so OpenCV can load it
            final Context context = mContextWeakReference.get();
            InputStream is = context.getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "haarcascade_frontalcatface.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);


            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();

            // Load the cascade classifier
            cascadeClassifier = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            return true;
        } catch (Exception e) {
            Log.e("OpenCVActivity", "Error loading cascade", e);
            return false;
        }
    }

    public void setIsDetecting(boolean mIsDetecting) {
        this.mIsDetecting = mIsDetecting;
    }


    public void setIsRecognizing(boolean mIsRecognizing) {
        this.mIsRecognizing = mIsRecognizing;
    }

    public void setFilters(List<Filter> mFilters) {
        this.mFilters = mFilters;
    }
}
