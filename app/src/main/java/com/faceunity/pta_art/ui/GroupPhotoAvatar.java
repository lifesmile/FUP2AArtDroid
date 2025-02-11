package com.faceunity.pta_art.ui;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.faceunity.pta_art.R;
import com.faceunity.pta_art.constant.Constant;
import com.faceunity.pta_art.entity.AvatarPTA;
import com.faceunity.pta_art.entity.BundleRes;
import com.faceunity.pta_art.entity.DBHelper;
import com.faceunity.pta_art.entity.Scenes;

import java.util.Arrays;
import java.util.List;

/**
 * Created by tujh on 2018/12/18.
 */
public class GroupPhotoAvatar extends RelativeLayout {
    public static final String TAG = GroupPhotoAvatar.class.getSimpleName();

    private TextView mNextBtn;
    private RecyclerView mAvatarRecycler;
    private List<AvatarPTA> mAvatarP2As;
    private AvatarAdapter mAvatarAdapter;
    private ImageView mAvatarBackground;
    private TextView mAvatarPoint;
    private static final String str_select = "选择至多%d个模型";
    private static final String str_select_boy = "选择一个男模型";
    private static final String str_select_girl = "选择一个女模型";
    private static final String str_select_one = "选择一个模型";
    private static final String str_creating = "生成中...";
    private static final String str_create_complete = "完美";

    private boolean[] isSelectList;

    private Scenes mScenes;
    private int maxBoy, maxGirl;
    private int maxNum;

    private boolean isSelectEnable = true;

    public GroupPhotoAvatar(@NonNull Context context) {
        this(context, null);
    }

