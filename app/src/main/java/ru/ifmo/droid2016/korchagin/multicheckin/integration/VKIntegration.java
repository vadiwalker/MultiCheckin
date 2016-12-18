package ru.ifmo.droid2016.korchagin.multicheckin.integration;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

import com.vk.sdk.VKSdk;
import com.vk.sdk.api.model.VKScopes;

import java.lang.ref.WeakReference;

import ru.ifmo.droid2016.korchagin.multicheckin.R;

/**
 * Created by ME on 17.12.2016.
 */

public class VKIntegration implements SocialIntegration{

    private final String TAG = "VK_Int";

    private WeakReference<IntegrationActivity> weakActivity;

    @Override
    public void login() {
        Activity activity = weakActivity.get();
        if(activity != null){
            VKSdk.login(activity, VKScopes.WALL, VKScopes.PHOTOS);
        }
    }

    @Override
    public void logout() {
        VKSdk.logout();
    }

    @Override
    public Drawable getIcon() {
        Activity activity = weakActivity.get();
        if(activity == null)
            return null;
        return activity.getResources().getDrawable(R.mipmap.ic_vk);
        // Deprecated since API 22, but we target API 16+
    }

    @Override
    @NonNull
    public String getName() {
        return "VK";
    }

    @Override
    public boolean getStatus() {
        return VKSdk.isLoggedIn();
    }
}
