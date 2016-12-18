package ru.ifmo.droid2016.korchagin.multicheckin.integration;
import android.util.Log;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.appevents.AppEventsLogger;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;

import org.json.JSONObject;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import ru.ifmo.droid2016.korchagin.multicheckin.MainActivity;

public class FacebookIntegration {
    static final String LOG_TAG = "facebook_integration";

    private static MainActivity activity;

    private static void testRequest() {

        GraphRequest request = GraphRequest.newMeRequest(
                AccessToken.getCurrentAccessToken(),
                new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        Log.d(LOG_TAG, response.toString() + "\n" + object.toString());
                    }
                }
        );


    }


    public static CallbackManager init(MainActivity newActivity) {
        activity = newActivity;
        CallbackManager facebookCallbackManager;

        facebookCallbackManager = CallbackManager.Factory.create();

        LoginManager.getInstance().registerCallback(facebookCallbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(LoginResult loginResult) {
                        Log.d(LOG_TAG, "Залогинились успешно.");

                        AccessToken.setCurrentAccessToken(loginResult.getAccessToken());
                    }

                    @Override
                    public void onCancel() {
                        Log.d(LOG_TAG, "Отмена входа.");
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d(LOG_TAG, "Ошибка : " + error.getMessage());
                    }
                }
        );

        return facebookCallbackManager;
    }

    private static List permissions = Arrays.asList("public_profile", "user_friends");

    public static void login() {
        if ((activity == null) || (activity.isFinishing())) {
            Log.d(LOG_TAG, "Кто-то набажил : (activity == null) || (activity.isFinishing()) ");
            return;
        }
        Log.d(LOG_TAG, "in_login");
        LoginManager.getInstance().logInWithReadPermissions(activity, permissions);
    }

    public static void unLogin() {
        AccessToken.setCurrentAccessToken(null);
        Log.d(LOG_TAG, "unlogin");
    }


}
