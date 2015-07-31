package com.ysy.ysywb.support.asyncdrawable;

import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.util.DisplayMetrics;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.ysy.ysywb.R;
import com.ysy.ysywb.support.file.FileLocationMethod;
import com.ysy.ysywb.support.file.FileManager;
import com.ysy.ysywb.support.imageutility.ImageUtility;
import com.ysy.ysywb.support.lib.LayerEnablingAnimatorListener;
import com.ysy.ysywb.support.utils.GlobalContext;
import com.ysy.ysywb.support.utils.Utility;

import java.lang.ref.WeakReference;

/**
 * Created by ggec5486 on 2015/7/30.
 */
public class LocalWorker extends AbstractWorker<String, Integer, Bitmap>{

    private String data = "";
    private boolean isMultiPictures = false;

    private WeakReference<ImageView> viewWeakReference;
    private WeakReference<ProgressBar> pbWeakReference;

    private FileLocationMethod method;

    private IWeiciyuanDrawable IWeiciyuanDrawable;

    @Override
    public String getUrl() {
        return data;
    }
    public LocalWorker(ImageView view, String url, FileLocationMethod method,
                       boolean isMultiPictures) {

        this.viewWeakReference = new WeakReference<ImageView>(view);
        this.data = url;
        this.method = method;
        this.isMultiPictures = isMultiPictures;
    }

    public LocalWorker(IWeiciyuanDrawable view, String url, FileLocationMethod method,
                       boolean isMultiPictures) {

        this(view.getImageView(), url, method, false);
        this.IWeiciyuanDrawable = view;
        this.pbWeakReference = new WeakReference<ProgressBar>(view.getProgressBar());
        view.setGifFlag(false);

        if (view.getProgressBar() != null) {
            view.getProgressBar().setVisibility(View.INVISIBLE);
            view.getProgressBar().setProgress(0);
        }

        this.isMultiPictures = isMultiPictures;
    }


    @Override
    protected Bitmap doInBackground(String... url) {

        String path = FileManager.getFilePathFromUrl(data, method);

        int height = 0;
        int width = 0;

        switch (method) {
            case avatar_small:
            case avatar_large:
                width = GlobalContext.getInstance().getResources()
                        .getDimensionPixelSize(R.dimen.timeline_avatar_width)
                        - Utility.dip2px(5) * 2;
                height = GlobalContext.getInstance().getResources()
                        .getDimensionPixelSize(R.dimen.timeline_avatar_height)
                        - Utility.dip2px(5) * 2;
                break;

            case picture_thumbnail:
                width = GlobalContext.getInstance().getResources()
                        .getDimensionPixelSize(R.dimen.timeline_pic_thumbnail_width);
                height = GlobalContext.getInstance().getResources()
                        .getDimensionPixelSize(R.dimen.timeline_pic_thumbnail_height);
                break;

            case picture_large:
            case picture_bmiddle:
                if (!isMultiPictures) {
                    DisplayMetrics metrics = GlobalContext.getInstance().getDisplayMetrics();

                    float reSize = GlobalContext.getInstance().getResources()
                            .getDisplayMetrics().density;

                    height = GlobalContext.getInstance().getResources()
                            .getDimensionPixelSize(R.dimen.timeline_pic_high_thumbnail_height);
                    //8 is  layout padding
                    width = (int) (metrics.widthPixels - (8 + 8) * reSize);
                } else {
                    height = width = Utility.dip2px(120);
                }
                break;
        }

        Bitmap bitmap;

        switch (method) {
            case avatar_small:
            case avatar_large:
                bitmap = ImageUtility.getRoundedCornerPic(path, width, height, Utility.dip2px(2));
                break;
            default:
                bitmap = ImageUtility.getRoundedCornerPic(path, width, height, 0);
                break;
        }

        return bitmap;
    }

    @Override
    protected void onCancelled(Bitmap bitmap) {
        super.onCancelled(bitmap);
        ImageView imageView = viewWeakReference.get();
        if (!isMySelf(imageView)) {
            return;
        }

        imageView.setImageDrawable(
                new ColorDrawable(DebugColor.READ_CANCEL));
    }

    @Override
    protected void onPostExecute(Bitmap bitmap) {
        super.onPostExecute(bitmap);
        displayBitmap(bitmap);
    }

    private void displayBitmap(Bitmap bitmap) {

        ImageView imageView = viewWeakReference.get();
        if (!isMySelf(imageView)) {
            return;
        }

        if (pbWeakReference != null) {
            ProgressBar pb = pbWeakReference.get();
            if (pb != null) {
                pb.setVisibility(View.INVISIBLE);
            }
        }

        if (bitmap != null) {
            if (IWeiciyuanDrawable != null) {
                IWeiciyuanDrawable.setGifFlag(ImageUtility.isThisPictureGif(getUrl()));
            }
            playImageViewAnimation(imageView, bitmap);
            GlobalContext.getInstance().getBitmapCache().put(data, bitmap);
        } else {
            imageView.setImageDrawable(new ColorDrawable(DebugColor.READ_FAILED));
        }
    }

    private void playImageViewAnimation(final ImageView view, final Bitmap bitmap) {

        view.setImageBitmap(bitmap);
        resetProgressBarStatues();
        view.setAlpha(0f);
        view.animate().alpha(1.0f).setDuration(500)
                .setListener(new LayerEnablingAnimatorListener(view, null));
        view.setTag(getUrl());
    }

    private void resetProgressBarStatues() {
        if (pbWeakReference == null) {
            return;
        }
        ProgressBar pb = pbWeakReference.get();
        if (pb != null) {
            pb.setVisibility(View.INVISIBLE);
        }
    }


}
