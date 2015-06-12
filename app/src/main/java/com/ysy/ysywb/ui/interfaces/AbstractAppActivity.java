package com.ysy.ysywb.ui.interfaces;

import android.support.v4.app.FragmentActivity;

import com.ysy.ysywb.support.asyncdrawable.TimeLineBitmapDownloader;

/**
 * Created by ggec5486 on 2015/6/11.
 */
public class AbstractAppActivity extends FragmentActivity {

    protected int theme = 0;

    public TimeLineBitmapDownloader getBitmapDownloader() {
        return TimeLineBitmapDownloader.getInstance();
    }
}
