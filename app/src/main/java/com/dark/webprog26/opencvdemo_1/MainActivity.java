package com.dark.webprog26.opencvdemo_1;

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
import com.dark.webprog26.opencvdemo_1.managers.OpenCvManager;
import com.dark.webprog26.opencvdemo_1.models.FaceModel;
import com.dark.webprog26.opencvdemo_1.providers.DbProvider;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity_TAG";

    private boolean isFilterLoaded = false;
    private List<Filter> mFilters;
    private OpenCvManager mOpenCvManager;

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
        mOpenCvManager = new OpenCvManager(this, mPbLoading);
        mCameraView.setVisibility(SurfaceView.VISIBLE);
        mCameraView.setCvCameraViewListener(mOpenCvManager);
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
        if(mOpenCvManager.initializeOpenCVDependencies()){
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
        mOpenCvManager.setFilters(mFilters);
        isFilterLoaded = true;
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
                mOpenCvManager.setIsDetecting(true);
                return true;
            case R.id.actionRecognize:
                if(mFilters.size() > 0){
                    if(isFilterLoaded){
                        mOpenCvManager.setIsRecognizing(true);
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
