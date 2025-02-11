package com.faceunity.pta_art.web;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.faceunity.pta_art.constant.Constant;
import com.faceunity.pta_art.utils.BitmapUtil;
import com.faceunity.pta_art.utils.FileUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * Created by tujh on 2018/3/8.
 */

public class OkHttpUtils {
    private static final String TAG = OkHttpUtils.class.getSimpleName();
    public static final String PEM_PATH = "pta_ptoa.pem";
    public static final String PEM_API2_PATH = "pta_api2.pem";

    private volatile static OkHttpUtils sOkHttpUtils;

    private OkHttpClient mOkHttpClient = null;
    private OkHttpClient mOkHttpClient2 = null;

    public static OkHttpClient initOkHttpClient(Context context) {
        SSLSocketFactory sslSocketFactory = null;
        try {
//            InputStream p12 = context.getAssets().open(P12_PATH);
//            byte[] p12Bytes = new byte[p12.available()];
//            p12.read(p12Bytes);
//            p12.close();


            //p2a服务器需要的ca，手动传避免部分机型ca不全
            InputStream ca = context.getAssets().open(PEM_PATH);
            byte[] caBytes = new byte[ca.available()];
            ca.read(caBytes);
            ca.close();

            TrustManagerFactory tmf = OkHttpUtils.getTrustManagerFactory(caBytes);
//            sslSocketFactory = new CustomSslSocketFactory(OkHttpUtils.getKeyManagerFactory(p12Bytes).getKeyManagers(),
//                    tmf == null ? null : tmf.getTrustManagers());
            sslSocketFactory = new CustomSslSocketFactory(null,
                    tmf == null ? null : tmf.getTrustManagers());
        } catch (Exception e) {
        }

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(60000L, TimeUnit.MILLISECONDS)
                .writeTimeout(60000L, TimeUnit.MILLISECONDS)
                .readTimeout(60000L, TimeUnit.MILLISECONDS);
        OkHttpClient okHttpClient;
        if (sslSocketFactory != null) {
            okHttpClient = builder.sslSocketFactory(sslSocketFactory).build();
        } else {
            okHttpClient = builder.build();
        }
        return okHttpClient;
    }

    public static OkHttpClient initOkHttpClient2(Context context) {
        SSLSocketFactory sslSocketFactory = null;
        try {

            //p2a服务器需要的ca，手动传避免部分机型ca不全
            InputStream ca = context.getAssets().open(PEM_API2_PATH);
            byte[] caBytes = new byte[ca.available()];
            ca.read(caBytes);
            ca.close();

            TrustManagerFactory tmf = OkHttpUtils.getTrustManagerFactory(caBytes);
            sslSocketFactory = new CustomSslSocketFactory(null,
                    tmf == null ? null : tmf.getTrustManagers());
        } catch (Exception e) {
        }

        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(60000L, TimeUnit.MILLISECONDS)
                .writeTimeout(60000L, TimeUnit.MILLISECONDS)
                .readTimeout(60000L, TimeUnit.MILLISECONDS);
        OkHttpClient okHttpClient;
        if (sslSocketFactory != null) {
            okHttpClient = builder.sslSocketFactory(sslSocketFactory).build();
        } else {
            okHttpClient = builder.build();
        }
        return okHttpClient;
    }

    public static OkHttpUtils initOkHttpUtils(OkHttpClient okHttpClient, OkHttpClient okHttpClient2) {
        if (sOkHttpUtils == null) {
            synchronized (OkHttpUtils.class) {
                if (sOkHttpUtils == null) {
                    sOkHttpUtils = new OkHttpUtils(okHttpClient, okHttpClient2);
                }
            }
        }
        return sOkHttpUtils;
    }

    private OkHttpUtils(OkHttpClient okHttpClient, OkHttpClient okHttpClient2) {
        if (okHttpClient == null) {
            mOkHttpClient = new OkHttpClient();
            mOkHttpClient2 = new OkHttpClient();
        } else {
            mOkHttpClient = okHttpClient;
            mOkHttpClient2 = okHttpClient2;
        }
    }

    public static OkHttpUtils getInstance() {
        return initOkHttpUtils(null, null);
    }

