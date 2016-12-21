package ru.ifmo.droid2016.korchagin.multicheckin.odnoklassniki;

import android.app.Activity;
import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONObject;

import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.OkListener;
import ru.ok.android.sdk.util.OkAuthType;
import ru.ok.android.sdk.util.OkScope;

/**
 * Created by vadim on 19.12.16.
 */

public class OkIntegration {

    private static final String APP_ID = "1249212672";
    private static final String PUBLIC_KEY = "CBAKHGHLEBABABABA";
    private static final String SECRET_KEY = "EFC90F68D96FB26BDD678927";
    private static final String REDIRECT_URI = "okauth://ok1249212672";

    protected Odnoklassniki ok;
    Context context;
    Activity activity;

    public OkIntegration(Context context, Activity activity) {
        this.context = context;
        this.activity = activity;
        if (!Odnoklassniki.hasInstance())
            ok = Odnoklassniki.createInstance(context, APP_ID, PUBLIC_KEY);
        else
            ok = Odnoklassniki.getInstance();
    }

    public void login() {
        Log.d(TAG, "login");
        ok.requestAuthorization(activity, REDIRECT_URI, OkAuthType.ANY, OkScope.VALUABLE_ACCESS, OkScope.LONG_ACCESS_TOKEN);
    }

    public void logout() {
        Log.d(TAG, "logout");
        ok.clearTokens();
    }

    public void post() {
        Log.d(TAG, "post");
        new Thread(new Runnable() {
            public void run() {
                Log.d(TAG, "run");
                try {
                    ok.request("users.getCurrentUser", null, "get", new OkListener() {

                        @Override
                        public void onSuccess(JSONObject json) {
                            Log.d(TAG, "Success: " + json.toString());
                        }

                        @Override
                        public void onError(String error) {
                            Log.d(TAG, "Error: " + error);
                        }

                    });
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

    }

    private static final String TAG = "OkIntegration";
}
