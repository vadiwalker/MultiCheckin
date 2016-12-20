package ru.ifmo.droid2016.korchagin.multicheckin.integration;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.List;

import ru.ifmo.droid2016.korchagin.multicheckin.R;

public class FacebookIntegration implements SocialIntegration{
    private static final String LOG_TAG = "facebook_integration";

    private WeakReference<Activity> weakActivity;

    private FacebookIntegration() {
        weakActivity = new WeakReference<Activity>(null);
    }

    private static FacebookIntegration mInstance;

    public static FacebookIntegration getmInstance() {
        if (mInstance != null) {
            return mInstance;
        } else {
            mInstance = new FacebookIntegration();
            return mInstance;
        }
    }

    public void testRequest() {
        if (AccessToken.getCurrentAccessToken() == null) {
            Log.d(LOG_TAG, "int testRequest no AccessToken");
            return;
        }

        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.d(LOG_TAG, response.toString() + "\n" + object.toString());
                    }
                }
        );

        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,link");
        request.setParameters(parameters);
        request.executeAsync();
    }

    public CallbackManager init(IntegrationActivity newActivity) {
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
                                            .putExtra(IntegrationActivity.NETWORK_NAME, "Фейсбук")
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
    public String getName() {
        return "Фейсбук";
    }

    @Override
    public boolean getStatus() {
        Log.d(LOG_TAG, String.valueOf((AccessToken.getCurrentAccessToken() != null)));
        return (AccessToken.getCurrentAccessToken() != null);
    }

}