    public OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }

    public OkHttpClient getOkHttpClient2() {
        return mOkHttpClient2;
    }

    public static KeyManagerFactory getKeyManagerFactory(byte[] p12) {
        KeyManagerFactory kmf = null;
        try {
            KeyStore p12KeyStore = KeyStore.getInstance("PKCS12");
            InputStream in = new ByteArrayInputStream(p12);
            p12KeyStore.load(in, "".toCharArray());
            kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            kmf.init(p12KeyStore, "".toCharArray());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return kmf;
    }

    static class TrustAllCerts implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }

    /**
     * 请求服务器，验证验证码
     */
    public static void createAvatarRequest(final Context context, String code, final Callback callback) {
        String imei = "";
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED) {
            imei = ((TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE)).getDeviceId();
        }
        RequestBody requestBody = RequestBody.create(
                MediaType.parse("application/x-www-form-urlencoded;charset=utf-8"),
                String.format("code=%s&device=%s", code, imei)
        );
        Log.e(TAG, "response " + String.format("code=%s&device=%s", code, imei));
        getInstance().getOkHttpClient().newCall(new Request.Builder().url(Constant.web_url_check).post(requestBody).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onResponse(call, response);
            }
        });
    }

    /**
     * 请求服务器，处理图片并获得处理后的数据
     *
     * @param callback
     */
    public static void getAvatarToken(final Callback callback) {
        String url = Constant.web_url_get_token;
        Log.i(TAG, "getAvatarToken url " + url);
        getInstance().getOkHttpClient2().newCall(new Request.Builder().url(url).get().build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onResponse(call, response);
            }
        });
    }

    /**
     * 请求服务器，上传图片
     *
     * @param uploadFile 图片路径
     * @param gender     性别 0：男 1：女
     * @param callback
     */
    public static void updatePicRequest(String token, final String uploadFile, int gender, final Callback callback) {
        String url = Constant.web_url_create_upload_image + "?access_token=" + token;
        Log.i(TAG, "createAvatarRequest url " + url);
        Log.i(TAG, "createAvatarRequest uploadFile " + uploadFile);
        Bitmap bitmap = BitmapUtil.loadBitmap(uploadFile);
        String tmp = Constant.filePath + "tmp.png";
        FileUtil.saveBitmapToFile(tmp, bitmap);
        final File reallyUploadFile = new File(tmp);
        RequestBody requestBody = (new okhttp3.MultipartBody.Builder())
                .setType(MultipartBody.FORM)
                .addFormDataPart("gender", String.valueOf(gender))
                .addFormDataPart("version", Constant.style == Constant.style_new ? Constant.pta_client_version_new : Constant.pta_client_version_art)
                .addFormDataPart("image", "filename", RequestBody.create(MediaType.parse("image/png"), reallyUploadFile))
                .build();
        getInstance().getOkHttpClient().newCall(new Request.Builder().url(url).post(requestBody).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!reallyUploadFile.equals(uploadFile)) {
                    reallyUploadFile.delete();
                }
                callback.onResponse(call, response);
            }
        });
    }

    /**
     * 请求服务器，上传图片
     *
     * @param uploadFile 图片路径
     * @param gender     性别 0：男 1：女
     * @param callback
     */
    public static void updatePicRequest(String token, final String uploadFile, int gender, final Callback callback, ProgressRequestBody.UploadProgressListener uploadProgressListener) {
        String url = Constant.web_url_create_upload_image + "?access_token=" + token;
        Log.i(TAG, "createAvatarRequest url " + url);
        Log.i(TAG, "createAvatarRequest uploadFile " + uploadFile);
        Bitmap bitmap = BitmapUtil.loadBitmap(uploadFile);
        String tmp = Constant.filePath + "tmp.png";
        FileUtil.saveBitmapToFile(tmp, bitmap);
        final File reallyUploadFile = new File(tmp);
        RequestBody requestBody = (new okhttp3.MultipartBody.Builder())
                .setType(MultipartBody.FORM)
                .addFormDataPart("gender", String.valueOf(gender))
                .addFormDataPart("version", Constant.style == Constant.style_new ? Constant.pta_client_version_new : Constant.pta_client_version_art)
                .addFormDataPart("image", "filename", RequestBody.create(MediaType.parse("image/png"), reallyUploadFile))
                .build();
        if (uploadProgressListener != null) {
            requestBody = new ProgressRequestBody(requestBody, uploadProgressListener);
        }
        getInstance().getOkHttpClient().newCall(new Request.Builder().url(url).post(requestBody).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (!reallyUploadFile.equals(uploadFile)) {
                    reallyUploadFile.delete();
                }
                callback.onResponse(call, response);
            }
        });
    }

    /**
     * 请求服务器，下载生成数据
     *
     * @param taskId   任务ID
     * @param callback
     */
    public static void downloadAvatarRequest(String token, String taskId, final Callback callback) {
        String url = Constant.web_url_create_download + "?access_token=" + token;
        Log.i(TAG, "createAvatarRequest url " + url);
        RequestBody requestBody = (new okhttp3.MultipartBody.Builder())
                .setType(MultipartBody.FORM)
                .addFormDataPart("taskid", taskId)
                .build();
        getInstance().getOkHttpClient().newCall(new Request.Builder().url(url).post(requestBody).build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                callback.onFailure(call, e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                callback.onResponse(call, response);
            }
        });
    }

    /**
     * 取消所有网络请求
     */
    public static void cancelAll() {
        for (Call call : getInstance().getOkHttpClient().dispatcher().queuedCalls()) {
            call.cancel();
        }
        for (Call call : getInstance().getOkHttpClient().dispatcher().runningCalls()) {
            call.cancel();
        }
    }

    /**
     * 这里配置初始化ca，拿到trustmanager
     */
    public static TrustManagerFactory getTrustManagerFactory(byte[] caBytes) {
        if (caBytes == null) return null;
        TrustManagerFactory tmf = null;
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            InputStream caInput = new ByteArrayInputStream(caBytes);
            Certificate ca = null;
            try {
                ca = cf.generateCertificate(caInput);
                System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                caInput.close();
            }
            // Create a KeyStore containing our trusted CAs
            String keyStoreType = KeyStore.getDefaultType();
            KeyStore keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", ca);
            // Create a TrustManager that trusts the CAs in our KeyStore
            String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
            tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
            tmf.init(keyStore);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmf;
    }
}
