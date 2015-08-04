package com.ysy.ysywb.support.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.ysy.ysywb.bean.MessageBean;
import com.ysy.ysywb.bean.MessageListBean;
import com.ysy.ysywb.bean.android.MessageTimeLineData;
import com.ysy.ysywb.bean.android.TimeLinePosition;
import com.ysy.ysywb.support.database.table.HomeOtherGroupTable;
import com.ysy.ysywb.support.debug.AppLogger;
import com.ysy.ysywb.support.utils.AppConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * User: ysy
 * Date: 2015/8/4
 */
public class HomeOtherGroupTimeLineDBTask {
    private HomeOtherGroupTimeLineDBTask() {

    }

    private static SQLiteDatabase getWsd() {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getWritableDatabase();
    }

    private static SQLiteDatabase getRsd() {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getReadableDatabase();
    }

    static MessageTimeLineData getTimeLineData(String accountId, String groupId) {
        TimeLinePosition position = getPosition(accountId, groupId);
        MessageListBean msgList = get(accountId, groupId,
                position.position + AppConfig.DB_CACHE_COUNT_OFFSET);
        return new MessageTimeLineData(groupId, msgList, position);
    }

    public static TimeLinePosition getPosition(String accountId, String groupId) {
        String sql = "select * from " + HomeOtherGroupTable.TABLE_NAME + " where "
                + HomeOtherGroupTable.ACCOUNTID + "  = "
                + accountId + " and " + HomeOtherGroupTable.GROUPID + " = " + groupId;
        Cursor c = getRsd().rawQuery(sql, null);
        Gson gson = new Gson();
        while (c.moveToNext()) {
            String json = c.getString(c.getColumnIndex(HomeOtherGroupTable.TIMELINEDATA));
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

    static MessageListBean get(String accountId, String groupId, int limitCount) {
        Gson gson = new Gson();
        MessageListBean result = new MessageListBean();
        int limit = limitCount > AppConfig.DEFAULT_MSG_COUNT_50 ? limitCount
                : AppConfig.DEFAULT_MSG_COUNT_50;
        List<MessageBean> msgList = new ArrayList<MessageBean>();
        String sql = "select * from " + HomeOtherGroupTable.HomeOtherGroupDataTable.TABLE_NAME
                + " where " + HomeOtherGroupTable.HomeOtherGroupDataTable.ACCOUNTID + "  = "
                + accountId + " and " + HomeOtherGroupTable.HomeOtherGroupDataTable.GROUPID + " =  "
                + groupId + " order by " + HomeOtherGroupTable.HomeOtherGroupDataTable.ID
                + " asc limit " + limit;
        Cursor c = getRsd().rawQuery(sql, null);
        while (c.moveToNext()) {
            String json = c.getString(
                    c.getColumnIndex(HomeOtherGroupTable.HomeOtherGroupDataTable.JSONDATA));
            if (!TextUtils.isEmpty(json)) {
                try {
                    MessageBean value = gson.fromJson(json, MessageBean.class);
                    if (!value.isMiddleUnreadItem()) {
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
        for (int i = msgList.size() - 1; i >= 0; i--) {
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
}
