package ru.ifmo.droid2016.korchagin.multicheckin;

import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobCreator;

import ru.ifmo.droid2016.korchagin.multicheckin.integration.*;

/**
 * Created by ME on 20.12.2016.
 */

public class MyJobCreator implements JobCreator{
    private static final String TAG = "JobCreator";
    @Override
    public Job create(String tag) {
        switch (tag){
            case SendToAllJob.TAG:
                return new SendToAllJob();
            case VKIntegration.VKSendJob.TAG:
                return new VKIntegration.VKSendJob();
            default:
                Log.e(TAG, "Job with nonexistent tag " + tag + " was created");
                return null;
        }
    }
}
