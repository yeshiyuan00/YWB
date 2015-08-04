package com.ysy.ysywb.support.utils;

import com.ysy.ysywb.bean.AccountBean;
import com.ysy.ysywb.bean.GeoBean;
import com.ysy.ysywb.bean.GroupBean;
import com.ysy.ysywb.bean.GroupListBean;
import com.ysy.ysywb.bean.MessageBean;
import com.ysy.ysywb.bean.MessageListBean;
import com.ysy.ysywb.bean.UserBean;

/**
 * Created by ggec5486 on 2015/6/9.
 */
public class ObjectToStringUtility {

    public static String toString(MessageListBean listBean) {
        StringBuilder builder = new StringBuilder();
        for (MessageBean data : listBean.getItemList()) {
            builder.append(data.toString());
        }
        return builder.toString();
    }

    public static String toString(MessageBean msg) {
        UserBean userBean = msg.getUser();
        String username = (userBean != null ? userBean.getScreen_name() : "user is null");
        return String.format("%s @%s:%s", TimeUtility.getListTime(msg.getMills()), username,
                msg.getText());
    }

    public static String toString(GeoBean bean) {
        double[] c = bean.getCoordinates();
        return "type=" + bean.getType() + "coordinates=" + "[" + c[0] + "," + c[1] + "]";
    }


    public static String toString(AccountBean account) {
        return account.getUsernick();
    }

    public static String toString(UserBean bean) {
        return "user id=" + bean.getId()
                + "," + "name=" + bean.getScreen_name();
    }

    public static String toString(GroupBean bean) {
        return "group id=" + bean.getIdstr() + "," + "name=" + bean.getName();
    }

    public static String toString(GroupListBean listBean) {
        StringBuilder builder = new StringBuilder();
        for (GroupBean data : listBean.getLists()) {
            builder.append(data.toString());
        }
        return builder.toString();
    }

}
