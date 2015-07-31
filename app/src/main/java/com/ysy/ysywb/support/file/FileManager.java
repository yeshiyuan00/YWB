package com.ysy.ysywb.support.file;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.text.TextUtils;
import android.widget.Toast;

import com.ysy.ysywb.R;
import com.ysy.ysywb.support.database.DownloadPicturesDBTask;
import com.ysy.ysywb.support.debug.AppLogger;
import com.ysy.ysywb.support.utils.GlobalContext;

import java.io.File;
import java.io.IOException;

/**
 * Created by ggec5486 on 2015/7/30.
 */
public class FileManager {

    private static final String PICTURE_CACHE = "picture_cache";
    private static final String TXT2PIC = "txt2pic";
    private static final String WEBVIEW_FAVICON = "favicon";
    private static final String LOG = "log";
    private static final String WEICIYUAN = "weiciyuan";


    public static String getTxt2picPath() {
        if (!isExternalStorageMounted()) {
            return "";
        }
        String path = getSdCardPath() + File.separator + TXT2PIC;
        File file = new File(path);
        if (file.exists()) {
            file.mkdirs();
        }
        return path;
    }

    public static String getLogDir() {
        if (!isExternalStorageMounted()) {
            return "";
        } else {
            String path = getSdCardPath() + File.separator + LOG;
            if (!new File(path).exists()) {
                new File(path).mkdirs();
            }
            return path;
        }
    }
    public static boolean isExternalStorageMounted() {

        boolean canRead = Environment.getExternalStorageDirectory().canRead();
        boolean onlyRead = Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED_READ_ONLY);
        boolean unMounted = Environment.getExternalStorageState().equals(
                Environment.MEDIA_UNMOUNTED);

        return !(!canRead || onlyRead || unMounted);
    }

    private static volatile boolean cantReadBecauseOfAndroidBugPermissionProblem = false;

    public static String getSdCardPath() {
        if (isExternalStorageMounted()) {
            File path = GlobalContext.getInstance().getExternalCacheDir();
            if (path != null) {
                return path.getAbsolutePath();
            } else {
                if (!cantReadBecauseOfAndroidBugPermissionProblem) {
                    cantReadBecauseOfAndroidBugPermissionProblem = true;
                    final Activity activity = GlobalContext.getInstance().getActivity();
                    if (activity == null || activity.isFinishing()) {
                        GlobalContext.getInstance().getUIHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(GlobalContext.getInstance(),
                                        R.string.please_deleted_cache_dir, Toast.LENGTH_SHORT)
                                        .show();
                            }
                        });

                        return "";
                    }
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            new AlertDialog.Builder(activity)
                                    .setTitle(R.string.something_error)
                                    .setMessage(R.string.please_deleted_cache_dir)
                                    .setPositiveButton(R.string.ok,
                                            new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog,
                                                                    int which) {

                                                }
                                            })
                                    .show();
                        }
                    });
                }
            }
        } else {
            return "";
        }

        return "";
    }

    public static String getFilePathFromUrl(String url, FileLocationMethod method) {

        if (!isExternalStorageMounted()) {
            return "";
        }

        if (TextUtils.isEmpty(url)) {
            return "";
        }

        return DownloadPicturesDBTask.get(url);
    }

    public static String generateDownloadFileName(String url) {

        if (!isExternalStorageMounted()) {
            return "";
        }

        if (TextUtils.isEmpty(url)) {
            return "";
        }

        String path = String.valueOf(url.hashCode());
        String result = getSdCardPath() + File.separator + PICTURE_CACHE + File.separator + path;
        if (url.endsWith(".jpg")) {
            result += ".jpg";
        } else if (url.endsWith(".gif")) {
            result += ".gif";
        }
        if (!result.endsWith(".jpg") && !result.endsWith(".gif") && !result.endsWith(".png")) {
            result = result + ".jpg";
        }

        return result;
    }

    public static File createNewFileInSDCard(String absolutePath) {
        if (!isExternalStorageMounted()) {
            AppLogger.e("sdcard unavailiable");
            return null;
        }

        if (TextUtils.isEmpty(absolutePath)) {
            return null;
        }

        File file = new File(absolutePath);
        if (file.exists()) {
            return file;
        } else {
            File dir = file.getParentFile();
            if (!dir.exists()) {
                dir.mkdirs();
            }

            try {
                if (file.createNewFile()) {
                    return file;
                }
            } catch (IOException e) {
                AppLogger.d(e.getMessage());
                return null;
            }
        }
        return null;
    }

    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for (int i = 0; i < files.length; i++) {
                if (files[i].isDirectory()) {
                    deleteDirectory(files[i]);
                } else {
                    files[i].delete();
                }
            }
        }
        return (path.delete());
    }
}
