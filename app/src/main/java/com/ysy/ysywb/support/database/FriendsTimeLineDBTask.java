package com.ysy.ysywb.support.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.ysy.ysywb.bean.GroupBean;
import com.ysy.ysywb.bean.GroupListBean;
import com.ysy.ysywb.bean.MessageBean;
import com.ysy.ysywb.bean.MessageListBean;
import com.ysy.ysywb.bean.android.MessageTimeLineData;
import com.ysy.ysywb.bean.android.TimeLinePosition;
import com.ysy.ysywb.support.database.table.HomeTable;
import com.ysy.ysywb.support.debug.AppLogger;
import com.ysy.ysywb.support.utils.AppConfig;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * User: ysy
 * Date: 2015/8/4
 */
public class FriendsTimeLineDBTask {

    /**
     * the number of messages to read is calculated by listview position, for example,
     * if you have 1000 messages, but the first position of listview is 60,
     * weiciyuan will save 1000 messages to database, but at the next time when
     * app need to read database, app will read only 60+ DB_CACHE_COUNT_OFFSET =70 messages.
     */

    private FriendsTimeLineDBTask() {

    }

    private static SQLiteDatabase getWsd() {
        DatabaseHelper databasehelper = DatabaseHelper.getInstance();
        return databasehelper.getWritableDatabase();
    }

    private static SQLiteDatabase getRsd() {
        DatabaseHelper databasehelper = DatabaseHelper.getInstance();
        return databasehelper.getReadableDatabase();
    }

    public static MessageTimeLineData getRecentGroupData(String accountId) {
        String groupId = getRecentGroupId(accountId);
        MessageListBean msgList;
        TimeLinePosition position;
        if (groupId.equals("0")) {
            position = getPosition(accountId);
            msgList = getHomeLineMsgList(accountId,
                    position.position + AppConfig.DB_CACHE_COUNT_OFFSET);
        } else {
            position = HomeOtherGroupTimeLineDBTask.getPosition(accountId, groupId);
            msgList = HomeOtherGroupTimeLineDBTask.get(accountId, groupId,
                    position.position + AppConfig.DB_CACHE_COUNT_OFFSET);
        }
        return new MessageTimeLineData(groupId, msgList, position);
    }

    private static MessageListBean getHomeLineMsgList(String accountId, int limitCount) {
        Gson gson = new Gson();
        MessageListBean result = new MessageListBean();
        int limit = limitCount > AppConfig.DEFAULT_MSG_COUNT_50 ? limitCount
                : AppConfig.DEFAULT_MSG_COUNT_50;
        List<MessageBean> msgList = new ArrayList<MessageBean>();
        String sql = "select * from " + HomeTable.HomeDataTable.TABLE_NAME +
                " where " + HomeTable.HomeDataTable.ACCOUNTID + "=" +
                accountId + " order by " + HomeTable.HomeDataTable.ID +
                " asc limit " + limit;
        Cursor c = getRsd().rawQuery(sql, null);
        while (c.moveToNext()) {
            String json = c.getString(c.getColumnIndex(HomeTable.HomeDataTable.JSONDATA));
            if (!TextUtils.isEmpty(json)) {
                try {
                    MessageBean value = gson.fromJson(json, MessageBean.class);
                    if (!value.isMiddleUnreadItem() && !TextUtils.isEmpty(value.getText())) {
                        value.getListViewSpannableString();
                    }
                    msgList.add(value);
                } catch (JsonSyntaxException e) {
                    AppLogger.e(e.getMessage());
                }
            } else {
                msgList.add(null);
            }
        }

        //delete the null flag at the head positon and the end position
        for (int i = 0; i < msgList.size(); i++) {
            if (msgList.get(i) == null) {
                msgList.remove(i);
            } else {
                break;
            }
        }

        for (int i = 0; i < msgList.size(); i++) {
            if (msgList.get(i) == null) {
                msgList.remove(i);
            } else {
                break;
            }
        }
        result.setStatuses(msgList);
        c.close();
        return result;
    }

    private static TimeLinePosition getPosition(String accountId) {
        String sql = "select * from " + HomeTable.TABLE_NAME + " where " + HomeTable.ACCOUNTID
                + "  = "
                + accountId;
        Cursor c = getRsd().rawQuery(sql, null);
        Gson gson = new Gson();
        while (c.moveToNext()) {

            String json = c.getString(c.getColumnIndex(HomeTable.TIMELINEDATA));
            if (!TextUtils.isEmpty(json)) {
                try {
                    TimeLinePosition value = gson.fromJson(json, TimeLinePosition.class);
                    c.close();
                    return value;
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            }
        }
        c.close();
        return TimeLinePosition.empty();
    }

    private static String getRecentGroupId(String accountId) {
        String sql = "select * from " + HomeTable.TABLE_NAME + " where " + HomeTable.ACCOUNTID
                + "=" + accountId;
        Cursor c = getRsd().rawQuery(sql, null);
        Gson gson = new Gson();
        while (c.moveToNext()) {
            String id = c.getString(c.getColumnIndex(HomeTable.RECENT_GROUP_ID));
            if (!TextUtils.isEmpty(id)) {
                return id;
            }
        }
        c.close();
        return "0";
    }

    public static List<MessageTimeLineData> getOtherGroupData(String accountId,
                                                              String exceptGroupId) {
        List<MessageTimeLineData> data = new ArrayList<MessageTimeLineData>();

        if (!"0".equals(exceptGroupId)) {
            TimeLinePosition position = getPosition(accountId);
            MessageListBean msgList = getHomeLineMsgList(accountId,
                    position.position + AppConfig.DB_CACHE_COUNT_OFFSET);
            MessageTimeLineData home = new MessageTimeLineData("0", msgList, position);
            data.add(home);
        }

        MessageTimeLineData biGroup = HomeOtherGroupTimeLineDBTask.getTimeLineData(accountId, "1");
        data.add(biGroup);

        GroupListBean groupListBean = GroupDBTask.get(accountId);

        if (groupListBean != null) {
            List<GroupBean> lists = groupListBean.getLists();
            for (GroupBean groupBean : lists) {
                MessageTimeLineData dbMsg = HomeOtherGroupTimeLineDBTask
                        .getTimeLineData(accountId, groupBean.getId());
                data.add(dbMsg);
            }
        }

        Iterator<MessageTimeLineData> iterator = data.iterator();
        while (iterator.hasNext()) {
            MessageTimeLineData single = iterator.next();
            if (single.groupId.equals(exceptGroupId)) {
                iterator.remove();
                break;
            }
        }

        return data;
    }


}
