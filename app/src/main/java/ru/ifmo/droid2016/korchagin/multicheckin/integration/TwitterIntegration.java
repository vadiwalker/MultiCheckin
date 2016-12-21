package ru.ifmo.droid2016.korchagin.multicheckin.integration;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.util.support.PersistableBundleCompat;

import java.lang.ref.WeakReference;

import ru.ifmo.droid2016.korchagin.multicheckin.R;
import ru.ifmo.droid2016.korchagin.multicheckin.utils.BitmapFileUtil;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;

/**
 * Created by ME on 20.12.2016.
 */

public class TwitterIntegration implements SocialIntegration {

    @Override
    public String getSandJobTag() {
        return TwitterIntegration.TAG;
    }

    @Override
    public Job getJob() {
        return new TwitterSendJob();
    }

    public static class TwitterSendJob extends Job {
        public static final String TAG = "SendPhotoToTwitterJob";
        @NonNull
        @Override
        protected Result onRunJob(Params params) {

            PersistableBundleCompat extras = getParams().getExtras();
            String photoPath = extras.getString(SendToAllJob.PHOTO_TAG, null);
            String message = extras.getString(SendToAllJob.MSG_TAG, null);
            Twitter twitter = TwitterFactory.getSingleton();
            StatusUpdate update = new StatusUpdate(message);
            update.setMedia(BitmapFileUtil.getFileFromPath(photoPath));
            try{
                twitter.updateStatus(update);
            } catch (TwitterException e){
                return Result.RESCHEDULE;
            }
            return Result.SUCCESS;
        }
    }

    private static final String TAG = "TwitterIntegration";

    public static final String TOKEN = "twitter_auth_token";

    public static final String SECRET = "twitter_auth_token_secret";

    private WeakReference<Activity> weakActivity;

    private TwitterIntegration() {
        weakActivity = new WeakReference<>(null);
    }

    private static TwitterIntegration mInstance;

    public static TwitterIntegration getInstance(){
        if(mInstance != null){
            return mInstance;
        } else {
            mInstance = new TwitterIntegration();
            return mInstance;
        }
    }

    public void updateActivityReference(Activity activity){
        weakActivity = new WeakReference<>(activity);
    }

    @Override
    public void login() {
        Activity activity = weakActivity.get();
        if(activity == null){
            return;
        }
        Intent twitterIntent = new Intent();
        twitterIntent.setClassName(activity.getBaseContext(), TwitterIntegrationLoginActivity.class.getCanonicalName());
        activity.startActivityForResult(twitterIntent, SocialIdentifier.TWITTER_AUTH);
    }

    @Override
    public void logout() {
        TwitterFactory.getSingleton().setOAuthAccessToken(null);
        Activity activity = weakActivity.get();
        if(activity == null){
            return;
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(TOKEN);
        editor.remove(SECRET);
        editor.apply();
    }

    @Nullable
    @Override
    public Drawable getIcon() {
        Activity activity = weakActivity.get();
        if(activity == null) {
            return null;
        } else {
            return activity.getResources().getDrawable(R.mipmap.ic_twitter);
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
        return "Twitter";
    }

    @Override
    public boolean getStatus() {
        Activity activity = weakActivity.get();
        if(activity == null){
            return false;
        }
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activity.getApplicationContext());
        return prefs.contains(TOKEN) && prefs.contains(SECRET);
    }

    public void applicationInit(Context appContext){
        TwitterFactory.getSingleton()
                .setOAuthConsumer("umXFKIidggWmK6waeDhP3fA1f", "rK5kBhSJuaybp4FMhM5SeZ7YKkTz7gtQQB4qVaVZC3nRqfcbeX");
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(appContext);
        if(prefs.contains(TOKEN) && prefs.contains(SECRET)){
            AccessToken token = new AccessToken(prefs.getString(TOKEN, ""), prefs.getString(SECRET, ""));
            TwitterFactory.getSingleton().setOAuthAccessToken(token);
        }
    }

    public void tryLoginFinish(int requestCode, int resultCode, Intent data){
        if(getStatus()){
            Log.d(TAG, "Twitter login successful");
            Activity activity = weakActivity.get();
            if(activity != null) {
                Intent successIntent = new Intent(IntegrationActivity.NEW_NETWORK_IS_LOGGED);
                successIntent.putExtra(IntegrationActivity.NETWORK_NAME, getNetworkName());
                activity.sendBroadcast(successIntent);
            }
        }
    }
}
