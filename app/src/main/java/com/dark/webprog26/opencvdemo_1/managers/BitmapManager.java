package com.dark.webprog26.opencvdemo_1.managers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import com.dark.webprog26.opencvdemo_1.models.FaceModel;
import com.dark.webprog26.opencvdemo_1.providers.DbProvider;

import org.opencv.android.Utils;
import org.opencv.core.CvException;
import org.opencv.core.Mat;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by webpr on 16.02.2017.
 */

public class BitmapManager {

    public static final String SAVED_IMAGES_TEMP_DIR = "/saved_images";
    private static final String EXTENSION = ".jpg";

    /**
     * Saves image of detected face to temporary directory
     * @param rgba {@link Mat}
     */
    public static void saveTemporaryBitmap(final Mat rgba) {
        Bitmap bitmap = null;
        try{
            bitmap = Bitmap.createBitmap(rgba.cols(), rgba.rows(), Bitmap.Config.ARGB_8888);
            Utils.matToBitmap(rgba, bitmap);
        } catch (CvException cve){
            cve.printStackTrace();
        }

        if(bitmap != null){
            String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
            File myDir = new File(root + SAVED_IMAGES_TEMP_DIR);
            myDir.mkdirs();
            Random generator = new Random();
            int n = 10000;
            n = generator.nextInt(n);
            String fname = "Image-" + n + EXTENSION;
            File file1 = new File(myDir, fname);
            if (file1.exists())
                file1.delete();
            try {
                FileOutputStream out = new FileOutputStream(file1);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Reads images with previosly detected faces from temporary directory
     * @return List<Bitmap>
     */
    public static List<Bitmap> readTemporaryBitmapsFromSD(){
        List<Bitmap> bitmaps = new ArrayList<>();
        String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        File myDir = new File(root + SAVED_IMAGES_TEMP_DIR);
        for(File fileBitmap: myDir.listFiles()){
            if(isFileJpg(fileBitmap)){
                bitmaps.add(getBitmap(fileBitmap.getAbsolutePath()));
            }
        }
        return bitmaps;
    }

    /**
     * Reads bitmap from given path
     * @param pathToBitmap {@link String}
     * @return
     */
    public static Bitmap getBitmap(String pathToBitmap){
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        return BitmapFactory.decodeFile(pathToBitmap, options);
    }

    /**
     * Prevents app from loading incorrect files by checking file extension with jpg
     * @param file {@link File}
     * @return boolean
     */
    private static boolean isFileJpg(File file){
        return file.getAbsolutePath().endsWith(EXTENSION);
    }

    /**
     * Saves photos selected by user via {@link ContentResolver} so they are visible in device PhotoGallery
     * @param contentResolver {@link ContentResolver}
     * @param folderName {@link String}
     * @param bitmapToSave {@link Bitmap}
     */
    public static void savePhotoToGallery(ContentResolver contentResolver, final String folderName, final Bitmap bitmapToSave) {
        final long currentTimeMillis = System.currentTimeMillis();
        final String galleryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        final String albumPath = galleryPath + "/" + folderName;
        final String photoName = currentTimeMillis + ".jpg";
        final String photoPath = albumPath + "/" +
                photoName;


        final ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DATA, photoPath);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
        values.put(MediaStore.Images.Media.TITLE, folderName);
        values.put(MediaStore.Images.Media.DESCRIPTION, folderName);
        values.put(MediaStore.Images.Media.DATE_TAKEN, currentTimeMillis);

        File album = new File(albumPath);
        if (!album.isDirectory() && !album.mkdirs()) {
            return;
        }

        File file1 = new File(albumPath, photoName);
        if (file1.exists())
            file1.delete();
        try {
            FileOutputStream out = new FileOutputStream(file1);
            bitmapToSave.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Uri uri;

        try {
            uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } catch (final Exception e) {


            File photo = new File(photoPath);
            photo.delete();
            return;
        }
    }


    public static void savePhotoToGallery(ContentResolver contentResolver, final String folderName,
                                          final HashMap<Bitmap, FaceModel> bitmapFaceModelHashMap,
                                          DbProvider dbProvider) {
        final long currentTimeMillis = System.currentTimeMillis();
        final String galleryPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
        final String albumPath = galleryPath + "/" + folderName;

        List<FaceModel> faceModels  = new ArrayList<>();

        for(Bitmap bitmap: bitmapFaceModelHashMap.keySet()){
            final String photoName = bitmapFaceModelHashMap.get(bitmap).getDescription() + "_" + System.currentTimeMillis() + ".jpg";
            final String photoPath = albumPath + "/" +
                    photoName;

            FaceModel.Builder builder = FaceModel.newBuilder();
            builder.setDescription(bitmapFaceModelHashMap.get(bitmap).getDescription());
            builder.setImageAddr(photoPath);

            faceModels.add(builder.build());

            final ContentValues values = new ContentValues();
            values.put(MediaStore.MediaColumns.DATA, photoPath);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
            values.put(MediaStore.Images.Media.TITLE, folderName);
            values.put(MediaStore.Images.Media.DESCRIPTION, folderName);
            values.put(MediaStore.Images.Media.DATE_TAKEN, currentTimeMillis);

            File album = new File(albumPath);
            if (!album.isDirectory() && !album.mkdirs()) {
                return;
            }

            File file1 = new File(albumPath, photoName);
            if (file1.exists())
                file1.delete();
            try {
                FileOutputStream out = new FileOutputStream(file1);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out);
                out.flush();
                out.close();
            }
            catch (Exception e) {
                e.printStackTrace();
            }

            Uri uri;

            try {
                uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            } catch (final Exception e) {


                File photo = new File(photoPath);
                photo.delete();
                return;
            }
            insertFaceModelsToDb(faceModels, dbProvider);
        }
    }

    private static void insertFaceModelsToDb(final List<FaceModel> faceModels, final DbProvider dbProvider){
        for(FaceModel faceModel: faceModels){
            dbProvider.insertFaceModel(faceModel);
        }
    }
}
