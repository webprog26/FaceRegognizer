package com.dark.webprog26.opencvdemo_1;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ProgressBar;
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
import butterknife.BindView;
import butterknife.ButterKnife;
import static com.dark.webprog26.opencvdemo_1.managers.BitmapManager.SAVED_IMAGES_TEMP_DIR;


public class PhotoLabActivity extends AppCompatActivity {

    private static final String TAG = "PhotoLabActivity_TAG";

    private static final String IS_WARNING_DIALOG_ON = "is_warning_dialog_on";

    private List<Bitmap> mCorrectFaces = new ArrayList<>();
    private FaceModel.Builder mBuilder = FaceModel.newBuilder();
    private HashMap<Bitmap, FaceModel> mBitmapFaceModelHashMap = new HashMap<>();
    private DbProvider mDbProvider;
    private SharedPreferences mSharedPreferences;

    @BindView(R.id.gridView)
    GridView mGridView;
    @BindView(R.id.pbPhotoSaving)
    ProgressBar mPbPhotoSaving;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_lab);
        ButterKnife.bind(this);
        mDbProvider = new DbProvider(this);
        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        if(!mSharedPreferences.getBoolean(IS_WARNING_DIALOG_ON, false)){
            //if warning dialog wasn't cancelled, then show it to the user
            showMessageWithWarning();
        }
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
     * @param temporaryBitmapsRequestEvent {@link TemporaryBitmapsRequestEvent}
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
                                    invalidateOptionsMenu();
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
                invalidateOptionsMenu();
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
                if(mPbPhotoSaving.getVisibility() == View.GONE){
                    mPbPhotoSaving.setVisibility(View.VISIBLE);
                }
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
        EventBus.getDefault().post(new DeleteUnselectedPhotosEvent());
    }


    /**
     * Delete selected photos from temporary folder to save device physical memory space
     * @param deleteUnselectedPhotosEvent {@link DeleteUnselectedPhotosEvent}
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
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(mPbPhotoSaving.getVisibility() == View.VISIBLE){
                    mPbPhotoSaving.setVisibility(View.GONE);
                }
            }
        });
        finish();
     }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.getItem(0).setEnabled(mBitmapFaceModelHashMap.size() > 0);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK){
              if(mBitmapFaceModelHashMap.size() == 0){
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

    /**
     * Shows dialog with warning to the user that at least 5 photos needed for better recognizing performance
     */
    private void showMessageWithWarning(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final View dialogView = getLayoutInflater().inflate(R.layout.photos_num_warning_dialog, null);
        builder.setTitle(getString(R.string.warning))
                .setView(dialogView)
                .setCancelable(true)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        CheckBox chbDontShow = (CheckBox) dialogView.findViewById(R.id.chbDontShow);
        chbDontShow.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                SharedPreferences.Editor editor = mSharedPreferences.edit();
                editor.putBoolean(IS_WARNING_DIALOG_ON, isChecked).apply();
            }
        });
        builder.show();
    }
}
