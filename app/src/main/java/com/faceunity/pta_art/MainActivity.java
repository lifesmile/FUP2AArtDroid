package com.faceunity.pta_art;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.RelativeLayout;

import com.faceunity.pta_art.constant.Constant;
import com.faceunity.pta_art.core.AvatarHandle;
import com.faceunity.pta_art.core.FUPTARenderer;
import com.faceunity.pta_art.core.PTACore;
import com.faceunity.pta_art.entity.AvatarPTA;
import com.faceunity.pta_art.entity.DBHelper;
import com.faceunity.pta_art.fragment.ARFilterFragment;
import com.faceunity.pta_art.fragment.AvatarFragment;
import com.faceunity.pta_art.fragment.BaseFragment;
import com.faceunity.pta_art.fragment.EditFaceFragment;
import com.faceunity.pta_art.fragment.GroupPhotoFragment;
import com.faceunity.pta_art.fragment.HomeFragment;
import com.faceunity.pta_art.fragment.TakePhotoFragment;
import com.faceunity.pta_art.renderer.CameraRenderer;
import com.faceunity.pta_art.utils.DownLoadUtils;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

public class MainActivity extends AppCompatActivity implements
        CameraRenderer.OnCameraRendererStatusListener {
    private static final String TAG = MainActivity.class.getSimpleName();

    private RelativeLayout mMainLayout;
    private View mGroupPhotoRound;
    private GLSurfaceView mGLSurfaceView;
    private CameraRenderer mCameraRenderer;
    private FUPTARenderer mFUP2ARenderer;
    private PTACore mP2ACore;
    private AvatarHandle mAvatarHandle;

    private String mShowFragmentFlag;
    private HomeFragment mHomeFragment;
    private BaseFragment mBaseFragment;

    private boolean isCanController = true;
    private GestureDetectorCompat mGestureDetector;
    private ScaleGestureDetector mScaleGestureDetector;

    private DBHelper mDBHelper;
    private List<AvatarPTA> mAvatarP2As;
    private int mShowIndex;
    private AvatarPTA mShowAvatarP2A;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        mMainLayout = findViewById(R.id.main_layout);

        mGroupPhotoRound = findViewById(R.id.group_photo_round);
        mGLSurfaceView = findViewById(R.id.main_gl_surface);
        mGLSurfaceView.setEGLContextClientVersion(3);
        mCameraRenderer = new CameraRenderer(this, mGLSurfaceView);
        mCameraRenderer.setOnCameraRendererStatusListener(this);
        mGLSurfaceView.setRenderer(mCameraRenderer);
        mGLSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);

        mDBHelper = new DBHelper(this);
        mAvatarP2As = mDBHelper.getAllAvatarP2As();
        mShowAvatarP2A = mAvatarP2As.get(mShowIndex = 0);

        mFUP2ARenderer = new FUPTARenderer(this);
        mP2ACore = new PTACore(this, mFUP2ARenderer);
        mFUP2ARenderer.setFUCore(mP2ACore);
        mAvatarHandle = mP2ACore.createAvatarHandle();
        mAvatarHandle.setAvatar(getShowAvatarP2A(), new Runnable() {
            @Override
            public void run() {
                mHomeFragment.checkGuide();
            }
        });

        showHomeFragment();

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        final int screenWidth = metrics.widthPixels;
        final int screenHeight = metrics.heightPixels;
        mGestureDetector = new GestureDetectorCompat(this, new GestureDetector.SimpleOnGestureListener() {

            @Override
            public boolean onSingleTapUp(MotionEvent e) {
                if (HomeFragment.TAG.equals(mShowFragmentFlag)) {
                    return mHomeFragment.onSingleTapUp(e);
                } else if (mBaseFragment != null) {
                    return mBaseFragment.onSingleTapUp(e);
                }
                return false;
            }

            @Override
            public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
                if (touchMode != 1) {
                    touchMode = 1;
                    return false;
                }
                float rotDelta = -distanceX / screenWidth;
                float translateDelta = distanceY / screenHeight;
                mAvatarHandle.setRotDelta(rotDelta);
                mAvatarHandle.setTranslateDelta(translateDelta);
                return distanceX != 0 || translateDelta != 0;
            }
        });
        mScaleGestureDetector = new ScaleGestureDetector(this, new ScaleGestureDetector.SimpleOnScaleGestureListener() {
            @Override
            public boolean onScale(ScaleGestureDetector detector) {
                if (touchMode != 2) {
                    touchMode = 2;
                    return false;
                }
                float scale = detector.getScaleFactor() - 1;
                mAvatarHandle.setScaleDelta(scale);
                return scale != 0;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        mCameraRenderer.openCamera();
    }

    @Override
    public void onPause() {
        super.onPause();
        mCameraRenderer.releaseCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCameraRenderer.onDestroy();
    }

    private int touchMode = 0;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isCanController && (HomeFragment.TAG.equals(mShowFragmentFlag)
                || EditFaceFragment.TAG.equals(mShowFragmentFlag)
                || AvatarFragment.TAG.equals(mShowFragmentFlag))
        ) {
            if (event.getPointerCount() == 2) {
                mScaleGestureDetector.onTouchEvent(event);
            } else if (event.getPointerCount() == 1)
                mGestureDetector.onTouchEvent(event);
        }
        return super.onTouchEvent(event);
    }

    public FUPTARenderer getFUP2ARenderer() {
        return mFUP2ARenderer;
    }

    public PTACore getP2ACore() {
        return mP2ACore;
    }

    public AvatarHandle getAvatarHandle() {
        return mAvatarHandle;
    }

    public CameraRenderer getCameraRenderer() {
        return mCameraRenderer;
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        updateAvatarP2As();
        mAvatarHandle.setAvatar(getShowAvatarP2A());
        boolean isToHome = false;
        if (intent != null && intent.getExtras() != null) {
            isToHome = intent.getBooleanExtra("isToHome", false);
        }
        if (isToHome) {
            if (mBaseFragment instanceof GroupPhotoFragment) {
                mP2ACore.bind();
                mFUP2ARenderer.setFUCore(mP2ACore);
                ((GroupPhotoFragment) mBaseFragment).backToHome();
            }
        }
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        mFUP2ARenderer.onSurfaceCreated();
    }

    @Override
    public void onSurfaceChanged(GL10 gl, final int width, int height) {
    }

