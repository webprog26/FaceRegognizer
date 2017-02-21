package com.dark.webprog26.opencvdemo_1.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by webpr on 21.02.2017.
 */

public class DbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "face_models_db";
    private static final int DB_VERSION = 1;

    public static final String FACE_MODELS_TABLE = "face_models_table";
    public static final String ID = "_id";
    public static final String FACE_MODEL_DESCRIPTION = "face_model_description";
    public static final String FACE_MODEL_IMAGE_ADDR = "face_model_image_addr";

    public DbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + FACE_MODELS_TABLE + "("
        + ID + " integer primary key autoincrement, "
        + FACE_MODEL_DESCRIPTION + " varchar(200), "
        + FACE_MODEL_IMAGE_ADDR  + " varchar(200))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //
    }
}
