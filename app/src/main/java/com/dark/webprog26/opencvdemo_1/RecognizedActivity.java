package com.dark.webprog26.opencvdemo_1;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.TextView;

import com.dark.webprog26.opencvdemo_1.events.LoadRecognizedImageEvent;
import com.dark.webprog26.opencvdemo_1.events.RecognizedObjectImageLoadedEvent;
import com.dark.webprog26.opencvdemo_1.managers.BitmapManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

public class RecognizedActivity extends AppCompatActivity {

    private static final String TAG = "RecognizedActivity";

    public static final String DETECTED_IMAGE_ADDR = "com.dark.webprog26.opencvdemo_1.detected_image_addr";
    public static final String DETECTED_OBJECT_TAG = "com.dark.webprog26.opencvdemo_1.detected_object_tag";

    @BindView(R.id.ivRecognizedObjectImage)
    ImageView mIvRecognizedObjectImage;
    @BindView(R.id.tvDetectedObjectTag)
    TextView mTvDetectedObjectTag;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognized);
        ButterKnife.bind(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String imageAddrString = getIntent().getStringExtra(DETECTED_IMAGE_ADDR);
        if(imageAddrString != null){
            EventBus.getDefault().post(new LoadRecognizedImageEvent(imageAddrString));
        }
    }

    /**
     * Loads recognized person image from sd card in background thread
     * @param loadRecognizedImageEvent {@link LoadRecognizedImageEvent}
     */
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onLoadRecognizedImageEvent(LoadRecognizedImageEvent loadRecognizedImageEvent){
        EventBus.getDefault()
                .post(new RecognizedObjectImageLoadedEvent(BitmapManager.getBitmap(loadRecognizedImageEvent.getImageAddr())));
    }

    /**
     * Sets recognized persons image to mIvRecognizedObjectImage and it's tag to mTvDetectedObjectTag
     * @param recognizedObjectImageLoadedEvent {@link RecognizedObjectImageLoadedEvent}
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecognizedObjectImageLoadedEvent(RecognizedObjectImageLoadedEvent recognizedObjectImageLoadedEvent){
        final Bitmap recognizedObjectBitmap = recognizedObjectImageLoadedEvent.getRecognizedObjectBitmap();
        if(recognizedObjectBitmap != null){
            mIvRecognizedObjectImage.setImageBitmap(recognizedObjectBitmap);
        }

        final String objectTagString = getIntent().getStringExtra(DETECTED_OBJECT_TAG);
        if(objectTagString != null){
            mTvDetectedObjectTag.setText(objectTagString);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            finish();
            return true;
        }
        return false;
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }
}
