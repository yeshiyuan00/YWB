package com.ysy.ysywb.ui.main;

import android.app.ActionBar;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.slidingmenu.lib.SlidingMenu;
import com.ysy.ysywb.R;
import com.ysy.ysywb.bean.AccountBean;
import com.ysy.ysywb.bean.UserBean;
import com.ysy.ysywb.othercomponent.MusicReceiver;
import com.ysy.ysywb.support.lib.RecordOperationAppBroadcastReceiver;
import com.ysy.ysywb.support.settinghelper.SettingUtility;
import com.ysy.ysywb.support.utils.BundleArgsConstants;
import com.ysy.ysywb.support.utils.GlobalContext;
import com.ysy.ysywb.ui.maintimeline.FriendsTimeLineFragment;
import com.ysy.ysywb.ui.search.SearchMainParentFragment;

/**
 * Created by ggec5486 on 2015/6/9.
 */
public class MainTimeLineActivity extends MainTimeLineParentActivity {
    public static final int REQUEST_CODE_UPDATE_FRIENDS_TIMELINE_COMMENT_REPOST_COUNT = 0;
    public static final int REQUEST_CODE_UPDATE_MENTIONS_WEIBO_TIMELINE_COMMENT_REPOST_COUNT = 1;
    public static final int REQUEST_CODE_UPDATE_MY_FAV_TIMELINE_COMMENT_REPOST_COUNT = 2;

    private AccountBean accountBean;

    private NewMsgInterruptBroadcastReceiver newMsgInterruptBroadcastReceiver;
    private MusicReceiver musicReceiver;

    private ScrollableListFragment currentFragment;
    private TextView titleText;
    private View clickToTop;

    public static interface ScrollableListFragment {
        public void scrollToTop();
    }

    public static Intent newIntent() {
        return new Intent(GlobalContext.getInstance(), MainTimeLineActivity.class);
    }

    public static Intent newIntent(AccountBean accountBean) {
        Intent intent = newIntent();
        intent.putExtra(BundleArgsConstants.ACCOUNT_EXTRA, accountBean);
        return intent;
    }

    public String getToken() {
        return accountBean.getAccess_token();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            accountBean = savedInstanceState.getParcelable(BundleArgsConstants.ACCOUNT_EXTRA);
        } else {
            Intent intent = getIntent();
            accountBean = intent
                    .getParcelableExtra(BundleArgsConstants.ACCOUNT_EXTRA);
        }
        if (accountBean == null) {
            accountBean = GlobalContext.getInstance().getAccountBean();
        }

        GlobalContext.getInstance().setGroup(null);
        GlobalContext.getInstance().setAccountBean(accountBean);
        SettingUtility.setDefaultAccountId(accountBean.getUid());

        buildInterface(savedInstanceState);
    }


    //build phone ui or table ui
    private void buildInterface(Bundle savedInstanceState) {
        getActionBar().setTitle(GlobalContext.getInstance().getCurrentAccountName());
        getWindow().setBackgroundDrawable(null);
        setContentView(R.layout.menu_right);
        boolean phone = findViewById(R.id.menu_frame) == null;

        if (phone) {
            buildPhoneInterface(savedInstanceState);
        } else {
            buildPadInterface(savedInstanceState);
        }

        buildCustomActionBarTitle(savedInstanceState);
        if (savedInstanceState == null) {
            initFragments();
        }

    }

    private void buildPhoneInterface(Bundle savedInstanceState) {
        setBehindContentView(R.layout.menu_frame);
        getSlidingMenu().setSlidingEnabled(true);
        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getSlidingMenu().setMode(SlidingMenu.LEFT);
        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
    }

    private void buildPadInterface(Bundle savedInstanceState) {
        View v = new View(this);
        setBehindContentView(v);
        getSlidingMenu().setSlidingEnabled(false);
        getSlidingMenu().setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
    }

    private void buildCustomActionBarTitle(Bundle savedInstanceState) {
        View title = getLayoutInflater().inflate(R.layout.maintimelineactivity_title_layout, null);
        titleText = (TextView) title.findViewById(R.id.tv_title);
        clickToTop = title.findViewById(R.id.tv_click_to_top);
        clickToTop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scrollCurrentListViewToTop();
            }
        });
        View write = title.findViewById(R.id.btn_write);
        write.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: 2015/8/4
               /* Intent intent = WriteWeiboActivity
                        .newIntent(GlobalContext.getInstance().getAccountBean());
                startActivity(intent);*/
            }
        });
        ActionBar.LayoutParams layoutParams = new ActionBar.LayoutParams(ViewGroup
                .LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT,
                Gravity.RIGHT);
        getActionBar().setCustomView(title, layoutParams);
        getActionBar().setDisplayShowCustomEnabled(true);
    }

    private void scrollCurrentListViewToTop() {
        if (this.currentFragment != null) {
            this.currentFragment.scrollToTop();
        }
    }

    //init fragments
    private void initFragments() {
        Fragment friend = getFriendsTimeLineFragment();
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        if (!friend.isAdded()) {
            fragmentTransaction.add(R.id.menu_right_fl, friend,
                    FriendsTimeLineFragment.class.getName());
            fragmentTransaction.hide(friend);
        }

        if(GlobalContext.getInstance().getAccountBean().isBlack_magic()){
            Fragment search = getSearchFragment();
            if (!search.isAdded()) {
                fragmentTransaction
                        .add(R.id.menu_right_fl, search, SearchMainParentFragment.class.getName());
                fragmentTransaction.hide(search);
            }

        }
        if (!fragmentTransaction.isEmpty()) {
            fragmentTransaction.commit();
            getSupportFragmentManager().executePendingTransactions();
        }
    }



    public SearchMainParentFragment getSearchFragment() {
        SearchMainParentFragment fragment = ((SearchMainParentFragment) getSupportFragmentManager()
                .findFragmentByTag(
                        SearchMainParentFragment.class.getName()));
        if (fragment == null) {
            fragment = SearchMainParentFragment.newInstance();
        }
        return fragment;
    }

    public FriendsTimeLineFragment getFriendsTimeLineFragment() {
        FriendsTimeLineFragment fragment = (FriendsTimeLineFragment) getSupportFragmentManager()
                .findFragmentByTag(FriendsTimeLineFragment.class.getName());
        if (fragment == null) {
            fragment = FriendsTimeLineFragment.newInstance(getAccount(), getUser(), getToken());
        }
        return fragment;
    }

    public UserBean getUser() {
        return accountBean.getInfo();
    }

    public AccountBean getAccount() {
        return accountBean;
    }

    //todo
    private class NewMsgInterruptBroadcastReceiver extends RecordOperationAppBroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            AccountBean intentAccount = intent
                    .getParcelableExtra(BundleArgsConstants.ACCOUNT_EXTRA);
        }
    }
}
