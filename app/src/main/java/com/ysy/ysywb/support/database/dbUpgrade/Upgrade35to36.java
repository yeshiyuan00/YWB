package com.ysy.ysywb.support.database.dbUpgrade;

import android.database.sqlite.SQLiteDatabase;

import com.ysy.ysywb.support.database.DatabaseHelper;
import com.ysy.ysywb.support.database.table.AtUsersTable;


/**
 * User: qii
 * Date: 14-4-8
 */
public class Upgrade35to36 {

    public static void upgrade(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + AtUsersTable.TABLE_NAME);
        db.execSQL(DatabaseHelper.CREATE_ATUSERS_TABLE_SQL);
    }
}
