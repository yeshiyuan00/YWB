package com.ysy.ysywb.ui.main;

import android.content.Intent;

import com.ysy.ysywb.bean.AccountBean;
import com.ysy.ysywb.support.utils.BundleArgsConstants;
import com.ysy.ysywb.support.utils.GlobalContext;

/**
 * Created by ggec5486 on 2015/6/9.
 */
public class MainTimeLineActivity extends MainTimeLineParentActivity {

    public static Intent newIntent() {
        return new Intent(GlobalContext.getInstance(), MainTimeLineActivity.class);
    }

    public static Intent newIntent(AccountBean accountBean) {
        Intent intent = newIntent();
        intent.putExtra(BundleArgsConstants.ACCOUNT_EXTRA, accountBean);
        return intent;
    }
}
