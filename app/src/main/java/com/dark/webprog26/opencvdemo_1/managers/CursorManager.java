package com.dark.webprog26.opencvdemo_1.managers;

import android.database.Cursor;

import com.dark.webprog26.opencvdemo_1.db.DbHelper;
import com.dark.webprog26.opencvdemo_1.models.FaceModel;

/**
 * Created by webpr on 21.02.2017.
 */

public class CursorManager {

    /**
     * Gets {@link FaceModel} instance via predefined {@link Cursor}
     * @param cursor {@link Cursor}
     * @return {@link FaceModel}
     */
    public static FaceModel getFaceModel(Cursor cursor){
        if(cursor == null){
            return null;
        }

        FaceModel.Builder builder = FaceModel.newBuilder();
        builder.setId(cursor.getLong(cursor.getColumnIndex(DbHelper.ID)));
        builder.setDescription(cursor.getString(cursor.getColumnIndex(DbHelper.FACE_MODEL_DESCRIPTION)));
        builder.setImageAddr(cursor.getString(cursor.getColumnIndex(DbHelper.FACE_MODEL_IMAGE_ADDR)));
        return builder.build();
    }
}
