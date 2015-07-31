package com.ysy.ysywb.ui.login;

import android.app.ActionBar;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.Toast;

import com.ysy.ysywb.R;
import com.ysy.ysywb.bean.AccountBean;
import com.ysy.ysywb.bean.UserBean;
import com.ysy.ysywb.dao.URLHelper;
import com.ysy.ysywb.dao.login.OAuthDao;
import com.ysy.ysywb.support.database.AccountDBTask;
import com.ysy.ysywb.support.debug.AppLogger;
import com.ysy.ysywb.support.error.WeiboException;
import com.ysy.ysywb.support.lib.MyAsyncTask;
import com.ysy.ysywb.support.utils.Utility;
import com.ysy.ysywb.ui.interfaces.AbstractAppActivity;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ggec5486 on 2015/6/8.
 */
public class OAuthActivity extends AbstractAppActivity {

    private static final String ACTION_OPEN_FROM_APP_INNER = "org.qii.weiciyuan:accountactivity";

    private WebView webView;
    private MenuItem refreshItem;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.oauthactivity_layout);
        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle(getString(R.string.login));
        webView = (WebView) findViewById(R.id.webView);
        webView.setWebViewClient(new WeiboWebViewClient());


        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setSaveFormData(false);
        settings.setSavePassword(false);
        settings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        settings.setRenderPriority(WebSettings.RenderPriority.HIGH);

        CookieSyncManager.createInstance(this);
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();


    }

    private class WeiboWebViewClient extends WebViewClient {

        @Override
        public void onPageFinished(WebView view, String url) {
            if (!url.equals("about:blank")) {
                if (refreshItem.getActionView() != null) {
                    refreshItem.getActionView().clearAnimation();
                    refreshItem.setActionView(null);
                }
            }
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            view.loadUrl(url);
            return true;
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {

            if (url.startsWith(URLHelper.DIRECT_URL)) {
                handleRedirectUrl(view, url);
                view.stopLoading();
                finish();
                return;
            }
            super.onPageStarted(view, url, favicon);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.actionbar_menu_oauthactivity, menu);
        refreshItem = menu.findItem(R.id.menu_refresh);
        refresh();
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(OAuthActivity.this, AccountActivity.class);
                intent.setAction(ACTION_OPEN_FROM_APP_INNER);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                return true;
            case R.id.menu_refresh:
                refresh();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void refresh() {

        webView.clearView();
        webView.loadUrl("about:blank");
        LayoutInflater inflater = (LayoutInflater) getSystemService(
                Context.LAYOUT_INFLATER_SERVICE);
        ImageView iv = (ImageView) inflater.inflate(R.layout.refresh_action_view, null);

        Animation rotation = AnimationUtils.loadAnimation(this, R.anim.refresh);
        iv.startAnimation(rotation);

        refreshItem.setActionView(iv);
        webView.loadUrl(getWeiboOAuthUrl());
    }

    private String getWeiboOAuthUrl() {
        Map<String, String> parameters = new HashMap<String, String>();
        parameters.put("client_id", Utility.rot47("`_edd``d`b"));
        parameters.put("response_type", "token");
        parameters.put("redirect_uri", Utility.rot47("9EEADi^^2A:]H6:3@]4@>^@2FE9a^5672F=E]9E>="));
        parameters.put("display", "mobile");
        return "https://api.weibo.com/oauth2/authorize" + "?" + Utility.encodeUrl(parameters)
                + "&scope=friendships_groups_read,friendships_groups_write";
    }

    private void handleRedirectUrl(WebView view, String url) {
        Bundle values = Utility.parseUrl(url);
        String error = values.getString("error");
        String error_code = values.getString("error_code");

        Intent intent = new Intent();
        intent.putExtras(values);

        if (error == null && error_code == null) {

            String access_token = values.getString("access_token");
            String expires_time = values.getString("expires_in");
            setResult(RESULT_OK, intent);
            new OAuthTask(this).execute(access_token, expires_time);
        } else {
            Toast.makeText(OAuthActivity.this, getString(R.string.you_cancel_login),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private static class OAuthTask extends MyAsyncTask<String, UserBean, DBResult> {

        private WeiboException e;
        private ProgressFragment progressFragment = ProgressFragment.newInstance();
        private WeakReference<OAuthActivity> oAuthActivityWeakReference;

        private OAuthTask(OAuthActivity activity) {
            oAuthActivityWeakReference = new WeakReference<OAuthActivity>(activity);
        }

        @Override
        protected void onPreExecute() {
            progressFragment.setAsyncTask(this);
            OAuthActivity activity = oAuthActivityWeakReference.get();
            if (activity != null) {
                progressFragment.show(activity.getSupportFragmentManager(), "");
            }
        }

        @Override
        protected DBResult doInBackground(String... params) {

            String token = params[0];
            long expiresInSeconds = Long.valueOf(params[1]);

            try {
                UserBean user = new OAuthDao(token).getOAuthUserInfo();
                AccountBean account = new AccountBean();
                account.setAccess_token(token);
                account.setExpires_time(System.currentTimeMillis() + expiresInSeconds * 1000);
                account.setInfo(user);
                AppLogger
                        .e("token expires in " + Utility.calcTokenExpiresInDays(account) + " days");
                return AccountDBTask.addOrUpdateAccount(account, false);
            } catch (WeiboException e) {
                AppLogger.e(e.getError());
                this.e = e;
                cancel(true);
                return null;
            }
        }
    }

    public static class ProgressFragment extends DialogFragment {
        MyAsyncTask asyncTask = null;

        public static ProgressFragment newInstance() {
            ProgressFragment frag = new ProgressFragment();
            frag.setRetainInstance(true);
            Bundle args = new Bundle();
            frag.setArguments(args);
            return frag;
        }

        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            ProgressDialog dialog = new ProgressDialog(getActivity());
            dialog.setMessage(getString(R.string.oauthing));
            dialog.setIndeterminate(false);
            dialog.setCancelable(true);
            return dialog;
        }

        @Override
        public void onCancel(DialogInterface dialog) {
            if (asyncTask != null) {
                asyncTask.cancel(true);
            }
            super.onCancel(dialog);
        }

        void setAsyncTask(MyAsyncTask task) {
            asyncTask = task;
        }
    }

    public static enum DBResult {
        add_successfuly, update_successfully
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) {
            webView.stopLoading();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        webView.clearCache(true);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            Toast.makeText(OAuthActivity.this, getString(R.string.you_cancel_login),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}
