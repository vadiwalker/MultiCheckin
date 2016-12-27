package ru.ifmo.droid2016.korchagin.multicheckin;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import com.evernote.android.job.JobManager;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.vk.sdk.VKSdk;

import java.util.Map;
import java.util.TreeMap;

import ru.ifmo.droid2016.korchagin.multicheckin.integration.OkIntegration;
import ru.ifmo.droid2016.korchagin.multicheckin.integration.SocialIntegration;
import ru.ifmo.droid2016.korchagin.multicheckin.integration.TwitterIntegration;
import ru.ifmo.droid2016.korchagin.multicheckin.utils.IntegrationsUtil;
import ru.ok.android.sdk.Odnoklassniki;

public class MainApplication extends Application {
    public static Map<String, Integer> selectedSocialIntegrations;
    public static final String APP_PREFERENCES = "preferences";

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
        OkIntegration.getInstance();

        SharedPreferences mySharedPreferences = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);

        selectedSocialIntegrations = new TreeMap<>();

        for (SocialIntegration w : IntegrationsUtil.getAllIntegrations()) {
            int res = mySharedPreferences.getInt(w.getNetworkName(), 0);
            selectedSocialIntegrations.put(w.getNetworkName(), res);
        }
    }
}
