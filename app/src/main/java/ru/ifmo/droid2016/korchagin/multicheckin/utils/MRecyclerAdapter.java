package ru.ifmo.droid2016.korchagin.multicheckin.utils;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Vector;

import ru.ifmo.droid2016.korchagin.multicheckin.MainApplication;
import ru.ifmo.droid2016.korchagin.multicheckin.R;
import ru.ifmo.droid2016.korchagin.multicheckin.integration.SocialIntegration;

public class MRecyclerAdapter extends RecyclerView.Adapter<MRecyclerAdapter.ViewHolder> {


    private Vector<SocialIntegration> socialNetworks = new Vector<>();
    private static WeakReference<Activity> weakReference = null;
    public Vector<SocialIntegration> getData() {
        return socialNetworks;
    }
    private boolean []prevState;


    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView iconOfNetwork;
        private TextView nameOfNetwork;
        private CheckBox statusOfNetwork;
        private ImageButton loginLogoutButton;

        private ViewHolder(View v) {
            super(v);

            iconOfNetwork = (ImageView) v.findViewById(R.id.icon_of_network);
            nameOfNetwork = (TextView) v.findViewById(R.id.name_of_network);
            statusOfNetwork = (CheckBox) v.findViewById(R.id.status_of_network);
            loginLogoutButton = (ImageButton) v.findViewById(R.id.login_logout_button);
        }

        private void onLogin() {
            if (weakReference.get() == null) {
                return;
            }
            loginLogoutButton.setImageDrawable(weakReference.get().getResources().getDrawable(R.mipmap.ic_logout));
            Animation animation = AnimationUtils.loadAnimation(weakReference.get(), R.anim.login_logout_stretching_animation);
            loginLogoutButton.startAnimation(animation);
        }

        private void onLogout() {
            loginLogoutButton.setImageDrawable(weakReference.get().getResources().getDrawable(R.mipmap.ic_login));
            Animation animation = AnimationUtils.loadAnimation(weakReference.get(), R.anim.login_logout_stretching_animation);
            loginLogoutButton.startAnimation(animation);
        }
    }

    public MRecyclerAdapter(Vector<SocialIntegration> socialNetworks, Map<String, Integer> posInAdapter, Activity activity) {
        super();
        weakReference = new WeakReference<>(activity);
        this.socialNetworks = socialNetworks;

        prevState = new boolean[socialNetworks.size()];

        int pos = 0;
        for(SocialIntegration w : socialNetworks) {
            posInAdapter.put(w.getNetworkName(), pos);
            prevState[pos] = w.getStatus();

            pos++;
        }
    }

    @Override
    public MRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.element_of_recycle_view, parent, false);

        return new MRecyclerAdapter.ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(final MRecyclerAdapter.ViewHolder holder, int position) {
        Drawable mIcon = socialNetworks.elementAt(position).getIcon();

        Log.d("MRecyclerAdapter", String.valueOf(position));

        if (mIcon != null) {
            holder.iconOfNetwork.setImageDrawable(mIcon);
        }

        SocialIntegration socialIntegration = socialNetworks.elementAt(position);

        holder.nameOfNetwork.setText(socialIntegration.getNetworkNameLocalized());
        boolean status = socialNetworks.elementAt(position).getStatus();

        if (status != prevState[position]) {
            if (status) {
                holder.onLogin();
            } else {
                holder.onLogout();
            }
        }
        prevState[position] = status;

        if (status) {
            holder.loginLogoutButton.setImageDrawable(weakReference.get().getResources().getDrawable(R.mipmap.ic_logout));
        } else {
            holder.loginLogoutButton.setImageDrawable(weakReference.get().getResources().getDrawable(R.mipmap.ic_login));
        }

        if (status) {
            holder.statusOfNetwork.setVisibility(View.VISIBLE);
            Integer res = MainApplication.selectedSocialIntegrations.get(socialIntegration.getNetworkName());
            if (res == null) {
                res = 0;
            }
            boolean isSelected = (res != 0);
            holder.statusOfNetwork.setChecked(isSelected);

            holder.statusOfNetwork.setOnClickListener(
                    new View.OnClickListener() {
                        private final MRecyclerAdapter.ViewHolder mHolder = holder;

                        @Override
                        public void onClick(View v) {
                            int position = mHolder.getAdapterPosition();

                            SocialIntegration socialIntegration = socialNetworks.elementAt(mHolder.getAdapterPosition());
                            Integer res = MainApplication.selectedSocialIntegrations.get(socialIntegration.getNetworkName());

                            MainApplication.selectedSocialIntegrations.put(socialIntegration.getNetworkName(), res ^ 1);
                            SharedPreferencesUtil.saveSelectionStatus(weakReference.get().getApplicationContext(), socialIntegration.getNetworkName(), res ^ 1);
                            notifyItemChanged(position);
                        }
                    }
            );
        } else {
            holder.statusOfNetwork.setVisibility(View.INVISIBLE);
        }
        holder.loginLogoutButton.setOnClickListener(
                new View.OnClickListener() {
                    private final MRecyclerAdapter.ViewHolder mHolder = holder;
                    
                    @Override
                    public void onClick(View v) {
                        int position = mHolder.getAdapterPosition();
                        if (socialNetworks.elementAt(position).getStatus()) {
                            socialNetworks.elementAt(position).logout();

                            notifyItemChanged(position);
                        } else {
                            Animation animation = AnimationUtils.loadAnimation(weakReference.get(), R.anim.login_logout_animation);

                            mHolder.loginLogoutButton.startAnimation(animation);
                            socialNetworks.elementAt(position).login();
                        }
                    }
                }
        );
    }

    @Override
    public int getItemCount() {
        return socialNetworks.size();
    }
}
