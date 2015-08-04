package com.ysy.ysywb.ui.basefragment;

import com.ysy.ysywb.bean.ListBean;
import com.ysy.ysywb.bean.MessageBean;
import com.ysy.ysywb.ui.interfaces.IRemoveItem;

/**
 * User: ysy
 * Date: 2015/8/4
 */
public abstract class AbstractMessageTimeLineFragment<T extends ListBean<MessageBean, ?>>
        extends AbstractTimeLineFragment<T> implements IRemoveItem {
    @Override
    public void removeItem(int position) {

    }

    @Override
    public void removeCancel() {

    }
}
