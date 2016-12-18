package ru.ifmo.droid2016.korchagin.multicheckin.integration;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.facebook.CallbackManager;

import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

import ru.ifmo.droid2016.korchagin.multicheckin.R;
import ru.ifmo.droid2016.korchagin.multicheckin.utils.MRecyclerAdapter;

public class IntegrationActivity extends AppCompatActivity {
    static final String LOG_TAG = "facebook_integration";

    private CallbackManager facebookCallbackManager;

    public static final String NEW_NETWORK_IS_LOGGED = "NEW_NETWORK_IS_LOGGED";
    public static final String NETWORK_NAME = "NETWORK_NAME";


    private RecyclerView mRecyclerView;
    private MRecyclerAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    private Map<String, Integer> posInAdapter = new TreeMap<>(); // Позиция в адаптере
    // после успешного логирования надо вызвать у mAdapter notifyItemChanged()

    private BroadcastReceiver networkLoggingReceiver;



    void initReceivers() {
        networkLoggingReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals(NEW_NETWORK_IS_LOGGED)) {
                    String name = intent.getStringExtra(NETWORK_NAME);

                    Log.d("INTEGRATION_ACTIVITY", name);

                    Log.d("INTEGRATION_ACTIVITY", "Залогинился номер :" + posInAdapter.get(name));

                    mAdapter.notifyItemChanged(posInAdapter.get(name));
                }
            }
        };

        registerReceivers();
    }

    void registerReceivers() {
        registerReceiver(networkLoggingReceiver, new IntentFilter(NEW_NETWORK_IS_LOGGED));
    }

    void unregisterReceivers() {
        this.unregisterReceiver(networkLoggingReceiver);
    }

    void initRecyclerView() {
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        Vector<SocialIntegration> networks = new Vector<>();

        networks.addElement(new FacebookIntegration());

        // TODO  добавить сюда все Integration-ы

        mAdapter = new MRecyclerAdapter(networks, posInAdapter);
        mRecyclerView.setAdapter(mAdapter);

        // TODO КАЖДОМУ послать broadcast после успешного логирования!
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_integration);

        initRecyclerView();
        initReceivers();

        facebookCallbackManager = FacebookIntegration.init(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (facebookCallbackManager.onActivityResult(requestCode, resultCode, data)) {
            Log.d(LOG_TAG, "OK");
            return;
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceivers();
    }
}
