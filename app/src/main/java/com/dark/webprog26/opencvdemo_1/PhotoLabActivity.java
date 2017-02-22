package com.dark.webprog26.opencvdemo_1;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import com.dark.webprog26.opencvdemo_1.adapters.LabGridViewAdapter;
import com.dark.webprog26.opencvdemo_1.events.DeleteUnselectedPhotosEvent;
import com.dark.webprog26.opencvdemo_1.events.SelectedPhotosSaveEvent;
import com.dark.webprog26.opencvdemo_1.events.TemporaryBitmapsLoadedEvent;
import com.dark.webprog26.opencvdemo_1.events.TemporaryBitmapsRequestEvent;
import com.dark.webprog26.opencvdemo_1.managers.BitmapManager;
import com.dark.webprog26.opencvdemo_1.models.FaceModel;
import com.dark.webprog26.opencvdemo_1.providers.DbProvider;
import com.dark.webprog26.opencvdemo_1.views.FaceView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static com.dark.webprog26.opencvdemo_1.managers.BitmapManager.SAVED_IMAGES_TEMP_DIR;

public class PhotoLabActivity extends AppCompatActivity {

    private static final String TAG = "PhotoLabActivity_TAG";
    private GridView mGridView;
    private List<Bitmap> mCorrectFaces = new ArrayList<>();
    private FaceModel.Builder mBuilder = FaceModel.newBuilder();
    private HashMap<Bitmap, FaceModel> mBitmapFaceModelHashMap = new HashMap<>();
    private DbProvider mDbProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_lab);
        mGridView = (GridView) findViewById(R.id.gridView);
        mDbProvider = new DbProvider(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        EventBus.getDefault().post(new TemporaryBitmapsRequestEvent());
    }

    @Override
    protected void onStop() {
        EventBus.getDefault().unregister(this);
        super.onStop();
    }


    /**
     * Makes request to uploaded Bitmaps with detected faces from the temporary folder
     * @param temporaryBitmapsRequestEvent
     */
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onTemporaryBitmapsRequestEvent(TemporaryBitmapsRequestEvent temporaryBitmapsRequestEvent){
        EventBus.getDefault().post(new TemporaryBitmapsLoadedEvent(BitmapManager.readTemporaryBitmapsFromSD()));
    }

    /**
     * Makes uploaded bitmaps visible to the user via {@link GridView} and inits {@link LabGridViewAdapter}
     * @param temporaryBitmapsLoadedEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onTemporaryBitmapsLoadedEvent(TemporaryBitmapsLoadedEvent temporaryBitmapsLoadedEvent){
        List<Bitmap> facesBitmaps = temporaryBitmapsLoadedEvent.getBitmaps();
        LabGridViewAdapter labGridViewAdapter = new LabGridViewAdapter(this, R.layout.lab_image_item, facesBitmaps);
        mGridView.setOnItemClickListener(new FacesClickListener(facesBitmaps));
        mGridView.setAdapter(labGridViewAdapter);
    }

    /**
     * Checks is given Bitmap alreaady presented in the list
     * @param faceBitmap {@link Bitmap}
     * @return
     */
    private boolean isFaceChecked(Bitmap faceBitmap){
        return mCorrectFaces.contains(faceBitmap);
    }

    /**
     * Handles clicks on {@link GridView} items
     */
    private class FacesClickListener implements GridView.OnItemClickListener  {
        private List<Bitmap> facesList;

        public FacesClickListener(List<Bitmap> facesList) {
            this.facesList = facesList;
        }

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            final Bitmap faceBitmap = facesList.get(position);
            if(!isFaceChecked(faceBitmap)){
                ((FaceView) view).drawMarker(faceBitmap, false);
                mCorrectFaces.add(faceBitmap);
                final View dialogView = getLayoutInflater().inflate(R.layout.face_data_layout, null);
                AlertDialog.Builder builder = new AlertDialog.Builder(PhotoLabActivity.this)
                        .setTitle(getResources().getString(R.string.person_description_tag))
                        .setView(dialogView)
                        .setCancelable(false)
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EditText etDescription = (EditText) dialogView.findViewById(R.id.etDescription);
                                mBuilder.setDescription(etDescription.getText().toString());
                                Log.i(TAG, etDescription.getText().toString());
                                FaceModel faceModel = mBuilder.build();
                                if(faceModel.getDescription() != null){
                                    Log.i(TAG, "faceModel.getDescription() " + faceModel.getDescription());
                                    mBitmapFaceModelHashMap.put(faceBitmap, faceModel);
                                }
                                Log.i(TAG, "map: " + mBitmapFaceModelHashMap.toString() + ", size: " + mBitmapFaceModelHashMap.size());
                                dialog.dismiss();
                            }
                        }).setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.show();
            } else {
                ((FaceView) view).drawMarker(faceBitmap, true);
                mCorrectFaces.remove(faceBitmap);
                mBitmapFaceModelHashMap.remove(faceBitmap);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.lab_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.actionSave:
                EventBus.getDefault().post(new SelectedPhotosSaveEvent(mCorrectFaces));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Saves selected photos to constant folder
     * @param selectedPhotosSaveEvent {@link SelectedPhotosSaveEvent}
     */
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onSelectedPhotosSaveEvent(SelectedPhotosSaveEvent selectedPhotosSaveEvent){
        List<Bitmap> bitmaps = selectedPhotosSaveEvent.getBitmaps();
        if(bitmaps.size() < 1){
            finish();
            EventBus.getDefault().post(new DeleteUnselectedPhotosEvent());
            return;
        }

        if(mBitmapFaceModelHashMap.size() > 0){
            BitmapManager.savePhotoToGallery(getContentResolver(), getResources().getString(R.string.app_name), mBitmapFaceModelHashMap, mDbProvider);
        }
//        for(Bitmap selectedBitmap: bitmaps){
//            BitmapManager.savePhotoToGallery(getContentResolver(), getResources().getString(R.string.app_name), selectedBitmap);
//        }
        EventBus.getDefault().post(new DeleteUnselectedPhotosEvent());
    }


    /**
     * Delete selected photos from temporary folder to save device physical memory space
     * @param deleteUnselectedPhotosEvent
     */
    @Subscribe(threadMode = ThreadMode.ASYNC)
    public void onDeleteUnselectedPhotosEvent(DeleteUnselectedPhotosEvent deleteUnselectedPhotosEvent){
         String root = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString();
         File myDir = new File(root + SAVED_IMAGES_TEMP_DIR);

         if(!myDir.isDirectory()){
             finish();
             return;
         } else if(myDir.listFiles().length < 1){
             finish();
             return;
         } else {
             for(File fileToDelete: myDir.listFiles()){
                if(!fileToDelete.delete()){
                    break;
                }
             }
         }
         finish();
     }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            if(mCorrectFaces.size() == 0){
                AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(getResources().getString(R.string.warning))
                .setMessage(getResources().getString(R.string.no_photos_selected))
                        .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                EventBus.getDefault().post(new DeleteUnselectedPhotosEvent());
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                builder.show();
                return true;
            }
        }
        return false;
    }
}
