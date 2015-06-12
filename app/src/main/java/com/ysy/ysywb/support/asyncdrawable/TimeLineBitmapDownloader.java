package com.ysy.ysywb.support.asyncdrawable;

import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import com.ysy.ysywb.R;
import com.ysy.ysywb.bean.UserBean;

/**
 * Created by ggec5486 on 2015/6/11.
 */
public class TimeLineBitmapDownloader {

    private int defaultPictureResId;

    private Handler handler;

    static volatile boolean pauseDownloadWork = false;
    static final Object pauseDownloadWorkLock = new Object();

    static volatile boolean pauseReadWork = false;
    static final Object pauseReadWorkLock = new Object();

    private static final Object lock = new Object();

    private static TimeLineBitmapDownloader instance;

    private TimeLineBitmapDownloader(Handler handler) {
        this.handler = handler;
        //this.defaultPictureResId = ThemeUtility.getResourceId(R.attr.listview_pic_bg);
    }

    public static TimeLineBitmapDownloader getInstance() {
        synchronized (lock) {
            if (instance == null) {
                instance = new TimeLineBitmapDownloader(new Handler(Looper.getMainLooper()));
            }
        }
        return instance;
    }


    public void downloadAvatar(ImageView view, UserBean user, boolean isFling) {

        if (user == null) {
            view.setImageResource(R.drawable.ic_ysywb);
            return;
        }
        view.setImageResource(R.drawable.ic_ysywb);
    }
}
