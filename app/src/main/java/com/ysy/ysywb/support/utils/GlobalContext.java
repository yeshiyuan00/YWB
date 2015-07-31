package com.ysy.ysywb.support.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.LruCache;
import android.view.Display;

import com.crashlytics.android.Crashlytics;
import com.ysy.ysywb.bean.AccountBean;
import com.ysy.ysywb.support.crashmanager.CrashManager;
import com.ysy.ysywb.support.crashmanager.CrashManagerConstants;
import com.ysy.ysywb.support.database.AccountDBTask;
import com.ysy.ysywb.support.settinghelper.SettingUtility;

import java.util.List;

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
        buildCache();
        CrashManagerConstants.loadFromContext(this);
        CrashManager.registerHandler();
        if (Utility.isCertificateFingerprintCorrect(this)) {
           Crashlytics.start(this);
        }
        //registerActivityLifecycleCallbacks(new AppActivityLifecycleCallbacks());
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

    public Activity getCurrentRunningActivity() {
        return currentRunningActivity;
    }

    public void setCurrentRunningActivity(Activity currentRunningActivity) {
        this.currentRunningActivity = currentRunningActivity;
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

    public String getCurrentAccountName() {

        return getAccountBean().getUsernick();
    }

    public AccountBean getAccountBean() {
        if (accountBean == null) {
            String id = SettingUtility.getDefaultAccountId();
            if (!TextUtils.isEmpty(id)) {
                accountBean = AccountDBTask.getAccount(id);
            } else {
                List<AccountBean> accountList = AccountDBTask.getAccountList();
                if (accountList != null && accountList.size() > 0) {
                    accountBean = accountList.get(0);
                }
            }
        }

        return accountBean;
    }

    public Handler getUIHandler() {
        return handler;
    }

    public DisplayMetrics getDisplayMetrics() {
        if (displayMetrics != null) {
            return displayMetrics;
        } else {
            Activity a = getActivity();
            if (a != null) {
                Display display = getActivity().getWindowManager().getDefaultDisplay();
                DisplayMetrics metrics = new DisplayMetrics();
                display.getMetrics(metrics);
                this.displayMetrics = metrics;
                return metrics;
            } else {
                //default screen is 800x480
                DisplayMetrics metrics = new DisplayMetrics();
                metrics.widthPixels = 480;
                metrics.heightPixels = 800;
                return metrics;
            }
        }
    }
}

