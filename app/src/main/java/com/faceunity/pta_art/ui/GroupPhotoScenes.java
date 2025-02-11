package com.faceunity.pta_art.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.faceunity.pta_art.R;
import com.faceunity.pta_art.constant.FilePathFactory;
import com.faceunity.pta_art.entity.Scenes;

/**
 * Created by tujh on 2018/12/18.
 */
public class GroupPhotoScenes extends RelativeLayout {
    public static final String TAG = GroupPhotoScenes.class.getSimpleName();

    private RecyclerView mSingleRecycler;
    private RecyclerView mMultipleRecycler;
    private RecyclerView mAnimationRecycler;

    public GroupPhotoScenes(@NonNull Context context) {
        this(context, null);
    }

    public GroupPhotoScenes(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GroupPhotoScenes(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_group_photo_scenes, this, true);

        view.findViewById(R.id.group_photo_scenes_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBackRunnable != null) {
                    mBackRunnable.run();
                }
            }
        });

        if (FilePathFactory.singleScenes().length > 0) {
            mSingleRecycler = view.findViewById(R.id.group_photo_scenes_single_recycler);
            mSingleRecycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            mSingleRecycler.setAdapter(new ScenesAdapter(FilePathFactory.singleScenes()));
            ((SimpleItemAnimator) mSingleRecycler.getItemAnimator()).setSupportsChangeAnimations(false);
            mSingleRecycler.setVisibility(VISIBLE);
            view.findViewById(R.id.group_photo_scenes_single_text).setVisibility(VISIBLE);
        }

        if (FilePathFactory.multipleScenes().length > 0) {
            mMultipleRecycler = view.findViewById(R.id.group_photo_scenes_multiple_recycler);
            mMultipleRecycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            mMultipleRecycler.setAdapter(new ScenesAdapter(FilePathFactory.multipleScenes()));
            ((SimpleItemAnimator) mMultipleRecycler.getItemAnimator()).setSupportsChangeAnimations(false);
            mMultipleRecycler.setVisibility(VISIBLE);
            view.findViewById(R.id.group_photo_scenes_multiple_text).setVisibility(VISIBLE);
        }

        if (FilePathFactory.animationScenes().length > 0) {
            mAnimationRecycler = view.findViewById(R.id.group_photo_animation_recycler);
            mAnimationRecycler.setLayoutManager(new LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false));
            mAnimationRecycler.setAdapter(new ScenesAdapter(FilePathFactory.animationScenes()));
            ((SimpleItemAnimator) mAnimationRecycler.getItemAnimator()).setSupportsChangeAnimations(false);
            mAnimationRecycler.setVisibility(VISIBLE);
            view.findViewById(R.id.group_photo_animation_text).setVisibility(VISIBLE);
        }
    }

    class ScenesAdapter extends RecyclerView.Adapter<ScenesAdapter.ScenesHolder> {
        Scenes[] scenes;

        public ScenesAdapter(Scenes[] scenes) {
            this.scenes = scenes;
        }

        @NonNull
        @Override
        public ScenesAdapter.ScenesHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ScenesHolder(new ImageView(getContext()));
        }

        @Override
        public void onBindViewHolder(@NonNull ScenesAdapter.ScenesHolder holder, final int position) {
            holder.image.setImageResource(scenes[position].resId);
            holder.image.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mScenesSelectListener != null) {
                        mScenesSelectListener.onScenesSelectListener(scenes == FilePathFactory.animationScenes(), scenes[position]);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return scenes.length;
        }

        class ScenesHolder extends RecyclerView.ViewHolder {
            ImageView image;

            public ScenesHolder(View itemView) {
                super(itemView);
                image = (ImageView) itemView;
                image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
                image.setLayoutParams(new ViewGroup.LayoutParams(getResources().getDimensionPixelSize(R.dimen.x280), ViewGroup.LayoutParams.MATCH_PARENT));
                int len = getResources().getDimensionPixelSize(R.dimen.x10);
                image.setPadding(len, 0, len, 0);
            }
        }
    }

    private Runnable mBackRunnable;

    public void setBackRunnable(Runnable backRunnable) {
        this.mBackRunnable = backRunnable;
    }

    private ScenesSelectListener mScenesSelectListener;

    public void setScenesSelectListener(ScenesSelectListener scenesSelectListener) {
        this.mScenesSelectListener = scenesSelectListener;
    }

    public interface ScenesSelectListener {
        void onScenesSelectListener(boolean isAnim, Scenes scenes);
    }
}
