package com.zplay.zplayads;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * lgd on 2017/10/12.
 */

class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.GalleryViewHolder> {


    private ArrayList<String> mData;

    GalleryAdapter(ArrayList<String> data) {
        mData = data;
    }

    @Override
    public GalleryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_gallery, parent, false);
        return new GalleryViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final GalleryViewHolder holder, int position) {
        Glide.with(holder.imageView).load(mData.get(position)).into(holder.imageView);
        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String path = mData.get(holder.getAdapterPosition());
                if (v.getContext() instanceof GalleryActivity) {
                    GalleryActivity activity = (GalleryActivity) v.getContext();
                    Intent i = new Intent();
                    i.putExtra(GalleryActivity.EXTRA_PATH, path);
                    activity.setResult(Activity.RESULT_OK, i);
                    activity.finish();
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    static class GalleryViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.ig_imageView)
        ImageView imageView;

        GalleryViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
