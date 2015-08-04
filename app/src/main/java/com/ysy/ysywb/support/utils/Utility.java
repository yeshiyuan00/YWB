package com.ysy.ysywb.support.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.text.TextUtils;
import android.view.HapticFeedbackConstants;
import android.view.View;
import android.widget.ListView;

import com.ysy.ysywb.BuildConfig;
import com.ysy.ysywb.bean.AccountBean;
import com.ysy.ysywb.support.debug.AppLogger;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by ggec5486 on 2015/6/9.
 */
public class Utility {

    //the position within the adapter's data set, will plus header view count
    public static void setListViewAdapterPosition(final ListView listView,
                                                  final int adapterItemPosition, final int top, final Runnable runnable) {

        listView.setSelectionFromTop(adapterItemPosition + listView.getHeaderViewsCount(), top);
        AppLogger.i("ListView scrollTo " + (adapterItemPosition + listView.getHeaderViewsCount())
                + " offset " + top);
        if (runnable != null) {
            runnable.run();
        }
    }

    public static void stopListViewScrollingAndScrollToTop(ListView listView) {
        Runnable runnable = JavaReflectionUtility.getValue(listView, "mFlingRunnable");
        listView.removeCallbacks(runnable);
        listView.setSelection(Math.min(listView.getFirstVisiblePosition(), 5));
        listView.smoothScrollToPosition(0);
    }

    //long click link(schedule show dialog event), press home button(onPause onSaveInstance), show dialog,then crash....
    //executePendingTransactions still occur crash
    public static void forceShowDialog(FragmentActivity activity, DialogFragment dialogFragment) {
        try {
            dialogFragment.show(activity.getSupportFragmentManager(), "");
            activity.getSupportFragmentManager().executePendingTransactions();
        } catch (Exception ignored) {

        }
    }

    public static void vibrate(Context context, View view) {
//        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
//        vibrator.vibrate(30);
        view.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
    }

    public static String getDomainFromWeiboAccountLink(String url) {
        url = convertWeiboCnToWeiboCom(url);

        final String NORMAL_DOMAIN_PREFIX = "http://weibo.com/";
        final String ENTERPRISE_DOMAIN_PREFIX = "http://e.weibo.com/";

        if (TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException("Url can't be empty");
        }

        if (!url.startsWith(NORMAL_DOMAIN_PREFIX) && !url.startsWith(ENTERPRISE_DOMAIN_PREFIX)) {
            throw new IllegalArgumentException(
                    "Url must start with " + NORMAL_DOMAIN_PREFIX + " or "
                            + ENTERPRISE_DOMAIN_PREFIX);
        }

        String domain = null;
        if (url.startsWith(ENTERPRISE_DOMAIN_PREFIX)) {
            domain = url.substring(ENTERPRISE_DOMAIN_PREFIX.length());
        } else if (url.startsWith(NORMAL_DOMAIN_PREFIX)) {
            domain = url.substring(NORMAL_DOMAIN_PREFIX.length());
        }
        domain = domain.replace("/", "");
        return domain;
    }

    //todo need refactor...
    public static boolean isWeiboAccountDomainLink(String url) {
        if (TextUtils.isEmpty(url)) {
            return false;
        } else {
            url = convertWeiboCnToWeiboCom(url);
            boolean a = url.startsWith("http://weibo.com/") || url
                    .startsWith("http://e.weibo.com/");
            boolean b = !url.contains("?");

            String tmp = url;
            if (tmp.endsWith("/")) {
                tmp = tmp.substring(0, tmp.lastIndexOf("/"));
            }

            int count = 0;
            char[] value = tmp.toCharArray();
            for (char c : value) {
                if ("/".equalsIgnoreCase(String.valueOf(c))) {
                    count++;
                }
            }
            return a && b && count == 3 && !"http://weibo.com/pub".equals(tmp);
        }
    }

    public static String getIdFromWeiboAccountLink(String url) {

        url = convertWeiboCnToWeiboCom(url);

        String id = url.substring("http://weibo.com/u/".length());
        id = id.replace("/", "");
        return id;
    }

    public static boolean isWeiboAccountIdLink(String url) {
        url = convertWeiboCnToWeiboCom(url);

        return !TextUtils.isEmpty(url) && url.startsWith("http://weibo.com/u/");
    }

    private static String convertWeiboCnToWeiboCom(String url) {
        if (!TextUtils.isEmpty(url)) {
            if (url.startsWith("http://weibo.cn")) {
                url = url.replace("http://weibo.cn", "http://weibo.com");
            } else if (url.startsWith("http://www.weibo.com")) {
                url = url.replace("http://www.weibo.com", "http://weibo.com");
            } else if (url.startsWith("http://www.weibo.cn")) {
                url = url.replace("http://www.weibo.cn", "http://weibo.com");
            }
        }
        return url;
    }

