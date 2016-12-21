package ru.ifmo.droid2016.korchagin.multicheckin.integration;

import android.content.Context;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.util.Log;

import com.evernote.android.job.Job;
import com.evernote.android.job.JobRequest;
import com.evernote.android.job.util.support.PersistableBundleCompat;

import java.util.Vector;

import ru.ifmo.droid2016.korchagin.multicheckin.utils.IntegrationsUtil;

/**
 * Created by ME on 20.12.2016.
 */

public class SendToAllJob extends Job {

    public static final String TAG = "SendToAllNetworksJob";
    public static final String PHOTO_TAG = "photo";
    public static final String MSG_TAG = "message";
    @NonNull
    @Override
    protected Result onRunJob(Params params) {
        PersistableBundleCompat extras = params.getExtras();
        if(!extras.containsKey(PHOTO_TAG)){
            Log.e(TAG, "Created send job without photo data");
        }
        long executionWindowStart,executionWindowEnd;
        ConnectivityManager check = (ConnectivityManager)getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if(check.getActiveNetworkInfo() != null && check.getActiveNetworkInfo().isConnected()){
            /*
             if connection available, start now but not later than 30 seconds
              */
            executionWindowStart = 1;
            executionWindowEnd = 30 * 1000;
        } else {
            /*
             if no connection, start after 30 seconds but not later than 5 minutes
              */
            executionWindowStart = 30 * 1000;
            executionWindowEnd = 5 * 60 * 1000;
        }

        Vector<SocialIntegration> vector = IntegrationsUtil.getSelectedIntegrations();

        Log.d("NEW", String.valueOf(vector.size()));

        for (SocialIntegration AbstractIntegration : vector) {
            Log.d("NEW", AbstractIntegration.getSandJobTag());

            new JobRequest.Builder(AbstractIntegration.getSandJobTag()) // build Abstract-Job
                    .setExtras(extras)
                    .setExecutionWindow(executionWindowStart, executionWindowEnd)
                    .setBackoffCriteria(15_000L, JobRequest.BackoffPolicy.LINEAR)
                    .setRequiredNetworkType(JobRequest.NetworkType.CONNECTED)
                    .setRequirementsEnforced(true)
                    .setPersisted(true)
                    .setUpdateCurrent(false)
                    .build().schedule();
        }

        return Result.SUCCESS;
    }
}
