package com.ysy.ysywb.support.utils;

import com.ysy.ysywb.bean.AccountBean;
import com.ysy.ysywb.bean.UserBean;

/**
 * Created by ggec5486 on 2015/6/9.
 */
public class ObjectToStringUtility {

    public static String toString(AccountBean account) {
        return account.getUsernick();
    }

    public static String toString(UserBean bean) {
        return "user id=" + bean.getId()
                + "," + "name=" + bean.getScreen_name();
    }
}
