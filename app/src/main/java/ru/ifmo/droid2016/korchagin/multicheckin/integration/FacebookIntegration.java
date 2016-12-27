package ru.ifmo.droid2016.korchagin.multicheckin.integration;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobManager;
import com.evernote.android.job.util.support.PersistableBundleCompat;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;

import ru.ifmo.droid2016.korchagin.multicheckin.R;
import ru.ifmo.droid2016.korchagin.multicheckin.utils.BitmapFileUtil;

public class FacebookIntegration implements SocialIntegration {
    private static final String LOG_TAG = "facebook_integration";

    private WeakReference<Activity> weakActivity;

    private FacebookIntegration() {
        weakActivity = new WeakReference<>(null);
    }

    private static FacebookIntegration mInstance;

    public static FacebookIntegration getInstance() {
        if (mInstance != null) {
            return mInstance;
        } else {
            mInstance = new FacebookIntegration();
            return mInstance;
        }
    }

    private boolean sendPhotosForJob(@NonNull Bitmap photo, @Nullable final String comment, final int newJobId) {
        if (AccessToken.getCurrentAccessToken() == null) {
            Log.d(LOG_TAG, "in testRequest_3 no AccessToken");
            return false;
        }
        File file = BitmapFileUtil.getFileFromPath(BitmapFileUtil.writeToCacheAndGivePath(photo));

        GraphRequest request;

        try {
            request = GraphRequest.newUploadPhotoRequest(
                    AccessToken.getCurrentAccessToken(),
                    "/me/photos",
                    file,
                    comment,
                    null,
                    new GraphRequest.Callback() {
                        @Override
                        public void onCompleted(GraphResponse graphResponse) {
                            Log.d(LOG_TAG, "Ответ : " + graphResponse.toString());
                            JobManager.instance().cancel(newJobId);
                        }
                    }
            );

        } catch (Exception ex) {
            Log.d(LOG_TAG, ex.toString());
            return false;
        }

        Log.d(LOG_TAG, " Запрос: " + request.toString());

        request.executeAsync();

        return true;
    }

    @Override
    public String getSandJobTag() {
        return FacebookSendJob.TAG;
    }

    @Override
    public Job getJob() {
        return new FacebookSendJob();
    }

    private static class FacebookSendJob extends Job {
        static final String TAG = "SendPhotoToFacebookJob";

        @NonNull
        @Override
        protected Result onRunJob(Params params) {
            return Result.RESCHEDULE;
        }

        @Override
        protected void onReschedule(int newJobId) {
            PersistableBundleCompat extras = getParams().getExtras();
            String photoPath = extras.getString(SendToAllJob.PHOTO_TAG, null);
            Bitmap photo = BitmapFileUtil.getFromPath(photoPath);
            String message = extras.getString(SendToAllJob.MSG_TAG, null);
            boolean res = FacebookIntegration.getInstance().sendPhotosForJob(photo, message, newJobId);
            super.onReschedule(newJobId);
        }
    }

    CallbackManager init(IntegrationActivity newActivity) {
        weakActivity = new WeakReference<Activity>(newActivity);

        CallbackManager facebookCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(facebookCallbackManager,
                new FacebookCallback<LoginResult>() {

                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(LOG_TAG, "Залогинились успешно.");

                        AccessToken.setCurrentAccessToken(loginResult.getAccessToken());
                        sendBroadcast();
                    }

                    @Override
                    public void onCancel() {
                        Log.d(LOG_TAG, "Отмена входа.");
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d(LOG_TAG, "Ошибка : " + error.getMessage());
                    }

                    void sendBroadcast() {
                        Activity activity = weakActivity.get();

                        if (activity != null) {
                            activity.sendBroadcast(
                                    new Intent(
                                            IntegrationActivity.NEW_NETWORK_IS_LOGGED)
                                            .putExtra(IntegrationActivity.NETWORK_NAME, getNetworkName())
                            );
                        }
                    }
                }
        );
        return facebookCallbackManager;
    }

    private static List permissions = Arrays.asList("public_profile", "user_friends");

    @Override
    public void login() {
        Activity activity = weakActivity.get();
        if (activity == null) {
            Log.d(LOG_TAG, "Кто-то набажил : (activity == null) ");
            return;
        }
        Log.d(LOG_TAG, "in_login");
        LoginManager.getInstance().logInWithReadPermissions(activity, permissions);
    }

    @Override
    public void logout() {
        AccessToken.setCurrentAccessToken(null);
        Log.d(LOG_TAG, "logout");
    }

    @Override
    public Drawable getIcon() {
        Activity activity = weakActivity.get();
        if(activity == null) {
            return null;
        } else {
            return activity.getResources().getDrawable(R.mipmap.ic_facebook);
        }
    }

    @Override
    @NonNull
    public String getNetworkNameLocalized() {
        Activity activity = weakActivity.get();
        if (activity != null) {
            return activity.getString(R.string.fb_name);
        } else {
            return "Facebook";
        }
    }

    @Override
    public boolean getStatus() {
        Log.d(LOG_TAG, String.valueOf((AccessToken.getCurrentAccessToken() != null)));
        return (AccessToken.getCurrentAccessToken() != null);
    }

    @Override
    @NonNull public String getNetworkName() {
        return "Facebook";
    }

}
