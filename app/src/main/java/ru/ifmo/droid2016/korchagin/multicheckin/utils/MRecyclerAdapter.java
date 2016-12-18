package ru.ifmo.droid2016.korchagin.multicheckin.utils;

import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Map;
import java.util.Vector;

import ru.ifmo.droid2016.korchagin.multicheckin.R;
import ru.ifmo.droid2016.korchagin.multicheckin.integration.SocialIntegration;

public class MRecyclerAdapter extends RecyclerView.Adapter<MRecyclerAdapter.ViewHolder> {


    protected Vector<SocialIntegration> socialNetworks = new Vector<>();

    public Vector<SocialIntegration> getData() {
        return socialNetworks;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView iconOfNetwork;
        public TextView nameOfNetwork;
        public CheckBox statusOfNetwork;

        public ViewHolder(View v) {
            super(v);

            iconOfNetwork = (ImageView) v.findViewById(R.id.icon_of_network);
            nameOfNetwork = (TextView) v.findViewById(R.id.name_of_network);
            statusOfNetwork = (CheckBox) v.findViewById(R.id.status_of_network);
        }
    }

    public MRecyclerAdapter(Vector<SocialIntegration> socialNetworks, Map<String, Integer> posInAdapter) {
        super();

        this.socialNetworks = socialNetworks;

        int pos = 0;
        for(SocialIntegration w : socialNetworks) {
            posInAdapter.put(w.getName(), pos);
            pos++;
        }

        Log.d("WTF", String.valueOf(pos) + " !!!!!!!!!!!!!!!!!");

    }

    @Override
    public MRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.element_of_recycle_view, parent, false);

        MRecyclerAdapter.ViewHolder vh = new MRecyclerAdapter.ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final MRecyclerAdapter.ViewHolder holder, final int position) {
        Drawable mIcon = socialNetworks.elementAt(position).getIcon();

        Log.d("MRecyclerAdapter", String.valueOf(position));

        if (mIcon != null) {
            holder.iconOfNetwork.setImageDrawable(mIcon);
        }

        holder.nameOfNetwork.setText(socialNetworks.elementAt(position).getName());

        holder.statusOfNetwork.setChecked(socialNetworks.elementAt(position).getStatus());

        holder.statusOfNetwork.setOnClickListener(
                new View.OnClickListener() {
                    private final MRecyclerAdapter.ViewHolder mHolder = holder;

                    @Override
                    public void onClick(View v) {
                        int position = mHolder.getAdapterPosition();
                        if (socialNetworks.elementAt(position).getStatus()) {
                            socialNetworks.elementAt(position).logout();

                            notifyItemChanged(position);

                            Log.d("WTF", "in");

                        } else {
                            socialNetworks.elementAt(position).login();

                            notifyItemChanged(position);

                            Log.d("WTF", "out");
                        }
                    }
                }

        );

    }

    @Override
    public int getItemCount() {
        return socialNetworks.size();
    }

    public void addItemToEnd(SocialIntegration element) {
        socialNetworks.addElement(element);
        notifyItemChanged(socialNetworks.size() - 1);
    }
}