//    private volatile float[] expressionData = new float[56];

    @Override
    public int onDrawFrame(byte[] cameraNV21Byte, int cameraTextureId, int cameraWidth, int cameraHeight) {
        mCameraRenderer.refreshLandmarks(mP2ACore.getLandmarksData());
//        expressionData = mP2ACore.getExpressionData();
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                if (HomeFragment.TAG.equals(mShowFragmentFlag) && mHomeFragment != null) {
//                    mHomeFragment.setExpress(expressionData);
//                }
//            }
//        });
        return mFUP2ARenderer.onDrawFrame(cameraNV21Byte, cameraTextureId, cameraWidth, cameraHeight);
    }

    @Override
    public void onSurfaceDestroy() {
        mFUP2ARenderer.onSurfaceDestroyed();
    }

    @Override
    public void onCameraChange(int currentCameraType, int cameraOrientation) {
        mFUP2ARenderer.onCameraChange(currentCameraType, cameraOrientation);
    }

    @Override
    public void onBackPressed() {
        if (mBaseFragment != null) {
            mBaseFragment.onBackPressed();
            return;
        }
//        super.onBackPressed();
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        Runtime.getRuntime().gc();
    }

    public void showHomeFragment() {
        if (mCameraRenderer.getCurrentCameraType() == Camera.CameraInfo.CAMERA_FACING_BACK) {
            mCameraRenderer.changeCamera();
        }

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if (mBaseFragment != null) {
            transaction.remove(mBaseFragment);
            mBaseFragment = null;
        }
        if (mHomeFragment == null) {
            mHomeFragment = new HomeFragment();
            transaction.add(R.id.main_fragment_layout, mHomeFragment);
        } else {
            transaction.show(mHomeFragment);
        }
        mShowFragmentFlag = HomeFragment.TAG;
        transaction.commit();
        mAvatarHandle.resetAllMin();
    }

    public void showBaseFragment(String tag) {
        if (mCameraRenderer.getCurrentCameraType() == Camera.CameraInfo.CAMERA_FACING_BACK
                && !TakePhotoFragment.TAG.equals(tag) && !ARFilterFragment.TAG.equals(tag)) {
            mCameraRenderer.changeCamera();
        }
        FragmentManager manager = getSupportFragmentManager();
        FragmentTransaction transaction = manager.beginTransaction();
        if (HomeFragment.TAG.equals(mShowFragmentFlag) && mHomeFragment != null) {
            transaction.hide(mHomeFragment);
        }
        if (mBaseFragment != null) {
            if (AvatarFragment.TAG.equals(mShowFragmentFlag)) {
                transaction.hide(mBaseFragment);
            } else {
                transaction.remove(mBaseFragment);
            }
        }
        Fragment fragment = manager.findFragmentByTag(tag);
        if (fragment == null) {
            if (EditFaceFragment.TAG.equals(tag)) {
                mBaseFragment = new EditFaceFragment();
            } else if (ARFilterFragment.TAG.equals(tag)) {
                mBaseFragment = new ARFilterFragment();
            } else if (TakePhotoFragment.TAG.equals(tag)) {
                mBaseFragment = new TakePhotoFragment();
            } else if (GroupPhotoFragment.TAG.equals(tag)) {
                mBaseFragment = new GroupPhotoFragment();
            } else if (AvatarFragment.TAG.equals(tag)) {
                mBaseFragment = new AvatarFragment();
            }
            transaction.add(R.id.main_fragment_layout, mBaseFragment, tag);
        } else {
            transaction.show(mBaseFragment = (BaseFragment) fragment);
        }
        mShowFragmentFlag = tag;
        transaction.commit();
    }

    public List<AvatarPTA> getAvatarP2As() {
        return mAvatarP2As;
    }

    public void updateStyle(Runnable runnable) {
        updateAvatarP2As();

        mP2ACore.release();
        mP2ACore = new PTACore(this, mFUP2ARenderer);
        mFUP2ARenderer.setFUCore(mP2ACore);
        mAvatarHandle = mP2ACore.createAvatarHandle();
        mAvatarHandle.setAvatar(getShowAvatarP2A(), runnable);
    }

    public void updateAvatarP2As() {
        List<AvatarPTA> avatarP2AS = mDBHelper.getAllAvatarP2As();
        mAvatarP2As.clear();
        mAvatarP2As.addAll(avatarP2AS);
        setShowIndex(avatarP2AS.contains(mShowAvatarP2A) ? avatarP2AS.indexOf(mShowAvatarP2A) : 0);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(AvatarFragment.TAG);
        if (fragment instanceof AvatarFragment) {
            ((AvatarFragment) fragment).notifyDataSetChanged();
        }
    }

    public void setShowIndex(int showIndex) {
        this.mShowAvatarP2A = mAvatarP2As.get(mShowIndex = showIndex);
    }

    public int getShowIndex() {
        return mShowIndex;
    }

    public AvatarPTA getShowAvatarP2A() {
        return mShowAvatarP2A;
    }

    public void setShowAvatarP2A(AvatarPTA showAvatarP2A) {
        mShowIndex = mAvatarP2As.indexOf(mShowAvatarP2A = showAvatarP2A);
    }

    public void setGLSurfaceViewSize(boolean isMin) {
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) mGLSurfaceView.getLayoutParams();
        params.width = isMin ? getResources().getDimensionPixelSize(R.dimen.x480) : RelativeLayout.LayoutParams.MATCH_PARENT;
        params.height = isMin ? getResources().getDimensionPixelSize(R.dimen.x592) : RelativeLayout.LayoutParams.MATCH_PARENT;
        params.topMargin = isMin ? getResources().getDimensionPixelSize(R.dimen.x158) : 0;
        mGLSurfaceView.setLayoutParams(params);
        mGroupPhotoRound.setVisibility(isMin ? View.VISIBLE : View.GONE);
    }

    public void setCanController(boolean canController) {
        isCanController = canController;
    }

    public void initDebug(View clickView) {
        if (!Constant.is_debug) return;
        try {
            Log.e(TAG, "initDebug");
            Class aClass = Class.forName("com.faceunity.pta_art.debug.DebugLayout");
            if (aClass != null) {
                View debugLayout = null;
                Constructor[] cons = aClass.getDeclaredConstructors();
                for (Constructor con : cons) {
                    Class<?>[] parameterTypes = con.getParameterTypes();
                    Log.e(TAG, "initDebug " + parameterTypes.length);
                    if (parameterTypes.length == 1 && parameterTypes[0] == Context.class) {
                        Log.e(TAG, "initDebug " + parameterTypes[0]);
                        debugLayout = (View) con.newInstance(new Object[]{this});
                        break;
                    }
                }
                Log.e(TAG, "initDebug " + debugLayout);
                if (debugLayout != null) {
                    Method initData = aClass.getMethod("initData", new Class[]{MainActivity.class, FUPTARenderer.class, AvatarHandle.class, View.class});
                    initData.invoke(debugLayout, new Object[]{this, mFUP2ARenderer, mAvatarHandle, clickView});
                    mMainLayout.addView(debugLayout);
                }
            }
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

}
