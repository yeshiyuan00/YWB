package com.ysy.ysywb.support.asyncdrawable;

import android.graphics.drawable.ColorDrawable;
import android.widget.ImageView;

import com.ysy.ysywb.support.file.FileLocationMethod;
import com.ysy.ysywb.support.file.FileManager;
import com.ysy.ysywb.support.imageutility.ImageUtility;

import java.lang.ref.WeakReference;

/**
 * Created by ggec5486 on 2015/7/23.
 */
public class LocalOrNetworkChooseWorker extends AbstractWorker<String, Integer, Boolean> {

    private String data = "";

    private boolean isMultiPictures = false;

    private WeakReference<ImageView> viewWeakReference;

    private FileLocationMethod method;

    private IWeiciyuanDrawable IWeiciyuanDrawable;


    @Override
    public String getUrl() {
        return data;
    }

    public LocalOrNetworkChooseWorker(ImageView view, String url, FileLocationMethod method,
                                      boolean isMultiPictures) {

        this.viewWeakReference = new WeakReference<ImageView>(view);
        this.data = url;
        this.method = method;
        this.isMultiPictures = isMultiPictures;
    }

    public LocalOrNetworkChooseWorker(IWeiciyuanDrawable view, String url,
                                      FileLocationMethod method,
                                      boolean isMultiPictures) {

        this(view.getImageView(), url, method, false);
        this.IWeiciyuanDrawable = view;
        this.isMultiPictures = isMultiPictures;
    }


    @Override
    protected Boolean doInBackground(String... url) {
        String path = FileManager.getFilePathFromUrl(data, method);
        return ImageUtility.isThisBitmapCanRead(path) && TaskCache.isThisUrlTaskFinished(data);
    }

    @Override
    protected void onCancelled(Boolean aBoolean) {
        super.onCancelled(aBoolean);
        ImageView imageView = viewWeakReference.get();
        if (!isMySelf(imageView)) {
            return;
        }

        imageView.setImageDrawable(
                new ColorDrawable(DebugColor.CHOOSE_CANCEL));
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);

        ImageView imageView = viewWeakReference.get();
        if (!isMySelf(imageView)) {
            return;
        }

        if (result) {
            LocalWorker newTask = null;

            if (IWeiciyuanDrawable != null) {
                newTask = new LocalWorker(IWeiciyuanDrawable, getUrl(), method,
                        isMultiPictures);
                PictureBitmapDrawable downloadedDrawable = new PictureBitmapDrawable(newTask);
                IWeiciyuanDrawable.setImageDrawable(downloadedDrawable);

            } else {
                newTask = new LocalWorker(imageView, getUrl(), method, isMultiPictures);
                PictureBitmapDrawable downloadedDrawable = new PictureBitmapDrawable(newTask);
                imageView.setImageDrawable(downloadedDrawable);
                // imageView.setImageResource(R.drawable.ic_ysywb);
            }

            newTask.executeOnIO();
        } else {

            ReadWorker newTask = null;

            if (IWeiciyuanDrawable != null) {
                newTask = new ReadWorker(IWeiciyuanDrawable, getUrl(), method,
                        isMultiPictures);
                PictureBitmapDrawable downloadedDrawable = new PictureBitmapDrawable(newTask);
                IWeiciyuanDrawable.setImageDrawable(downloadedDrawable);
            } else {
                newTask = new ReadWorker(imageView, getUrl(), method, isMultiPictures);
                PictureBitmapDrawable downloadedDrawable = new PictureBitmapDrawable(newTask);
                imageView.setImageDrawable(downloadedDrawable);
            }

            newTask.executeOnWaitNetwork();
        }

    }

}
