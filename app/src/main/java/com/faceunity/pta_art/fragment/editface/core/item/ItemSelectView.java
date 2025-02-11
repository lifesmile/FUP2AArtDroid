package com.faceunity.pta_art.fragment.editface.core.item;

import android.content.Context;
import android.graphics.Rect;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.util.AttributeSet;
import android.view.View;

import com.faceunity.pta_art.R;
import com.faceunity.pta_art.entity.FURes;

import java.util.List;

/**
 * Created by tujh on 2019/1/9.
 */
public class ItemSelectView extends RecyclerView {
    public static final String TAG = ItemSelectView.class.getSimpleName();

    private static final int spanCount = 4;

    private ItemAdapter mItemAdapter;
    private GridLayoutManager mGridLayoutManager;
    private ItemAdapter.ItemSelectListener mItemSelectListener;

    private int size;
    private int mDefaultSelectItem;

    public ItemSelectView(@NonNull Context context) {
        this(context, null);
    }

    public ItemSelectView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ItemSelectView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public <T extends FURes> void init(final List<T> itemList, int defaultSelectItem) {
        size = itemList.size();
        mItemAdapter = new ItemAdapter(getContext()) {
            @Override
            public int getRes(int pos) {
                return itemList.get(pos).resId;
            }

            @Override
            public int getSize() {
                return size;
            }
        };
        mItemAdapter.setSelectPosition(this.mDefaultSelectItem = defaultSelectItem);

        init();
    }

    private void init() {
        setLayoutManager(mGridLayoutManager = new GridLayoutManager(getContext(), spanCount, GridLayoutManager.VERTICAL, false));
        setAdapter(mItemAdapter);
        final int wL = getResources().getDimensionPixelSize(R.dimen.x8);
        final int hL = getResources().getDimensionPixelSize(R.dimen.x17);
        final int topNormalL = getResources().getDimensionPixelSize(R.dimen.x14);
        addItemDecoration(new ItemDecoration() {
            @Override
            public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
                int index = parent.getChildAdapterPosition(view);
                int left = index % spanCount == 0 ? wL : 0;
                int right = index % spanCount == spanCount - 1 ? wL : 0;
                int top = index < spanCount ? hL : topNormalL;
                int bottom = index < size / spanCount * spanCount ? 0 : hL;
                outRect.set(left, top, right, bottom);
            }
        });

        ((SimpleItemAnimator) getItemAnimator()).setSupportsChangeAnimations(false);
        mItemAdapter.setItemSelectListener(new ItemAdapter.ItemSelectListener() {
            @Override
            public boolean itemSelectListener(int lastPos, int position) {
                scrollToPosition(position);
                return mItemSelectListener != null && mItemSelectListener.itemSelectListener(lastPos, position);
            }
        });

        scrollToPosition(mDefaultSelectItem);
    }

    public void scrollToPosition(final int pos) {
        post(new Runnable() {
            @Override
            public void run() {
                final int topNormalL = getResources().getDimensionPixelSize(R.dimen.x14);
                final int itemW = getResources().getDimensionPixelOffset(R.dimen.x156);
                final int first = mGridLayoutManager.findFirstVisibleItemPosition();
                if (first < 0) return;
                int dy = (int) ((0.5 + pos / spanCount) * (itemW + topNormalL) - getHeight() / 2
                        - (first / spanCount * (itemW + topNormalL) - mGridLayoutManager.findViewByPosition(first).getTop()));
                smoothScrollBy(0, dy);
            }
        });
    }

    public void setSelectPosition(int selectPos) {
        mItemAdapter.setSelectPosition(selectPos);
    }

    public void setItemControllerListener(ItemAdapter.ItemSelectListener itemSelectListener) {
        mItemSelectListener = itemSelectListener;
    }

    public void setItem(int position) {
        mItemAdapter.setSelectPosition(position);
    }

    public int getSelectItem() {
        return mItemAdapter.mSelectPosition;
    }
}
