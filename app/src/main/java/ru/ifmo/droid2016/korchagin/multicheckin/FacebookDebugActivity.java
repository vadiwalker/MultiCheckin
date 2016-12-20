package ru.ifmo.droid2016.korchagin.multicheckin;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;

import ru.ifmo.droid2016.korchagin.multicheckin.integration.FacebookIntegration;

/**
 * Created by Vlad_kv on 20.12.2016.
 */

public class FacebookDebugActivity extends AppCompatActivity {

    EditText editText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_facebook_debug);

        editText = (EditText) findViewById(R.id.editText);
    }

    public void onClickSample(View view) {
        FacebookIntegration.getInstance().testRequest();
    }

    public void onClickPublishString(View view) {
        FacebookIntegration.getInstance().testRequest_2(editText.getText().toString());
    }
}
