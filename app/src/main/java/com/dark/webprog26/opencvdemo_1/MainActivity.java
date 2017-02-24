package com.dark.webprog26.opencvdemo_1;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.dark.webprog26.opencvdemo_1.ar.Filter;
import com.dark.webprog26.opencvdemo_1.ar.ImageDetectionFilter;
import com.dark.webprog26.opencvdemo_1.events.FaceModelsLoadedEvent;
import com.dark.webprog26.opencvdemo_1.events.FaceRecognizedEvent;
import com.dark.webprog26.opencvdemo_1.events.FacesDetectedEvent;
import com.dark.webprog26.opencvdemo_1.events.LoadFaceModelsEvent;
import com.dark.webprog26.opencvdemo_1.events.PhotosSavedToTemporaryDirEvent;
import com.dark.webprog26.opencvdemo_1.managers.BitmapManager;
import com.dark.webprog26.opencvdemo_1.models.FaceModel;
import com.dark.webprog26.opencvdemo_1.providers.DbProvider;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
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
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener{

    private static final String TAG = "MainActivity_TAG";
    private static final int NUMBER_OF_PHOTOS_TO_SAVE = 100;

    private CascadeClassifier cascadeClassifier;
    private Mat grayscaleImage;
    private int absoluteFaceSize;
    private boolean mIsDetecting;
    private boolean isFilterLoaded = false;
    private boolean mIsRecognizing;
    private List<Filter> mFilters;

    @BindView(R.id.jCvCamera)
    CameraBridgeViewBase mCameraView;
    @BindView(R.id.pbLoading)
    ProgressBar mPbLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //setting MainActivity to fullscreen mode
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        mCameraView.setVisibility(SurfaceView.VISIBLE);
        mCameraView.setCvCameraViewListener(this);
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
            runOnUiThread(new Runnable() {
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

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mCameraView != null){
            mCameraView.disableView();
        }
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        OpenCVLoader.initDebug();
        if(initializeOpenCVDependencies()){
            mCameraView.enableView();
            mFilters = new ArrayList<>();
            //load FaceModel instances from db in background thread
            EventBus.getDefault().post(new LoadFaceModelsEvent());
        }
    }

    /**
     * Loads FaceModel instances from db in background thread
     * @param loadFaceModelsEvent {@link LoadFaceModelsEvent}
     */
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onLoadFaceModelsEvent(LoadFaceModelsEvent loadFaceModelsEvent){
        EventBus.getDefault().post(new FaceModelsLoadedEvent(new DbProvider(this).getFaceModels()));
    }

    /**
     * Forms filter for recognizing persons
     * @param faceModelsLoadedEvent {@link FaceModelsLoadedEvent}
     */
    @Subscribe(threadMode = ThreadMode.BACKGROUND)
    public void onFaceModelsLoadedEvent(FaceModelsLoadedEvent faceModelsLoadedEvent){
        Log.i(TAG, "Loaded in background " + faceModelsLoadedEvent.getFaceModels().size() + " face models");
        for(FaceModel faceModel: faceModelsLoadedEvent.getFaceModels()){
            Log.i(TAG, "description: " + faceModel.getDescription()
                    + ", imageAddr: " + faceModel.getImageAddr());
            try{
                mFilters.add(new ImageDetectionFilter(faceModel.getImageAddr(),
                        BitmapManager.getBitmap(faceModel.getImageAddr()), faceModel.getDescription()));
            } catch (IOException ioe){
                ioe.printStackTrace();
            }
        }
        isFilterLoaded = true;
    }

    /**
     * Inits face detection via loading trained model file from assets
     * @return boolean
     */
    private boolean initializeOpenCVDependencies() {

        try {
            // Copy the resource into a temp file so OpenCV can load it
            InputStream is = getResources().openRawResource(R.raw.lbpcascade_frontalface);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.camera_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.actionDetect:
                mIsDetecting = true;
                return true;
            case R.id.actionRecognize:
                if(mFilters.size() > 0){
                    if(isFilterLoaded){
                        mIsRecognizing = true;
                    } else {
                        Toast.makeText(MainActivity.this, getString(R.string.filters_loading_message), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, getString(R.string.no_saved_models_detected_message), Toast.LENGTH_SHORT).show();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Saves detected faces to temporary directory
     * @param onFacesDetectedEvent {@link FacesDetectedEvent}
     */
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onFacesDetectedEvent(FacesDetectedEvent onFacesDetectedEvent){
        for(Mat mat: onFacesDetectedEvent.getDetectedFacesMats()){
            BitmapManager.saveTemporaryBitmap(mat);
        }
        EventBus.getDefault().post(new PhotosSavedToTemporaryDirEvent());
    }

    /**
     * Starts LabActivity, where detected faces will be visible to the user, so he will have possibility to choose correct ones
     * @param onPhotosSavedToTemporaryDirEvent {@link PhotosSavedToTemporaryDirEvent}
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onPhotosSavedToTemporaryDirEvent(PhotosSavedToTemporaryDirEvent onPhotosSavedToTemporaryDirEvent){
        Intent intent = new Intent(this, PhotoLabActivity.class);
        startActivity(intent);
    }

    /**
     * If persons has been recognized starts RecognizedActivity
     * @param faceRecognizedEvent {@link FaceRecognizedEvent}
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onFaceRecognizedEvent(FaceRecognizedEvent faceRecognizedEvent) {
        if(mPbLoading.getVisibility() == View.VISIBLE){
            mPbLoading.setVisibility(View.GONE);
        }
        if(faceRecognizedEvent.isRecognized()){
            Intent recognizedIntent = new Intent(this, RecognizedActivity.class);
            recognizedIntent.putExtra(RecognizedActivity.DETECTED_IMAGE_ADDR, faceRecognizedEvent.getImageAddr());
            recognizedIntent.putExtra(RecognizedActivity.DETECTED_OBJECT_TAG, faceRecognizedEvent.getTag());
            startActivity(recognizedIntent);
            return;
        }
    }
}
