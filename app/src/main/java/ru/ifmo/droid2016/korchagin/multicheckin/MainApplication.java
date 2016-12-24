package ru.ifmo.droid2016.korchagin.multicheckin;

import android.app.Application;

import com.evernote.android.job.JobManager;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.vk.sdk.VKSdk;

import ru.ifmo.droid2016.korchagin.multicheckin.integration.TwitterIntegration;
import ru.ok.android.sdk.Odnoklassniki;

/**
 * Created by ME on 17.12.2016.
 */

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(getApplicationContext());

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
        JobManager.create(this).addJobCreator(new MyJobCreator());

        TwitterIntegration.getInstance();
        TwitterIntegration.getInstance().applicationInit(getApplicationContext());

        final String APP_ID = "1249212672";
        final String PUBLIC_KEY = "CBAKHGHLEBABABABA";
        Odnoklassniki.createInstance(getApplicationContext(), APP_ID, PUBLIC_KEY);
    }
}
