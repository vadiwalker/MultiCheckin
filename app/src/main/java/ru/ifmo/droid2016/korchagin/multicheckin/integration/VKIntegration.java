package ru.ifmo.droid2016.korchagin.multicheckin.integration;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.model.VKApiPhoto;
import com.vk.sdk.api.model.VKAttachments;
import com.vk.sdk.api.model.VKPhotoArray;
import com.vk.sdk.api.model.VKScopes;
import com.vk.sdk.api.model.VKWallPostResult;
import com.vk.sdk.api.photo.VKImageParameters;
import com.vk.sdk.api.photo.VKUploadImage;

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

    private int getMyVKId() {
        final VKAccessToken vkAccessToken = VKAccessToken.currentToken();
        return vkAccessToken != null ? Integer.parseInt(vkAccessToken.userId) : 0;
    }

    /**
     * Отправляет пару из фоточки и коммента на стенку через AsyncTask
     * @param photo Bitmap фотки
     * @param comment Комментарий
     */
    public boolean sendPhotos(@NonNull Bitmap photo, @Nullable final String comment){
        VKRequest request = VKApi.uploadWallPhotoRequest(new VKUploadImage(photo,
            VKImageParameters.jpgImage(0.9f)), getMyVKId(), 0);
        request.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                VKApiPhoto photoModel = ((VKPhotoArray) response.parsedModel).get(0);
                makePost(new VKAttachments(photoModel), comment, getMyVKId());
            }
            @Override
            public void onError(VKError error) {
            }
        });
        return false;
    }

    void makePost(VKAttachments att, String msg, final int ownerId) {
        VKParameters parameters = new VKParameters();
        parameters.put(VKApiConst.OWNER_ID, String.valueOf(ownerId));
        parameters.put(VKApiConst.ATTACHMENTS, att);
        parameters.put(VKApiConst.MESSAGE, msg);
        VKRequest post = VKApi.wall().post(parameters);
        post.setModelClass(VKWallPostResult.class);
        post.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                // post was added
            }
            @Override
            public void onError(VKError error) {
                // error
            }
        });
    }
}