    public GroupPhotoAvatar(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public GroupPhotoAvatar(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        LayoutInflater.from(context).inflate(R.layout.layout_group_photo_avatar, this, true);
        mAvatarP2As = new DBHelper(context).getAllAvatarP2As();
        isSelectList = new boolean[mAvatarP2As.size()];
        findViewById(R.id.group_photo_avatar_back).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBackRunnable != null) {
                    mBackRunnable.run();
                }
            }
        });
        mNextBtn = findViewById(R.id.group_photo_avatar_next);
        mNextBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mNextRunnable != null) {
                    mNextRunnable.run();
                }
            }
        });

        mAvatarRecycler = findViewById(R.id.group_photo_avatar_recycler);
        mAvatarRecycler.setLayoutManager(new GridLayoutManager(context, 4, GridLayoutManager.VERTICAL, false));
        mAvatarRecycler.setAdapter(mAvatarAdapter = new AvatarAdapter());
        ((SimpleItemAnimator) mAvatarRecycler.getItemAnimator()).setSupportsChangeAnimations(false);

        mAvatarPoint = findViewById(R.id.group_photo_avatar_point);
        mAvatarBackground = findViewById(R.id.group_photo_avatar_background);
        mAvatarBackground.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mBackgroundRunnable != null) {
                    mBackgroundRunnable.run();
                }
            }
        });
    }

    class AvatarAdapter extends RecyclerView.Adapter<AvatarAdapter.HomeRecyclerHolder> {

        @Override
        public AvatarAdapter.HomeRecyclerHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new AvatarAdapter.HomeRecyclerHolder(LayoutInflater.from(getContext()).inflate(R.layout.layout_delete_bottom_item, parent, false));
        }

        @Override
        public void onBindViewHolder(final AvatarAdapter.HomeRecyclerHolder holder, int pos) {
            final int position = holder.getLayoutPosition();
            final AvatarPTA avatarP2A = mAvatarP2As.get(position);
            holder.img.setBackgroundResource(isSelectList[position] ? R.drawable.main_item_select : 0);
            if (avatarP2A.getOriginPhotoRes() > 0) {
                holder.img.setImageResource(avatarP2A.getOriginPhotoRes());
            } else {
                holder.img.setImageBitmap(BitmapFactory.decodeFile(avatarP2A.getOriginPhoto()));
            }
            if (Constant.style_art == Constant.style) {
                holder.itemView.setAlpha(!checkIsEnable(avatarP2A.getGender()) && !isSelectList[position] ? 0.5f : 1.0f);
            } else {
                holder.itemView.setAlpha(maxNum <= checkIsEnableNum() && !isSelectList[position] ? 0.5f : 1.0f);
            }

            holder.img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isSelectEnable) return;
                    if ((Constant.style_art == Constant.style
                            && !checkIsEnable(avatarP2A.getGender())
                            && !isSelectList[position])) return;
                    if ((Constant.style_new == Constant.style
                            && maxNum <= checkIsEnableNum()
                            && !isSelectList[position])) return;
                    isSelectList[position] = !isSelectList[position];
                    notifyDataSetChanged();
                    if (!isSelectList[position]) {
                        updateNextBtn(true);
                        updateAvatarPoint();
                    } else {
                        isSelectEnable = false;
                        mAvatarPoint.setText(str_creating);
                    }
                    if (mAvatarSelectListener != null) {
                        mAvatarSelectListener.onAvatarSelectListener(avatarP2A, isSelectList[position]);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return mAvatarP2As.size();
        }

        class HomeRecyclerHolder extends RecyclerView.ViewHolder {

            ImageView img;

            public HomeRecyclerHolder(View itemView) {
                super(itemView);
                img = itemView.findViewById(R.id.bottom_item_img);
            }
        }
    }

    private Runnable mBackRunnable;

    public void setBackRunnable(Runnable backRunnable) {
        this.mBackRunnable = backRunnable;
    }

    private Runnable mNextRunnable;

    public void setNextRunnable(Runnable nextRunnable) {
        mNextRunnable = nextRunnable;
    }

    private Runnable mBackgroundRunnable;

    public void setBackgroundRunnable(Runnable backgroundRunnable) {
        mBackgroundRunnable = backgroundRunnable;
    }

    private AvatarSelectListener mAvatarSelectListener;

    public void setAvatarSelectListener(AvatarSelectListener avatarSelectListener) {
        mAvatarSelectListener = avatarSelectListener;
    }

    public interface AvatarSelectListener {
        void onAvatarSelectListener(AvatarPTA avatar, boolean isSelect);
    }

    private boolean checkIsEnable(int gender) {
        return gender == AvatarPTA.gender_boy ? maxBoy > checkIsEnableNum(gender) : maxGirl > checkIsEnableNum(gender);
    }

    private int checkIsEnableNum(int gender) {
        int num = 0;
        for (int i = 0; i < mAvatarP2As.size(); i++) {
            if (isSelectList[i] && gender == mAvatarP2As.get(i).getGender()) {
                num++;
            }
        }
        return num;
    }

    private int checkIsEnableNum() {
        int num = 0;
        for (int i = 0; i < mAvatarP2As.size(); i++) {
            if (isSelectList[i]) {
                num++;
            }
        }
        return num;
    }

    public void updateAvatarPoint() {
        mAvatarPoint.post(new Runnable() {
            @Override
            public void run() {
                isSelectEnable = true;
                if (Constant.style == Constant.style_art) {
                    int boy = maxBoy - checkIsEnableNum(AvatarPTA.gender_boy);
                    int girl = maxGirl - checkIsEnableNum(AvatarPTA.gender_girl);
                    if (boy + girl > 1) {
                        mAvatarPoint.setText(String.format(str_select, boy + girl));
                    } else if (boy > 0) {
                        mAvatarPoint.setText(str_select_boy);
                    } else if (girl > 0) {
                        mAvatarPoint.setText(str_select_girl);
                    } else {
                        mAvatarPoint.setText(str_create_complete);
                    }
                } else {
                    int num = maxNum - checkIsEnableNum();
                    if (num > 1) {
                        mAvatarPoint.setText(String.format(str_select, num));
                    } else if (num > 0) {
                        mAvatarPoint.setText(str_select_one);
                    } else {
                        mAvatarPoint.setText(str_create_complete);
                    }
                }
            }
        });
    }

    public void setScenes(Scenes scenes) {
        Arrays.fill(isSelectList, false);
        mScenes = scenes;
        if (Constant.style == Constant.style_art) {
            maxBoy = maxGirl = 0;
            for (BundleRes b : mScenes.bundles) {
                if (b.gender == AvatarPTA.gender_boy) {
                    maxBoy++;
                } else {
                    maxGirl++;
                }
            }
        } else {
            maxNum = 0;
            for (BundleRes b : mScenes.bundles) {
                maxNum++;
            }
        }
        mAvatarAdapter.notifyDataSetChanged();
        updateNextBtn(true);
        updateAvatarPoint();
    }

    public void updateNextBtn(final boolean isEnabled) {
        mNextBtn.post(new Runnable() {
            @Override
            public void run() {
                if (Constant.style == Constant.style_art) {
                    mNextBtn.setEnabled(isEnabled && !checkIsEnable(AvatarPTA.gender_boy) && !checkIsEnable(AvatarPTA.gender_girl));
                } else {
                    mNextBtn.setEnabled(isEnabled && maxNum <= checkIsEnableNum());
                }
            }
        });
    }

    public boolean nextEnable() {
        return mNextBtn.isEnabled();
    }

    public boolean isCreateAnimate() {
        return mAvatarPoint.getText().toString().equals(str_create_complete)
                || mAvatarPoint.getText().toString().equals(str_creating);
    }
}
