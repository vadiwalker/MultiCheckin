package ru.ifmo.droid2016.korchagin.multicheckin;

import android.os.Bundle;
import android.preference.PreferenceFragment;

/**
 * Created by ME on 17.12.2016.
 */

public class ServicesFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.services);
    }
}
