package com.inertia.phyzmo.gallery;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Point;
import android.os.Bundle;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;
import com.inertia.phyzmo.R;
import com.inertia.phyzmo.datadisplay.DisplayDataActivity;
import com.inertia.phyzmo.firebase.FirebaseStorageUtils;

import net.igenius.customcheckbox.CustomCheckBox;

import java.util.ArrayList;

public class GalleryViewAdapter extends RecyclerView.Adapter<GalleryViewAdapter.ViewHolder> {

    private ArrayList<String> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private Context mContext;
    private RecyclerView mRecyclerView;
    private GalleryActivity mActivity;
    private boolean mSelectMode;
    private ArrayList<String> mSelectedToDelete;

    GalleryViewAdapter(Context context, GalleryActivity a, ArrayList<String> data) {
        this.mSelectMode = false;
        this.mContext = context;
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mActivity = a;
        this.mSelectedToDelete = new ArrayList<>();
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.video_preview_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, final int position) {
        final Display display = ((Activity)mContext).getWindowManager().getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);

        RequestOptions requestOptions = new RequestOptions();
        requestOptions = requestOptions.transforms(new CenterCrop(), new RoundedCorners(75));

        Glide.with(holder.thumbnail.getContext())
                .load(FirebaseStorageUtils.getThumbnailUrl(mData.get(position)))
                .apply(requestOptions)
                .placeholder(R.drawable.gray_square)
                .dontAnimate()
                .into(holder.thumbnail);
        holder.thumbnail.setClipToOutline(true);

        if (mSelectMode) {
            holder.checkbox.setVisibility(View.VISIBLE);
        } else {
            holder.checkbox.setVisibility(View.INVISIBLE);
            holder.checkbox.setChecked(false);
        }

        holder.checkbox.setClickable(false);

        holder.checkbox.setOnClickListener(v -> holder.thumbnail.callOnClick());

        holder.thumbnail.setOnClickListener(v -> {
            if (mSelectMode) {
                holder.checkbox.setChecked(!holder.checkbox.isChecked(), true);
                if (holder.checkbox.isChecked()) {
                    mSelectedToDelete.add(mData.get(position));
                } else {
                    mSelectedToDelete.remove(mData.get(position));
                }
                if (mSelectedToDelete.isEmpty()) {
                    mActivity.findViewById(R.id.deleteVideo).setBackgroundTintList(ColorStateList.valueOf(mActivity.getResources().getColor(R.color.ap_gray)));
                } else {
                    mActivity.findViewById(R.id.deleteVideo).setBackgroundTintList(ColorStateList.valueOf(mActivity.getResources().getColor(R.color.red_btn_bg_color)));
                }
            } else {
                System.out.println("Thumbnail for video with ID=" + mData.get(position) + " clicked.");
                Intent intent = new Intent(mContext, DisplayDataActivity.class);
                Bundle mBundle = new Bundle();
                mBundle.putString("video_url", mData.get(position));
                mBundle.putBoolean("existing_video", true);
                intent.putExtras(mBundle);
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public void setData(ArrayList<String> d){
        this.mData = d;
        this.notifyDataSetChanged();
    }

    public void setSelectMode(boolean b) {
        this.mSelectMode = b;
        this.notifyDataSetChanged();
        if (!b) {
            this.mSelectedToDelete.clear();
            mActivity.findViewById(R.id.deleteVideo).setBackgroundTintList(ColorStateList.valueOf(mActivity.getResources().getColor(R.color.ap_gray)));
        }
    }

    public boolean getSelectMode() {
        return this.mSelectMode;
    }

    public ArrayList<String> getSelectedToDelete() {
        return this.mSelectedToDelete;
    }

    public ArrayList<String> getData() {
        return mData;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        ImageView thumbnail;
        View itemView;
        CustomCheckBox checkbox;

        ViewHolder(View itemView) {
            super(itemView);
            this.itemView = itemView;
            thumbnail = itemView.findViewById(R.id.thumbnailImage);
            checkbox = itemView.findViewById(R.id.deleteObjectCheckbox);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);

        mRecyclerView = recyclerView;
    }
}