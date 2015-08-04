package com.ysy.ysywb.ui.search;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.util.SparseArray;

import com.ysy.ysywb.support.utils.Utility;
import com.ysy.ysywb.ui.basefragment.AbstractTimeLineFragment;
import com.ysy.ysywb.ui.basefragment.AbstractUserListFragment;
import com.ysy.ysywb.ui.interfaces.AbstractAppFragment;
import com.ysy.ysywb.ui.main.MainTimeLineActivity;

/**
 * User: ysy
 * Date: 2015/8/4
 */
public class SearchMainParentFragment extends AbstractAppFragment
        implements MainTimeLineActivity.ScrollableListFragment {

    private SparseArray<Fragment> searchFragments = new SparseArray<Fragment>();
    private ViewPager viewPager;

    public static SearchMainParentFragment newInstance() {
        SearchMainParentFragment fragment = new SearchMainParentFragment();
        fragment.setArguments(new Bundle());
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }


    @Override
    public void scrollToTop() {
        Fragment fragment = searchFragments.get(viewPager.getCurrentItem());
        if (fragment instanceof AbstractTimeLineFragment) {
            Utility.stopListViewScrollingAndScrollToTop(
                    ((AbstractTimeLineFragment) fragment).getListView());
        } else if (fragment instanceof AbstractUserListFragment) {
            Utility.stopListViewScrollingAndScrollToTop(
                    ((AbstractUserListFragment) fragment).getListView());
        }
    }
}
