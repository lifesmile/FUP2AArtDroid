package com.faceunity.pta_art.ui;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.faceunity.pta_art.R;

/**
 * Created by tujh on 2018/6/15.
 */
public class LoadingLayout extends FrameLayout {
    private String mLoadingStr = "模型生成中";

    private TextView mLoadingText;
    private ImageView mLoadingImg;
    private AnimationDrawable mLoadingAnimation;

    private Runnable mLoadingAnimRunnable = new Runnable() {
        int time;

        @Override
        public void run() {
            StringBuilder str = new StringBuilder(mLoadingStr);
            int t = (++time % 4);
            for (int i = 0; i < t; i++) {
                str.append(".");
            }
            mLoadingText.setText(str);
            mLoadingText.postDelayed(mLoadingAnimRunnable, 500);
        }
    };

    public LoadingLayout(Context context) {
        this(context, null);
    }

    public LoadingLayout(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public LoadingLayout(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        View view = LayoutInflater.from(context).inflate(R.layout.layout_loading, this, true);

        mLoadingText = view.findViewById(R.id.dialog_loading_text);
        mLoadingImg = view.findViewById(R.id.dialog_loading_img);
        mLoadingAnimation = ((AnimationDrawable) mLoadingImg.getDrawable());
    }

    public void stopLoadingAnimation() {
        mLoadingAnimation.stop();
        mLoadingText.removeCallbacks(mLoadingAnimRunnable);
    }

    public void startLoadingAnimation() {
        mLoadingAnimation.start();
        mLoadingText.post(mLoadingAnimRunnable);
    }

    public void setLoadingStr(String loadingStr) {
        mLoadingStr = loadingStr;
    }
}
