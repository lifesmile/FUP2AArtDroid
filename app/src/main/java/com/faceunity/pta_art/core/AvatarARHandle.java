package com.faceunity.pta_art.core;

import android.hardware.Camera;
import android.os.Message;
import android.util.Log;

import com.faceunity.pta_art.constant.FilePathFactory;
import com.faceunity.pta_art.core.base.BaseCore;
import com.faceunity.pta_art.core.base.BasePTAHandle;
import com.faceunity.pta_art.core.base.FUItem;
import com.faceunity.pta_art.core.base.FUItemHandler;
import com.faceunity.pta_art.entity.AvatarPTA;
import com.faceunity.wrapper.faceunity;

import java.util.Arrays;

/**
 * AR Controller
 * Created by tujh on 2018/12/17.
 */
public class AvatarARHandle extends BasePTAHandle {
    private static final String TAG = AvatarARHandle.class.getSimpleName();
    final int FUItemHandler_what_filter = FUItemHandler_what + 2;

    public final FUItem filterItem = new FUItem();
    public final FUItem headItem = new FUItem();
    public final FUItem hairItem = new FUItem();
    public final FUItem glassItem = new FUItem();
    public final FUItem beardItem = new FUItem();
    public final FUItem eyebrowItem = new FUItem();
    public final FUItem eyelashItem = new FUItem();
    public final FUItem hatItem = new FUItem();

    public AvatarARHandle(BaseCore baseCore, FUItemHandler FUItemHandler) {
        super(baseCore, FUItemHandler);
        mFUItemHandler.loadFUItem(FUItemHandler_what_controller, new FUItemHandler.LoadFUItemListener(FilePathFactory.bundleController()) {

            @Override
            public void onLoadComplete(FUItem fuItem) {
                controllerItem = fuItem.handle;
                mBaseCore.queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        faceunity.fuItemSetParam(controllerItem, "enter_ar_mode", 1);
                    }
                });
            }
        });
    }

    public void setARAvatar(final AvatarPTA avatar) {
        mFUItemHandler.removeMessages(FUItemHandler_what);
        Message msg = Message.obtain(mFUItemHandler, new Runnable() {
            @Override
            public void run() {
                loadItem(headItem, avatar.getHeadFile());
                loadItem(hairItem, avatar.getHairFile());
                loadItem(glassItem, avatar.getGlassesFile());
                loadItem(beardItem, avatar.getBeardFile());
                loadItem(eyebrowItem, avatar.getEyebrowFile());
                loadItem(eyelashItem, avatar.getEyelashFile());
                loadItem(hatItem, avatar.getHatFile());
                commitItem(avatar);
            }
        });
        msg.what = FUItemHandler_what;
        mFUItemHandler.sendMessage(msg);
    }

    public void setFilter(final String filter) {
        if (!filter.equals(filterItem.name)) {
            mFUItemHandler.removeMessages(FUItemHandler_what_filter);
            mFUItemHandler.loadFUItem(FUItemHandler_what_filter, new FUItemHandler.LoadFUItemListener(filter) {

                @Override
                public void onLoadComplete(FUItem fuItem) {
                    filterItem.handle = fuItem.handle;
                    filterItem.name = fuItem.name;
                }
            });
        }
    }

    @Override
    protected void bindAll() {
        if (controllerItem > 0)
            mBaseCore.queueEvent(new Runnable() {
                @Override
                public void run() {
                    int[] items = new int[]{headItem.handle, hairItem.handle, glassItem.handle, beardItem.handle, eyebrowItem.handle, eyelashItem.handle, hatItem.handle};
                    Log.i(TAG, "bundle avatarBindItem controlItem " + controllerItem + " bindAll " + Arrays.toString(items));
                    faceunity.fuBindItems(controllerItem, items);
                    setAvatarColor();
                }
            });
    }

    @Override
    protected void unBindAll() {
        if (controllerItem > 0)
            mBaseCore.queueEvent(new Runnable() {
                @Override
                public void run() {
                    int[] items = new int[]{headItem.handle, hairItem.handle, glassItem.handle, beardItem.handle, eyebrowItem.handle, eyelashItem.handle, hatItem.handle};
                    Log.i(TAG, "bundle avatarBindItem controlItem " + controllerItem + " unBindAll " + Arrays.toString(items));
                    faceunity.fuUnBindItems(controllerItem, items);
                }
            });
    }

    @Override
    public void release() {
        unBindAll();
        mBaseCore.queueEvent(mBaseCore.destroyItem(filterItem.handle));
        mBaseCore.queueEvent(mBaseCore.destroyItem(headItem.handle));
        mBaseCore.queueEvent(mBaseCore.destroyItem(hairItem.handle));
        mBaseCore.queueEvent(mBaseCore.destroyItem(glassItem.handle));
        mBaseCore.queueEvent(mBaseCore.destroyItem(beardItem.handle));
        mBaseCore.queueEvent(mBaseCore.destroyItem(eyebrowItem.handle));
        mBaseCore.queueEvent(mBaseCore.destroyItem(eyelashItem.handle));
        mBaseCore.queueEvent(mBaseCore.destroyItem(hatItem.handle));
        mBaseCore.queueEvent(mBaseCore.destroyItem(controllerItem));
        mBaseCore.queueEvent(new Runnable() {
            @Override
            public void run() {
                filterItem.clear();
                headItem.clear();
                hairItem.clear();
                glassItem.clear();
                beardItem.clear();
                eyebrowItem.clear();
                eyelashItem.clear();
                hatItem.clear();
                controllerItem = 0;
            }
        });
    }

    public void onCameraChange(final int currentCameraType, final int inputImageOrientation) {
        mBaseCore.queueEvent(new Runnable() {
            @Override
            public void run() {
                faceunity.fuItemSetParam(controllerItem, "is3DFlipH", Camera.CameraInfo.CAMERA_FACING_BACK == currentCameraType ? 1 : 0);
                faceunity.fuItemSetParam(controllerItem, "arMode", (Camera.CameraInfo.CAMERA_FACING_BACK == currentCameraType ? inputImageOrientation : (360 - inputImageOrientation)) / 90);
            }
        });
    }
}
