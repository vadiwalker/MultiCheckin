package ru.ifmo.droid2016.korchagin.multicheckin.integration;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by Vlad_kv on 18.12.2016.
 */

public interface SocialIntegration {

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
    @NonNull String getName();

    /**
     * No idea on this one
     * @return Something
     */
    boolean getStatus();
}
