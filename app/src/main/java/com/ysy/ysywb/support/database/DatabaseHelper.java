package com.ysy.ysywb.support.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ysy.ysywb.support.database.table.AccountTable;
import com.ysy.ysywb.support.utils.GlobalContext;

/**
 * Created by ggec5486 on 2015/6/11.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper singleton = null;

    private static final String DATABASE_NAME = "weibo.db";

    private static final int DATABASE_VERSION = 37;

    static final String CREATE_ACCOUNT_TABLE_SQL = "create table " + AccountTable.TABLE_NAME
            + "("
            + AccountTable.UID + " integer primary key autoincrement,"
            + AccountTable.OAUTH_TOKEN + " text,"
            + AccountTable.OAUTH_TOKEN_EXPIRES_TIME + " text,"
            + AccountTable.OAUTH_TOKEN_SECRET + " text,"
            + AccountTable.BLACK_MAGIC + " boolean,"
            + AccountTable.NAVIGATION_POSITION + " integer,"
            + AccountTable.INFOJSON + " text"
            + ");";



    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ACCOUNT_TABLE_SQL);

       // createOtherTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static synchronized DatabaseHelper getInstance() {
        if (singleton == null) {
            singleton = new DatabaseHelper(GlobalContext.getInstance());
        }
        return singleton;
    }

}
