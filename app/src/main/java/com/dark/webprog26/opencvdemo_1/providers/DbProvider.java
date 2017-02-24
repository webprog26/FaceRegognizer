package com.dark.webprog26.opencvdemo_1.providers;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;

import com.dark.webprog26.opencvdemo_1.managers.CursorManager;
import com.dark.webprog26.opencvdemo_1.models.FaceModel;
import com.dark.webprog26.opencvdemo_1.db.DbHelper;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Created by webpr on 21.02.2017.
 */

public class DbProvider {

    private final WeakReference<Context> mContextWeakReference;
    private final DbHelper mDbHelper;

    public DbProvider(Context context) {
        this.mContextWeakReference = new WeakReference<Context>(context);
        this.mDbHelper = new DbHelper(mContextWeakReference.get());
    }

    /**
     * Inserts {@link FaceModel} instance to db
     * @param faceModel {@link FaceModel}
     */
    public void insertFaceModel(FaceModel faceModel){
        ContentValues contentValues = new ContentValues();
        contentValues.put(DbHelper.FACE_MODEL_DESCRIPTION, faceModel.getDescription());
        contentValues.put(DbHelper.FACE_MODEL_IMAGE_ADDR, faceModel.getImageAddr());
        mDbHelper.getWritableDatabase().insert(DbHelper.FACE_MODELS_TABLE, null, contentValues);
    }

    /**
     * Gets {@link java.util.List} of {@link FaceModel} instances
     * @return ArrayList
     */
    public ArrayList<FaceModel> getFaceModels(){
        ArrayList<FaceModel> faceModels = new ArrayList<>();

        Cursor cursor = mDbHelper.getReadableDatabase().query(DbHelper.FACE_MODELS_TABLE,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                null,
                                                                DbHelper.ID);
        while(cursor.moveToNext()){
            faceModels.add(CursorManager.getFaceModel(cursor));
        }
        cursor.close();
        return faceModels;
    }
}
