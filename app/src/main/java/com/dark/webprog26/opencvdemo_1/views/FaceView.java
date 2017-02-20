package com.dark.webprog26.opencvdemo_1.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.ImageView;

import com.dark.webprog26.opencvdemo_1.R;

/**
 * Created by webpr on 16.02.2017.
 */

public class FaceView extends ImageView {

    public FaceView(Context context) {
        super(context);
    }

    public FaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void drawMarker(Bitmap imageBitmap, boolean isChecked) {
        if (!isChecked) {
            Bitmap drawableBitmap = imageBitmap.copy(Bitmap.Config.ARGB_8888, true);
            Canvas canvas = new Canvas(drawableBitmap);
            Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
            p.setStyle(Paint.Style.STROKE);
            p.setColor(Color.BLUE);
            p.setStrokeWidth(1);
            canvas.drawRect(1, 1, drawableBitmap.getWidth() - 1, drawableBitmap.getHeight() - 1, p);
            setAlpha(0.5f);
//
//            Bitmap markerBitmap = Bitmap.createScaledBitmap(BitmapFactory.decodeResource(getResources(), R.drawable.marker), getWidth() / 8, getHeight() / 8, true);
//            canvas.drawBitmap(markerBitmap, getWidth() - ((getWidth() / 8) * 2), getHeight() - ((getHeight() / 8) * 2), p);

            setImageBitmap(drawableBitmap);
        } else {
            setAlpha(1.0f);
            setImageBitmap(imageBitmap);
        }
    }


}
