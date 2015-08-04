package com.ysy.ysywb.ui.basefragment;

import android.widget.ListView;

import com.ysy.ysywb.support.lib.pulltorefresh.PullToRefreshListView;
import com.ysy.ysywb.ui.interfaces.AbstractAppFragment;

/**
 * User: ysy
 * Date: 2015/8/4
 */
public class AbstractUserListFragment extends AbstractAppFragment {

    protected PullToRefreshListView pullToRefreshListView;

    public ListView getListView() {
        return pullToRefreshListView.getRefreshableView();
    }

}
