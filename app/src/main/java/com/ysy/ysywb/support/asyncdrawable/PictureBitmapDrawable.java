package com.ysy.ysywb.support.asyncdrawable;

import android.graphics.drawable.ColorDrawable;

import com.ysy.ysywb.R;
import com.ysy.ysywb.support.utils.ThemeUtility;

import java.lang.ref.WeakReference;

/**
 * Created by ggec5486 on 2015/7/23.
 */
public class PictureBitmapDrawable extends ColorDrawable {

    private final WeakReference<IPictureWorker> bitmapDownloaderTaskReference;

    public PictureBitmapDrawable(IPictureWorker bitmapDownloaderTask) {
        super(ThemeUtility.getColor(R.attr.listview_pic_bg));
        bitmapDownloaderTaskReference =
                new WeakReference<IPictureWorker>(bitmapDownloaderTask);
    }

    public IPictureWorker getBitmapDownloaderTask() {
        return bitmapDownloaderTaskReference.get();
    }
}
