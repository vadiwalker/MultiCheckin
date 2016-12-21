package ru.ifmo.droid2016.korchagin.multicheckin;

import android.preference.PreferenceActivity;

import java.util.List;

/**
 * Created by ME on 17.12.2016.
 */

public class ServicesActivity extends PreferenceActivity {
    @Override
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.services_header, target);
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return ServicesFragment.class.getName().equals(fragmentName);
    }
}
