package com.ysy.ysywb.ui.interfaces;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.ysy.ysywb.support.asyncdrawable.TimeLineBitmapDownloader;
import com.ysy.ysywb.support.utils.GlobalContext;

/**
 * Created by ggec5486 on 2015/6/11.
 */
public class AbstractAppActivity extends FragmentActivity {

    protected int theme = 0;

    public TimeLineBitmapDownloader getBitmapDownloader() {
        return TimeLineBitmapDownloader.getInstance();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        GlobalContext.getInstance().setActivity(this);
    }
}
