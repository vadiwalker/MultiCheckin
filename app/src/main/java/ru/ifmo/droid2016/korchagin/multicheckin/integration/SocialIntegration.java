package ru.ifmo.droid2016.korchagin.multicheckin.integration;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.evernote.android.job.Job;

/**
 * Created by Vlad_kv on 18.12.2016.
 */

public interface SocialIntegration {


    String getSandJobTag();

    Job getJob();

    /**
     *   This method is to start login process; no idea where it finishes
     */
    void login();
    /**
     *   This method is to log out; usually simple
     */
    void logout();
    /**
     *   This method should return Drawable icon of social network.
     * @return Drawable icon or null if none
     */
    @Nullable Drawable getIcon();

    /**
     *   This method should return String name of social network.
     * @return Name of social network
     */
    @NonNull String getNetworkNameLocalized();

    @NonNull String getNetworkName();

    /**
     *  This method should return true if there is exists saved asses token, and false otherwise.
     */
    boolean getStatus();
}
