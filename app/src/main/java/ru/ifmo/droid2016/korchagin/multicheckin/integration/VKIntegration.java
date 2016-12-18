package ru.ifmo.droid2016.korchagin.multicheckin.integration;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.model.VKScopes;

import java.lang.ref.WeakReference;

import ru.ifmo.droid2016.korchagin.multicheckin.R;

/**
 * Created by ME on 17.12.2016.
 */

public class VKIntegration implements SocialIntegration{

    private VKIntegration() {}

    private static VKIntegration mInstance;

    public static VKIntegration getInstance(){
        if(mInstance != null){
            return mInstance;
        } else {
            mInstance = new VKIntegration();
            return mInstance;
        }
    }

    public void updateActivityReference(Activity activity){
        weakActivity = new WeakReference<>(activity);
    }

    private final String TAG = "VK_Int";

    private WeakReference<Activity> weakActivity;

    @Override
    public void login() {
        Activity activity = weakActivity.get();
        if(activity != null){
            if(VKSdk.isLoggedIn()){
                Log.d(TAG, "Already logged in");
                Intent successIntent = new Intent(IntegrationActivity.NEW_NETWORK_IS_LOGGED);
                successIntent.putExtra(IntegrationActivity.NETWORK_NAME, getName());
                activity.sendBroadcast(successIntent);
            }
            Log.d(TAG, "Starting login");
            VKSdk.login(activity, VKScopes.WALL, VKScopes.PHOTOS);
        } else {
            Log.d(TAG, "No activity found for log in");
        }
    }

    public void tryLoginFinish(int requestCode, int resultCode, Intent data){

        VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                Log.d(TAG, "VK login successful");
                Activity activity = weakActivity.get();
                if(activity != null) {
                    Intent successIntent = new Intent(IntegrationActivity.NEW_NETWORK_IS_LOGGED);
                    successIntent.putExtra(IntegrationActivity.NETWORK_NAME, getName());
                    activity.sendBroadcast(successIntent);
                }

            }
            @Override
            public void onError(VKError error) {
                Log.d(TAG, "VK login failed");
            }
        });
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
        // Deprecated since API 22, but substitute appeared at API 21 and we target API 16+
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
