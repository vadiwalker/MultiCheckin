package ru.ifmo.droid2016.korchagin.multicheckin;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.vk.sdk.util.VKUtil;

import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    enum Step{
        STEP_1,
        STEP_2
    }

    private Step currentStep;

    private View[] step1, step2;

    private ImageView imageView;
    private EditText commentText;

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
                Bundle extras = data.getExtras();
                Bitmap imageBitmap = (Bitmap) extras.get("data");
                if(imageBitmap != null){
                    gotoStep2(imageBitmap);
                }
                break;
            case REQUEST_PICTURE_FROM_FILE :
                Uri fileUri = data.getData();
                try{
                    imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), fileUri);
                    gotoStep2(imageBitmap);
                } catch (IOException e){
                    // silent
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        commentText = (EditText) findViewById(R.id.commentText);
        imageView = (ImageView)  findViewById(R.id.step2_image);

        step1 = new View[4];
        step1[0] = findViewById(R.id.step1_hint);
        step1[1] = findViewById(R.id.step1_textOr);
        step1[2] = findViewById(R.id.select_camera);
        step1[3] = findViewById(R.id.select_file);

        step2 = new View[3];
        step2[0] = imageView;
        step2[1] = findViewById(R.id.step2_undo);
        step2[2] = findViewById(R.id.fab);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        String[] fingerprints = VKUtil.getCertificateFingerprint(this, this.getPackageName());
        Log.e("CERT", fingerprints[0]);
        Toast t = Toast.makeText(getBaseContext(), fingerprints[0], Toast.LENGTH_LONG);
        t.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    public void selectPhotoFromFile(View v){
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        if (intent.resolveActivity(getPackageManager()) == null) {
            Toast errorToast = Toast.makeText(getBaseContext(), R.string.step1_nofile, Toast.LENGTH_SHORT);
            errorToast.show();
        }
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
        startActivityForResult(takePictureIntent, REQUEST_PICTURE_CAPTURE);
    }

    public void gotoStep2(@NonNull Bitmap image){
        imageView.setImageBitmap(image);
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
        imageView.setImageDrawable(null);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
