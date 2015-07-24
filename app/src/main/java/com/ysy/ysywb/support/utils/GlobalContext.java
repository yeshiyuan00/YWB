package com.ysy.ysywb.support.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.LruCache;

import com.ysy.ysywb.bean.AccountBean;

/**
 * User: Jiang Qi
 * Date: 12-7-27
 */
public final class GlobalContext extends Application {

    //singleton
    private static GlobalContext globalContext = null;

    //image size
    private Activity activity = null;
    private Activity currentRunningActivity = null;

    private DisplayMetrics displayMetrics = null;
    private Handler handler = new Handler();

    //image memory cache
    private LruCache<String, Bitmap> appBitmapCache = null;

    //current account info
    private AccountBean accountBean = null;

    @Override
    public void onCreate() {
        super.onCreate();
        globalContext = this;
    }

    public static GlobalContext getInstance() {
        return globalContext;
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }


    public synchronized LruCache<String, Bitmap> getBitmapCache() {
        if (appBitmapCache == null) {
            buildCache();
        }
        return appBitmapCache;
    }

    private void buildCache() {
        int memClass = ((ActivityManager) getSystemService(
                Context.ACTIVITY_SERVICE)).getMemoryClass();

        int cacheSize = Math.max(1024 * 1024 * 8, 1024 * 1024 * memClass / 5);

        appBitmapCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {

                return bitmap.getByteCount();
            }
        };
    }
}

