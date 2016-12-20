package ru.ifmo.droid2016.korchagin.multicheckin.integration;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.StrictMode;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import ru.ifmo.droid2016.korchagin.multicheckin.R;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

public class TwitterIntegrationLoginActivity extends AppCompatActivity {

    public final static int RESULT_FAILURE = 2;

    WebView view;
    RequestToken requestToken;

    private static final String RQ_TOKEN = "RequestToken";
    AccessToken accessToken;

    public class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) { // only deprecated after API 24
            Log.d("TwAct", "Loading URL " + url);
            if(url.startsWith("http://localhost/sign-in-with-twitter/")){
                onCallbackReceived(url);
                return true;
            }
            return super.shouldOverrideUrlLoading(view, url);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter_integration_login);
        view = (WebView) findViewById(R.id.twitterAuthWebView);
        view.setWebViewClient(new MyWebViewClient());
        if(savedInstanceState == null){
            Twitter twitter = TwitterFactory.getSingleton();
            try{
                StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
                StrictMode.setThreadPolicy(policy);
                requestToken = twitter.getOAuthRequestToken();
                view.loadUrl(requestToken.getAuthorizationURL());
            } catch (Exception e){
                e.printStackTrace();
                setResult(RESULT_FAILURE);
                twitter.setOAuthAccessToken(null);
                finish();
            }
        } else {
            requestToken = (RequestToken) savedInstanceState.getSerializable(RQ_TOKEN);
            view.loadUrl(requestToken.getAuthorizationURL());
        }
    }

    void onCallbackReceived(String callbackUri){
        Uri uri = Uri.parse(callbackUri);
        String requestTokenString = uri.getQueryParameter("oauth_token");
        String requestVerifierString = uri.getQueryParameter("oauth_verifier");
        Twitter twitter = TwitterFactory.getSingleton();
        if(!requestTokenString.equals(requestToken.getToken())){
            Log.e("TwAct", "Callback was called with different oauth token");
        }
        try {
            accessToken = twitter.getOAuthAccessToken(requestVerifierString);
        } catch (TwitterException e) {
            e.printStackTrace();
            TwitterFactory.getSingleton().setOAuthAccessToken(null);
            setResult(RESULT_FAILURE);
            finish();
        }
        if(accessToken != null){
            Log.d("TwAct", "Authorized successfully");
            twitter.setOAuthAccessToken(accessToken);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(TwitterIntegration.TOKEN, accessToken.getToken());
            editor.putString(TwitterIntegration.SECRET, accessToken.getTokenSecret());
            editor.commit();
            setResult(RESULT_OK);
            finish();
        } else {
            Log.d("TwAct", "Access token is null");
            setResult(RESULT_FAILURE);
            finish();
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putSerializable(RQ_TOKEN, requestToken);
    }

    @Override
    public void onBackPressed() {
        // When the user hits the back button set the resultCode
        // to Activity.RESULT_CANCELED to indicate a failure
        setResult(Activity.RESULT_CANCELED);
        TwitterFactory.getSingleton().setOAuthAccessToken(null);
        super.onBackPressed();
    }
}
