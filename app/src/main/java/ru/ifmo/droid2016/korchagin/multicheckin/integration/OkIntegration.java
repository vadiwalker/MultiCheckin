package ru.ifmo.droid2016.korchagin.multicheckin.integration;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.util.support.PersistableBundleCompat;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;

import ru.ifmo.droid2016.korchagin.multicheckin.R;
import ru.ifmo.droid2016.korchagin.multicheckin.utils.BitmapFileUtil;
import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkListener;
import ru.ok.android.sdk.OkPostingActivity;
import ru.ok.android.sdk.Shared;
import ru.ok.android.sdk.util.OkAuthType;
import ru.ok.android.sdk.util.OkEncryptUtil;
import ru.ok.android.sdk.util.OkRequestUtil;
import ru.ok.android.sdk.util.OkScope;

/**
 * Created by vadim on 24.12.16.
 */

public class OkIntegration implements SocialIntegration {

    private static final String APP_ID = "1249212672";
    private static final String PUBLIC_KEY = "CBAKHGHLEBABABABA";
    private static final String SECRET_KEY = "EFC90F68D96FB26BDD678927";
    private static final String REDIRECT_URI = "okauth://ok1249212672";
    private String access_token = null;

    private static OkIntegration instance = null;
    private boolean validTokens = false;
    WeakReference<IntegrationActivity> weakIntegrationActivity = new WeakReference<>(null);
    WeakReference<Activity> weakMainActivity = new WeakReference<>(null);

    private OkIntegration() {
        Odnoklassniki.getInstance().checkValidTokens(getValidationListener());
    }

    public static OkIntegration getInstance() {
        if (instance == null) {
            instance = new OkIntegration();
        }
        return instance;
    }

    private static class OkSendJob extends Job {
        public static final String TAG = "SendPhotoToOKJob";
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
            OkIntegration.getInstance().sendPhotosForJob(photo, message, newJobId);
            super.onReschedule(newJobId);
        }
    }

    public void sendPhotosForJob(Bitmap photo, String message, int newJobId) {
        Log.d(TAG, "send photo");

//        Odnoklassniki.getInstance().performPosting(weakMainActivity.get(), attachment.toString(), true, null);
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {

                    JSONObject attachment = new JSONObject();
                    JSONObject text = new JSONObject();
                    String attack = "{\n" +
                            "    \"media\": [\n" +
                            "        {\n" +
                            "            \"type\": \"text\",\n" +
                            "            \"text\": \"This is a text of a new topic\"\n" +
                            "        }\n" +
                            "    ]\n" +
                            "}";
                    text.put("type", "text");
                    text.put("text", "Hello");

                    attachment.put("media", text.toString());

                    Log.d(TAG, "attachemnt: " + attachment);



                    HashMap<String, String> map = new HashMap<>();
                    map.put("st.type", "user");
                    map.put("st.app", APP_ID);
                    map.put("st.attachment", OkEncryptUtil.toMD5(attack + SECRET_KEY));
                    map.put("st.access_token", access_token);

                    String callback = OkRequestUtil.executeRequest(map);
                    Log.d(TAG, "answer on request: " + callback);

                } catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public String getSandJobTag() {
        return OkSendJob.TAG;
    }

    @Override
    public Job getJob() {
        return new OkSendJob();
    }

    @Override
    public void login() {
        Log.d(TAG, "login");
        IntegrationActivity activity = weakIntegrationActivity.get();
        if (activity == null) {
            Log.d(TAG, "activity null");
        }
        Odnoklassniki.getInstance().requestAuthorization(activity, REDIRECT_URI, OkAuthType.ANY, OkScope.VALUABLE_ACCESS);
    }

    @Override
    public void logout() {
        Log.d(TAG, "logout");
        Odnoklassniki.getInstance().clearTokens();
        access_token = null;
        validTokens = false;
    }

    @Nullable
    @Override
    public Drawable getIcon() {
        IntegrationActivity activity = weakIntegrationActivity.get();
        if (activity == null) {
            return null;
        } else {
            return activity.getResources().getDrawable(R.mipmap.ic_ok);
        }
    }

    @NonNull
    @Override
    public String getNetworkNameLocalized() {
        return getNetworkName();
    }

    @NonNull
    @Override
    public String getNetworkName() {
        return "Odnoklassniki";
    }

    @Override
    public boolean getStatus() {
        Log.d(TAG, "getStatus");
        return validTokens;
    }

    void sendBroadcast() {
        Activity activity = weakIntegrationActivity.get();

        if (activity != null) {
            activity.sendBroadcast(
                    new Intent(
                            IntegrationActivity.NEW_NETWORK_IS_LOGGED)
                            .putExtra(IntegrationActivity.NETWORK_NAME, getNetworkName())
            );
        }
    }

    public void tryProcessRequest(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "tryLoginFinish");
        if (Odnoklassniki.getInstance().onAuthActivityResult(requestCode, resultCode, data, getAuthListener())) {
            Log.d(TAG, "Authentication callback");
        } else if (Odnoklassniki.getInstance().onActivityResultResult(requestCode, resultCode, data, new OkListener() {
            @Override
            public void onSuccess(JSONObject json) {
                Log.d(TAG, "succes posting: " + json);
            }
            @Override
            public void onError(String error) {
                Log.d(TAG, "posting failed " + error);
            }
        })) {
            Log.d(TAG, "Result result");
        }
    }

    private OkListener getAuthListener() {

        return new OkListener() {
            @Override
            public void onSuccess(JSONObject json) {
                try {
                    Log.d(TAG, String.format("acces_token is %s", json.getString("access_token")));
                    access_token = json.getString("access_token");
                    sendBroadcast();
                } catch(JSONException e) {
                    e.printStackTrace();
                }
                validTokens = true;
            }

            @Override
            public void onError(String error) {
                Log.d(TAG, "error in getAuthListener");
            }
        };
    }

    private OkListener getValidationListener() {
        return new OkListener() {
            @Override
            public void onSuccess(JSONObject json) {
                Log.d(TAG, "Validation success");
                validTokens = true;
            }

            @Override
            public void onError(String error) {
                Log.d(TAG, "Validation failed");
            }
        };
    }

    public void updateIntegrationReference(IntegrationActivity newActivity) {
        weakIntegrationActivity = new WeakReference(newActivity);
    }

    public void updateMainReference(Activity activity) {
        weakMainActivity = new WeakReference(activity);
    }

    private static final String TAG = "ok";
}
