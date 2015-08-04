package com.ysy.ysywb.ui.basefragment;

import android.widget.BaseAdapter;
import android.widget.ListView;

import com.ysy.ysywb.bean.ListBean;
import com.ysy.ysywb.support.lib.TopTipBar;
import com.ysy.ysywb.support.lib.pulltorefresh.PullToRefreshListView;
import com.ysy.ysywb.ui.interfaces.AbstractAppFragment;

/**
 * User: ysy
 * Date: 2015/8/4
 */
public class AbstractTimeLineFragment<T extends ListBean> extends AbstractAppFragment {

    protected PullToRefreshListView pullToRefreshListView;

    protected BaseAdapter timeLineAdapter;

    protected TopTipBar newMsgTipBar;

    public ListView getListView() {
        return pullToRefreshListView.getRefreshableView();
    }

    public PullToRefreshListView getPullToRefreshListView() {
        return pullToRefreshListView;
    }



}
