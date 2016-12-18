package ru.ifmo.droid2016.korchagin.multicheckin.integration;

import android.graphics.Bitmap;

/**
 * Created by Vlad_kv on 18.12.2016.
 */

public interface SocialIntegration {
    void login();
    void logout();

    Bitmap getIcon();
    String getName();

    boolean getStatus();
}
