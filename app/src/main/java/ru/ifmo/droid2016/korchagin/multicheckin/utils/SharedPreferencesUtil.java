package ru.ifmo.droid2016.korchagin.multicheckin.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import ru.ifmo.droid2016.korchagin.multicheckin.MainApplication;

class SharedPreferencesUtil {
    static void saveSelectionStatus(Context context, String name, int val) {
        SharedPreferences mySharedPreferences = context.getSharedPreferences(MainApplication.APP_PREFERENCES, Context.MODE_PRIVATE);
        SharedPreferences.Editor edit = mySharedPreferences.edit();

        Log.d("LOG", "put " + name + " " + val);
        edit.putInt(name, val);
        edit.apply();
    }
}