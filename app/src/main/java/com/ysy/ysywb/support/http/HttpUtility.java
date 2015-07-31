package com.ysy.ysywb.support.http;

import com.ysy.ysywb.support.error.WeiboException;
import com.ysy.ysywb.support.file.FileDownloaderHttpHelper;

import java.util.Map;

/**
 * Created by ggec5486 on 2015/6/11.
 */
public class HttpUtility {

    private static HttpUtility httpUtility = new HttpUtility();

    private HttpUtility() {
    }

    public static HttpUtility getInstance() {
        return httpUtility;
    }


    public String executeNormalTask(HttpMethod httpMethod, String url, Map<String, String> param)
            throws WeiboException {
        return new JavaHttpUtility().executeNormalTask(httpMethod, url, param);

    }

    public boolean executeDownloadTask(String url, String path,
                                       FileDownloaderHttpHelper.DownloadListener downloadListener) {
        return !Thread.currentThread().isInterrupted() && new JavaHttpUtility()
                .doGetSaveFile(url, path, downloadListener);
    }
}
