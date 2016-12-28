package ru.ifmo.droid2016.korchagin.multicheckin;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.squareup.picasso.Picasso;
import com.vk.sdk.util.VKUtil;

import java.io.File;
import java.io.IOException;

import ru.ifmo.droid2016.korchagin.multicheckin.integration.OkIntegration;
import ru.ifmo.droid2016.korchagin.multicheckin.integration.SendToAllJob;
import ru.ifmo.droid2016.korchagin.multicheckin.utils.BitmapFileUtil;

public class MainActivity extends AppCompatActivity  {

    enum Step{
        STEP_1,
        STEP_2
    }

    private Step currentStep;

    private View[] step1, step2;

    private ImageView imageView;
    private EditText commentText;

    private Uri imageTempFile;

    private static final String IMAGE_STORE_TAG = "ActualImage";

    private static final int REQUEST_PICTURE_CAPTURE = 1;
    private static final int REQUEST_PICTURE_FROM_FILE = 2;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode != RESULT_OK){
            return;
        }

        switch (requestCode) {
            case REQUEST_PICTURE_CAPTURE :
                Bitmap imageBitmap = null;
                try{
                    imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageTempFile);
                    if(imageBitmap != null){
                        gotoStep2(imageTempFile);
                    }
                } catch (IOException e){
                    // silent
                }
                break;
            case REQUEST_PICTURE_FROM_FILE :
                Uri image = data.getData();
                try{
                    imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), image);
                    if(imageBitmap != null) {
                        gotoStep2(image);
                    }
                } catch (IOException e){
                    // silent
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (currentStep == null) {
            super.onBackPressed();
            return;
        }

        switch (currentStep){
            case STEP_1:
                super.onBackPressed();
                break;
            case STEP_2:
                undoStep2(null);
                break;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(IMAGE_STORE_TAG, imageTempFile);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        commentText = (EditText) findViewById(R.id.commentText);
        imageView = (ImageView)  findViewById(R.id.step2_image);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String defaultComment = prefs.getString("step2_default_pref", null);

        if(defaultComment != null && defaultComment.length() > 0){
            commentText.setHint(defaultComment + "(Your default message)");
        }


        step1 = new View[4];
        step1[0] = findViewById(R.id.step1_hint);
        step1[1] = findViewById(R.id.step1_textOr);
        step1[2] = findViewById(R.id.select_camera);
        step1[3] = findViewById(R.id.select_file);

        step2 = new View[3];
        step2[0] = imageView;
        step2[1] = findViewById(R.id.step2_undo);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new SendBtnListener());
        step2[2] = fab;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(savedInstanceState != null){
            imageTempFile = savedInstanceState.getParcelable(IMAGE_STORE_TAG);
            if(imageTempFile != null){
                gotoStep2(imageTempFile);
            }
        }
    }

    class SendBtnListener implements View.OnClickListener{

        @Override
        public void onClick(View v) {
            Log.d("Sender", "Beginning send");
            PersistableBundleCompat extras = new PersistableBundleCompat();
            String pathToImage = BitmapFileUtil.writeToCacheAndGivePath(imageTempFile, getBaseContext());
            if(pathToImage == null){
                Log.e("Sender", "Unable to save image to cache");
                return;
            } else {
                extras.putString(SendToAllJob.PHOTO_TAG, pathToImage);
                imageTempFile = null;
            }
            String comment;
            if(commentText.getText() == null || commentText.getText().length() == 0){
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
                comment = prefs.getString("step2_default_pref", null);
            } else {
                comment = commentText.getText().toString();
            }
            extras.putString(SendToAllJob.MSG_TAG, comment);
            new JobRequest.Builder(SendToAllJob.TAG)
                    .setExecutionWindow(1L, 1000L)
                    .setExtras(extras)
                    .build().schedule();
            undoStep2(null);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getBaseContext());
        String defaultComment = prefs.getString("step2_default_pref", null);

        if(defaultComment != null && defaultComment.length() > 0){
            commentText.setHint(defaultComment + "(Your default message)");
        }
    }

    public void selectPhotoFromFile(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        if (intent.resolveActivity(getPackageManager()) == null) {
            Toast errorToast = Toast.makeText(getBaseContext(), R.string.step1_nofile, Toast.LENGTH_SHORT);
            errorToast.show();
        }
        Picasso.with(this).load(imageTempFile).into(imageView);
        startActivityForResult(intent, REQUEST_PICTURE_FROM_FILE);
    }

    public void selectPhotoFromCamera(View v){
        if(!getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA)){
            Toast errorToast = Toast.makeText(getBaseContext(), R.string.step1_nocamera, Toast.LENGTH_SHORT);
            errorToast.show();
            return;
        }
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) == null) {
            Toast errorToast = Toast.makeText(getBaseContext(), R.string.step1_nocamera, Toast.LENGTH_SHORT);
            errorToast.show();
            return;
        }
        File tempFile = BitmapFileUtil.getTempImageFile(getApplicationContext());

        if (tempFile != null) {
            imageTempFile = FileProvider.getUriForFile(getApplicationContext(),
                    "ru.ifmo.droid2016.korchagin.multicheckin",
                    tempFile);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageTempFile);
            startActivityForResult(takePictureIntent, REQUEST_PICTURE_CAPTURE);
        } else {
            Toast errorToast = Toast.makeText(getBaseContext(), R.string.step1_notempfile, Toast.LENGTH_SHORT);
            errorToast.show();
        }

    }

    public void gotoStep2(@NonNull Uri image){
        this.imageTempFile = image;
        Picasso.with(this).load(image).into(imageView);
        for (View view: step1) {
            view.setVisibility(View.INVISIBLE);
        }
        for (View view: step2) {
            view.setVisibility(View.VISIBLE);
        }
        currentStep = Step.STEP_2;
    }

    public void undoStep2(View v){
        currentStep = Step.STEP_1;
        for (View view: step1) {
            view.setVisibility(View.VISIBLE);
        }
        for (View view: step2) {
            view.setVisibility(View.INVISIBLE);
        }
        imageTempFile = null;
        imageView.setImageDrawable(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Intent intent;
        switch (id){
            case R.id.services_settings :
                intent = new Intent();
                intent.setClassName(this, "ru.ifmo.droid2016.korchagin.multicheckin.integration.IntegrationActivity");
                startActivity(intent);
                return true;
            case R.id.action_settings :
                intent = new Intent();
                intent.setClassName(this, ServicesActivity.class.getCanonicalName());
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void clickOk(View view) {
        OkIntegration.getInstance().sendPhotosForJob(null, "", 0);
    }


}
