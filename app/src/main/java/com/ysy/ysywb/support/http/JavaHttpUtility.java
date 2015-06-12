package com.ysy.ysywb.support.http;

import android.text.TextUtils;
import android.widget.Toast;

import com.ysy.ysywb.R;
import com.ysy.ysywb.support.debug.AppLogger;
import com.ysy.ysywb.support.error.WeiboException;
import com.ysy.ysywb.support.utils.GlobalContext;
import com.ysy.ysywb.support.utils.Utility;


import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.net.ssl.HttpsURLConnection;

/**
 * Created by ggec5486 on 2015/6/11.
 */
public class JavaHttpUtility {

    private static final int CONNECT_TIMEOUT = 10 * 1000;
    private static final int READ_TIMEOUT = 10 * 1000;
    private static final int DOWNLOAD_CONNECT_TIMEOUT = 15 * 1000;
    private static final int DOWNLOAD_READ_TIMEOUT = 60 * 1000;
    private static final int UPLOAD_CONNECT_TIMEOUT = 15 * 1000;
    private static final int UPLOAD_READ_TIMEOUT = 5 * 60 * 1000;

    public String executeNormalTask(HttpMethod httpMethod, String url, Map<String, String> param)
            throws WeiboException {
        switch (httpMethod) {
            case Post:
                return doPost(url, param);
            case Get:
                return doGet(url, param);
        }
        return null;
    }

    private String doPost(String urlAddress, Map<String, String> param) throws WeiboException {
        GlobalContext globalContext = GlobalContext.getInstance();
        String errorStr = globalContext.getString(R.string.timeout);
        globalContext = null;
        try {
            URL url = new URL(urlAddress);
            Proxy proxy = getProxy();
            HttpURLConnection uRlConnection;
            if (proxy != null) {
                uRlConnection = (HttpURLConnection) url.openConnection(proxy);
            } else {
                uRlConnection = (HttpsURLConnection) url.openConnection();
            }
            uRlConnection.setDoInput(true);
            uRlConnection.setDoOutput(true);
            uRlConnection.setRequestMethod("POST");
            uRlConnection.setUseCaches(false);
            uRlConnection.setConnectTimeout(CONNECT_TIMEOUT);
            uRlConnection.setReadTimeout(READ_TIMEOUT);
            uRlConnection.setInstanceFollowRedirects(false);
            uRlConnection.setRequestProperty("Connection", "Keep-Alive");
            uRlConnection.setRequestProperty("Charset", "UTF-8");
            uRlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");
            uRlConnection.connect();

            DataOutputStream out = new DataOutputStream(uRlConnection.getOutputStream());
            out.write(Utility.encodeUrl(param).getBytes());
            out.flush();
            out.close();
            return handleResponse(uRlConnection);
        } catch (IOException e) {
            e.printStackTrace();
            throw new WeiboException(errorStr, e);
        }

    }

    private String handleResponse(HttpURLConnection httpURLConnection) throws WeiboException {

        GlobalContext globalContext = GlobalContext.getInstance();
        String errorStr = globalContext.getString(R.string.timeout);
        globalContext = null;
        int status = 0;

        try {
            status = httpURLConnection.getResponseCode();
        } catch (IOException e) {
            e.printStackTrace();
            httpURLConnection.disconnect();
            throw new WeiboException(errorStr, e);
        }
        if (status != HttpURLConnection.HTTP_OK) {
            return handleError(httpURLConnection);
        }
        return readResult(httpURLConnection);
    }

    private static java.net.Proxy getProxy() {
        String proxyHost = System.getProperty("http.proxyHost");
        String proxyPort = System.getProperty("http.proxyPort");
        if (!TextUtils.isEmpty(proxyHost) && !TextUtils.isEmpty(proxyPort)) {
            return new java.net.Proxy(java.net.Proxy.Type.HTTP,
                    new InetSocketAddress(proxyHost, Integer.valueOf(proxyPort)));
        } else {
            return null;
        }

    }

    private String handleError(HttpURLConnection urlConnection) throws WeiboException {
        Toast.makeText(GlobalContext.getInstance(), "网络错误", Toast.LENGTH_SHORT).show();
        return null;
    }

    private String readResult(HttpURLConnection urlConnection) throws WeiboException {
        InputStream is = null;
        BufferedReader buffer = null;
        GlobalContext globalContext = GlobalContext.getInstance();
        String errorStr = globalContext.getString(R.string.timeout);
        globalContext = null;

        try {
            is = urlConnection.getInputStream();
            String content_encode = urlConnection.getContentEncoding();

            if (!TextUtils.isEmpty(content_encode) && content_encode
                    .equals("gzip")) {
                is = new GZIPInputStream(is);
            }

            buffer = new BufferedReader(new InputStreamReader(is));
            StringBuilder strBuilder = new StringBuilder();
            String line;
            while ((line = buffer.readLine()) != null) {
                strBuilder.append(line);
            }
            return strBuilder.toString();

        } catch (IOException e) {
            e.printStackTrace();
            throw new WeiboException(errorStr, e);
        } finally {
            Utility.closeSilently(is);
            Utility.closeSilently(buffer);
            urlConnection.disconnect();
        }
    }

    public String doGet(String urlStr, Map<String, String> param) throws WeiboException {
        GlobalContext globalContext = GlobalContext.getInstance();
        String errorStr = globalContext.getString(R.string.timeout);
        globalContext = null;
        InputStream is = null;
        try {

            StringBuilder urlBuilder = new StringBuilder(urlStr);
            urlBuilder.append("?").append(Utility.encodeUrl(param));
            URL url = new URL(urlBuilder.toString());
            AppLogger.d("get request" + url);
            Proxy proxy = getProxy();
            HttpURLConnection urlConnection;
            if (proxy != null) {
                urlConnection = (HttpURLConnection) url.openConnection(proxy);
            } else {
                urlConnection = (HttpURLConnection) url.openConnection();
            }

            urlConnection.setRequestMethod("GET");
            urlConnection.setDoOutput(false);
            urlConnection.setConnectTimeout(CONNECT_TIMEOUT);
            urlConnection.setReadTimeout(READ_TIMEOUT);
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("Charset", "UTF-8");
            urlConnection.setRequestProperty("Accept-Encoding", "gzip, deflate");

            urlConnection.connect();

            return handleResponse(urlConnection);
        } catch (IOException e) {
            e.printStackTrace();
            throw new WeiboException(errorStr, e);
        }
    }
}
