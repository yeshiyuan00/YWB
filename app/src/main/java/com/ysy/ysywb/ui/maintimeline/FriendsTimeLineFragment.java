package com.ysy.ysywb.ui.maintimeline;

import android.os.Bundle;
import android.view.View;
import android.widget.BaseAdapter;

import com.ysy.ysywb.bean.AccountBean;
import com.ysy.ysywb.bean.GroupBean;
import com.ysy.ysywb.bean.MessageListBean;
import com.ysy.ysywb.bean.UserBean;
import com.ysy.ysywb.bean.android.MessageTimeLineData;
import com.ysy.ysywb.bean.android.TimeLinePosition;
import com.ysy.ysywb.support.database.FriendsTimeLineDBTask;
import com.ysy.ysywb.support.debug.AppLogger;
import com.ysy.ysywb.support.lib.MyAsyncTask;
import com.ysy.ysywb.support.lib.TopTipBar;
import com.ysy.ysywb.support.settinghelper.SettingUtility;
import com.ysy.ysywb.support.utils.GlobalContext;
import com.ysy.ysywb.support.utils.Utility;
import com.ysy.ysywb.ui.basefragment.AbstractMessageTimeLineFragment;
import com.ysy.ysywb.ui.main.MainTimeLineActivity;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * User: ysy
 * Date: 2015/8/4
 */
public class FriendsTimeLineFragment extends AbstractMessageTimeLineFragment<MessageListBean>
        implements GlobalContext.MyProfileInfoChangeListener,
        MainTimeLineActivity.ScrollableListFragment {
    private static final String ARGUMENTS_ACCOUNT_EXTRA = FriendsTimeLineFragment.class.getName() + ":account_extra";
    private static final String ARGUMENTS_USER_EXTRA = FriendsTimeLineFragment.class.getName() + ":userBean_extra";
    private static final String ARGUMENTS_TOKEN_EXTRA = FriendsTimeLineFragment.class.getName() + ":token_extra";

    private BaseAdapter navAdapter;
    private AccountBean accountBean;
    private UserBean userBean;
    private String token;

    private DBCacheTask dbTask;
    private HashMap<String, MessageListBean> groupDataCache
            = new HashMap<String, MessageListBean>();
    private HashMap<String, TimeLinePosition> positionCache
            = new HashMap<String, TimeLinePosition>();


    public final static String ALL_GROUP_ID = "0";
    public final static String BILATERAL_GROUP_ID = "1";
    private String currentGroupId = ALL_GROUP_ID;

    private MessageListBean bean = new MessageListBean();

    //@Override
    public MessageListBean getList() {
        return bean;
    }


    public BaseAdapter getAdapter() {
        return timeLineAdapter;
    }


    public static FriendsTimeLineFragment newInstance(AccountBean accountBean, UserBean userBean,
                                                      String token) {
        FriendsTimeLineFragment fragment = new FriendsTimeLineFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARGUMENTS_ACCOUNT_EXTRA, accountBean);
        bundle.putParcelable(ARGUMENTS_USER_EXTRA, userBean);
        bundle.putString(ARGUMENTS_TOKEN_EXTRA, token);
        fragment.setArguments(bundle);
        return fragment;
    }


    @Override
    public void onChange(UserBean newUserBean) {
        if (navAdapter != null) {
            navAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void scrollToTop() {
        Utility.stopListViewScrollingAndScrollToTop(getListView());
    }

    private int getRecentNavIndex() {
        List<GroupBean> list = new ArrayList<GroupBean>();
        if (GlobalContext.getInstance().getGroup() != null) {
            list = GlobalContext.getInstance().getGroup().getLists();
        } else {
            list = new ArrayList<GroupBean>();
        }
        return getIndexFromGroupId(currentGroupId, list);
    }

    private int getIndexFromGroupId(String id, List<GroupBean> list) {
        if (list == null || list.size() == 0) {
            return 0;
        }

        int index = 0;

        if (id.equals("0")) {
            index = 0;
        } else if (id.equals("1")) {
            index = 1;
        }

        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getIdstr().equals(id)) {
                index = i + 2;
                break;
            }
        }
        return index;
    }

    private void setListViewPositionFromPositionsCache() {
        final TimeLinePosition timeLinePosition = positionCache.get(currentGroupId);
        AppLogger.i("Memory cached position first visible item id " + (timeLinePosition != null
                ? timeLinePosition.firstItemId : 0));
        int position =
                timeLinePosition != null ? timeLinePosition.getPosition(bean) : 0;
        int top = timeLinePosition != null ? timeLinePosition.top : 0;
        AppLogger.i("Set ListView position from memory cached position position " + position
                + " top " + top);
        Utility.setListViewAdapterPosition(getListView(), position, top, new Runnable() {
            @Override
            public void run() {
                setListViewUnreadTipBar(timeLinePosition);
            }
        });
    }

    private void setListViewUnreadTipBar(TimeLinePosition p) {
        if (p != null && p.newMsgIds != null) {
            if (SettingUtility.getEnableAutoRefresh()) {
                newMsgTipBar.setType(TopTipBar.Type.ALWAYS);
            }
            newMsgTipBar.setValue(p.newMsgIds);
        }
    }

    private void handleDBCacheResultData(List<MessageTimeLineData> result) {
        for (MessageTimeLineData single : result) {
            putToGroupDataMemoryCache(single.groupId, single.msgList);
            positionCache.put(single.groupId, single.position);
        }
    }

    private void putToGroupDataMemoryCache(String groupId, MessageListBean value) {
        MessageListBean copy = new MessageListBean();
        copy.addNewData(value);
        groupDataCache.put(groupId, copy);
    }

    private class DBCacheTask extends MyAsyncTask<Void, MessageTimeLineData,
            List<MessageTimeLineData>> {

        private WeakReference<FriendsTimeLineFragment> fragmentWeakReference;
        private String accountId;

        public DBCacheTask(FriendsTimeLineFragment friendsTimeLineFragment, String accountId) {
            fragmentWeakReference = new WeakReference<FriendsTimeLineFragment>(
                    friendsTimeLineFragment);
            this.accountId = accountId;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            FriendsTimeLineFragment fragment = fragmentWeakReference.get();
            if (fragment != null) {
                fragment.getPullToRefreshListView().setVisibility(View.INVISIBLE);
            }
        }

        @Override
        protected List<MessageTimeLineData> doInBackground(Void... params) {
            MessageTimeLineData recentGroupData = FriendsTimeLineDBTask
                    .getRecentGroupData(accountId);
            publishProgress(recentGroupData);
            return FriendsTimeLineDBTask.getOtherGroupData(accountId, recentGroupData.groupId);
        }

        @Override
        protected void onPostExecute(List<MessageTimeLineData> result) {
            super.onPostExecute(result);
            FriendsTimeLineFragment fragment = fragmentWeakReference.get();

            if (fragment == null) {
                return;
            }

            if (fragment.getActivity() == null) {
                return;
            }

            if (result != null && result.size() > 0) {
                fragment.handleDBCacheResultData(result);
            }
        }

        @Override
        protected void onProgressUpdate(MessageTimeLineData... result) {
            super.onProgressUpdate(result);
            FriendsTimeLineFragment fragment = fragmentWeakReference.get();
            if (fragment == null) {
                return;
            }
            if (fragment.getActivity() == null) {
                return;
            }

        }
    }
}
