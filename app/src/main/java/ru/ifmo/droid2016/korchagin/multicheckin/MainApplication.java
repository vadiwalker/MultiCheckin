package ru.ifmo.droid2016.korchagin.multicheckin;

import android.app.Application;

import com.vk.sdk.VKSdk;

/**
 * Created by ME on 17.12.2016.
 */

public class MainApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(getApplicationContext());
    }
}