    public static void printStackTrace(Exception e) {
        if (BuildConfig.DEBUG) {
            e.printStackTrace();
        }
    }

    public static boolean isKK() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean isTokenValid(AccountBean account) {
        return !TextUtils.isEmpty(account.getAccess_token())
                && (account.getExpires_time() == 0
                || (System.currentTimeMillis()) < account.getExpires_time());
    }

    public static boolean isDebugMode() {
        return BuildConfig.DEBUG;
    }


    /**
     * Parse a URL query and fragment parameters into a key-value bundle.
     */
    public static Bundle parseUrl(String url) {
        // hack to prevent MalformedURLException
        url = url.replace("weiboconnect", "http");
        try {
            URL u = new URL(url);
            Bundle b = decodeUrl(u.getQuery());
            b.putAll(decodeUrl(u.getRef()));
            return b;
        } catch (MalformedURLException e) {
            return new Bundle();
        }
    }

    public static Bundle decodeUrl(String s) {
        Bundle params = new Bundle();
        if (s != null) {
            String array[] = s.split("&");
            for (String parameter : array) {
                String v[] = parameter.split("=");
                try {
                    params.putString(URLDecoder.decode(v[0], "UTF-8"),
                            URLDecoder.decode(v[1], "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        return params;
    }

    public static String rot47(String value) {
        int length = value.length();
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < length; i++) {
            char c = value.charAt(i);

            // Process letters, numbers, and symbols -- ignore spaces.
            if (c != ' ') {
                // Add 47 (it is ROT-47, after all).
                c += 47;

                // If character is now above printable range, make it printable.
                // Range of printable characters is ! (33) to ~ (126).  A value
                // of 127 (just above ~) would therefore get rotated down to a
                // 33 (the !).  The value 94 comes from 127 - 33 = 94, which is
                // therefore the value that needs to be subtracted from the
                // non-printable character to put it into the correct printable
                // range.
                if (c > '~') {
                    c -= 94;
                }
            }

            result.append(c);
        }

        return result.toString();
    }

    public static String encodeUrl(Map<String, String> param) {
        if (param == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        Set<String> keys = param.keySet();
        boolean first = true;

        for (String key : keys) {
            String value = param.get(key);
            //pain...EditMyProfileDao params' values can be empty
            if (!TextUtils.isEmpty(value) || key.equals("description") || key.equals("url")) {
                if (first) {
                    first = false;
                } else {
                    sb.append("&");
                }
                try {
                    sb.append(URLEncoder.encode(key, "UTF-8")).append("=")
                            .append(URLEncoder.encode(param.get(key), "UTF-8"));
                } catch (UnsupportedEncodingException e) {

                }
            }
        }

        return sb.toString();
    }


    public static void closeSilently(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException ignored) {

            }
        }
    }

    public static long calcTokenExpiresInDays(AccountBean account) {
        long days = TimeUnit.MILLISECONDS
                .toDays(account.getExpires_time() - System.currentTimeMillis());
        return days;
    }

    public static boolean isSystemRinger(Context context) {
        AudioManager manager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        return manager.getRingerMode() == AudioManager.RINGER_MODE_NORMAL;
    }

    public static int dip2px(int dipValue) {
        float reSize = GlobalContext.getInstance().getResources().getDisplayMetrics().density;
        return (int) ((dipValue * reSize) + 0.5);
    }

    public static boolean isConnected(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();

        return networkInfo != null && networkInfo.isConnected();
    }

    public static boolean isWifi(Context context) {
        ConnectivityManager cm = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
                return true;
            }
        }
        return false;
    }

    public static boolean isCertificateFingerprintCorrect(Context context) {
        try {
            PackageManager pm = context.getPackageManager();
            String packageName = context.getPackageName();
            int flags = PackageManager.GET_SIGNATURES;

            PackageInfo packageInfo = pm.getPackageInfo(packageName, flags);

            Signature[] signatures = packageInfo.signatures;

            byte[] cert = signatures[0].toByteArray();

            String strResult = "";

            MessageDigest md;

            md = MessageDigest.getInstance("MD5");
            md.update(cert);
            for (byte b : md.digest()) {
                strResult += Integer.toString(b & 0xff, 16);
            }
            strResult = strResult.toUpperCase();
            //debug
            if ("DE421D82D4BBF9042886E72AA31FE22".toUpperCase().equals(strResult)) {
                return false;
            }
            //relaease
            if ("C96155C3DAD4CA1069808FBAC813A69".toUpperCase().equals(strResult)) {
                return true;
            }
            AppLogger.e(strResult);
        } catch (NoSuchAlgorithmException ex) {
            ex.printStackTrace();
        } catch (PackageManager.NameNotFoundException ex) {
            ex.printStackTrace();
        }

        return false;
    }
}
