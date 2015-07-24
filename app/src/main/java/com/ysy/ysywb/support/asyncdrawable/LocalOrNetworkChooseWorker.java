package com.ysy.ysywb.support.asyncdrawable;

import android.widget.ImageView;

import com.ysy.ysywb.support.file.FileLocationMethod;

import java.lang.ref.WeakReference;

/**
 * Created by ggec5486 on 2015/7/23.
 */
public class LocalOrNetworkChooseWorker extends AbstractWorker<String, Integer, Boolean> {

    private String data = "";

    private boolean isMultiPictures = false;

    private WeakReference<ImageView> viewWeakReference;

    private FileLocationMethod method;

    //private IWeiciyuanDrawable IWeiciyuanDrawable;


    public LocalOrNetworkChooseWorker(ImageView view, String url, FileLocationMethod method,
                                      boolean isMultiPictures) {

        this.viewWeakReference = new WeakReference<ImageView>(view);
        this.data = url;
        this.method = method;
        this.isMultiPictures = isMultiPictures;
    }

    @Override
    public String getUrl() {
        return data;
    }

    @Override
    protected Boolean doInBackground(String... url) {
//        String path = FileManager.getFilePathFromUrl(data, method);
//        return ImageUtility.isThisBitmapCanRead(path) && TaskCache.isThisUrlTaskFinished(data);
        return null;
    }
}
