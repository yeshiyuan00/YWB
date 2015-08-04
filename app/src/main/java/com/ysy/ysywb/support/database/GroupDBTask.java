package com.ysy.ysywb.support.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.ysy.ysywb.bean.GroupListBean;
import com.ysy.ysywb.support.database.table.GroupTable;

/**
 * User: ysy
 * Date: 2015/8/4
 */
public class GroupDBTask {
    private GroupDBTask() {

    }

    private static SQLiteDatabase getWsd() {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getWritableDatabase();
    }

    private static SQLiteDatabase getRsd() {
        DatabaseHelper databaseHelper = DatabaseHelper.getInstance();
        return databaseHelper.getReadableDatabase();
    }


    public static GroupListBean get(String accountId) {
        String sql = "select * from " + GroupTable.TABLE_NAME + " where " + GroupTable.ACCOUNTID
                + "  = "
                + accountId;
        Cursor c = getRsd().rawQuery(sql, null);
        if (c.moveToNext()) {

            String json = c.getString(c.getColumnIndex(GroupTable.JSONDATA));
            if (!TextUtils.isEmpty(json)) {
                GroupListBean bean = new Gson().fromJson(json, GroupListBean.class);
                if (bean != null) {
                    return bean;
                }
            }
        }
        return null;
    }
}
