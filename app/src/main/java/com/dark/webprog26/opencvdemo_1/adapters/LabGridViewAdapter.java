package com.dark.webprog26.opencvdemo_1.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.dark.webprog26.opencvdemo_1.R;
import com.dark.webprog26.opencvdemo_1.views.FaceView;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * Created by webpr on 16.02.2017.
 */

public class LabGridViewAdapter extends ArrayAdapter {

    //private fields to initialize input data
    private WeakReference<Context> mContextWeakReference;
    private int mLayoutResId;
    private List<Bitmap> mData;

    public LabGridViewAdapter(Context context, int layoutResId, List<Bitmap> data) {
        super(context, layoutResId, data);
        this.mContextWeakReference = new WeakReference<Context>(context);
        this.mLayoutResId = layoutResId;
        this.mData = data;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;
        ViewHolder viewHolder = null;

        if(view == null){
            LayoutInflater layoutInflater = ((Activity)mContextWeakReference.get()).getLayoutInflater();
            view = layoutInflater.inflate(mLayoutResId, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.mImage = (FaceView) view.findViewById(R.id.image);
            view.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) view.getTag();
        }
        Bitmap bitmap = mData.get(position);
        viewHolder.mImage.setImageBitmap(bitmap);
        return view;
    }

    static class ViewHolder{
        FaceView mImage;
    }
}
