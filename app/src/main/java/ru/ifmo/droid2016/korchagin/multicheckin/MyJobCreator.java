package ru.ifmo.droid2016.korchagin.multicheckin;

import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

import java.util.Vector;

import ru.ifmo.droid2016.korchagin.multicheckin.integration.*;
import ru.ifmo.droid2016.korchagin.multicheckin.utils.IntegrationsUtil;

/**
 * Created by ME on 20.12.2016.
 */

public class MyJobCreator implements JobCreator{
    private static final String TAG = "JobCreator";
    @Override
    public Job create(String tag) {
        Vector<SocialIntegration> vector = IntegrationsUtil.getSelectedIntegrations();

        if (tag.equals(SendToAllJob.TAG)) {
            return new SendToAllJob();
        }

        for (SocialIntegration AbstractIntegration : vector) {
            if (tag.equals(AbstractIntegration.getSandJobTag())) {
                return AbstractIntegration.getJob();
            }
        }

        Log.e(TAG, "Job with nonexistent tag " + tag + " was created");
        return null;
    }
}
