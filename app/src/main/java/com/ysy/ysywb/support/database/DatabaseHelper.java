package com.ysy.ysywb.support.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.ysy.ysywb.support.database.dbUpgrade.Upgrade35to36;
import com.ysy.ysywb.support.database.dbUpgrade.Upgrade36to37;
import com.ysy.ysywb.support.database.table.AccountTable;
import com.ysy.ysywb.support.database.table.AtUsersTable;
import com.ysy.ysywb.support.database.table.DownloadPicturesTable;
import com.ysy.ysywb.support.database.table.NotificationTable;
import com.ysy.ysywb.support.utils.GlobalContext;

/**
 * Created by ggec5486 on 2015/6/11.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static DatabaseHelper singleton = null;

    private static final String DATABASE_NAME = "weibo.db";

    private static final int DATABASE_VERSION = 37;

    private static final String CREATE_NOTIFICATION_TABLE_SQL = "create table "
            + NotificationTable.TABLE_NAME
            + "("
            + NotificationTable.ID + " integer ,"
            + NotificationTable.ACCOUNTID + " text,"
            + NotificationTable.MSGID + " text,"
            + NotificationTable.TYPE + " text,"
            + "primary key (" + NotificationTable.ACCOUNTID + "," + NotificationTable.MSGID + ","
            + NotificationTable.TYPE + ")"
            + ");";

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

    public static final String CREATE_DOWNLOAD_PICTURES_TABLE_SQL = "create table "
            + DownloadPicturesTable.TABLE_NAME
            + "("
            + DownloadPicturesTable.ID + " integer,"
            + DownloadPicturesTable.URL + " text primary key,"
            + DownloadPicturesTable.PATH + " text,"
            + DownloadPicturesTable.SIZE + " integer,"
            + DownloadPicturesTable.TIME + " integer,"
            + DownloadPicturesTable.TYPE + " integer"
            + ");";


    public static final String CREATE_ATUSERS_TABLE_SQL = "create table " + AtUsersTable.TABLE_NAME
            + "("
            + AtUsersTable.ID + " integer,"
            + AtUsersTable.USERID + " text,"
            + AtUsersTable.ACCOUNTID + " text,"
            + AtUsersTable.JSONDATA + " text,"
            + "primary key (" + AtUsersTable.USERID + "," + AtUsersTable.ACCOUNTID + ")"
            + ");";

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_ACCOUNT_TABLE_SQL);
        db.execSQL(CREATE_DOWNLOAD_PICTURES_TABLE_SQL);
        // createOtherTable(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        db.execSQL(CREATE_DOWNLOAD_PICTURES_TABLE_SQL);

        if (oldVersion <= 36) {
            Upgrade36to37.upgrade(db);
        }

        if (oldVersion <= 35) {
            Upgrade35to36.upgrade(db);
        }

        if (oldVersion <= 34) {
            upgrade34To35(db);
        }

        if (oldVersion <= 33) {
            deleteAllTable(db);
            onCreate(db);
        }
    }

    public static synchronized DatabaseHelper getInstance() {
        if (singleton == null) {
            singleton = new DatabaseHelper(GlobalContext.getInstance());
        }
        return singleton;
    }

    private void deleteAllTableExceptAccount(SQLiteDatabase db) {

        db.execSQL("DROP TABLE IF EXISTS " + NotificationTable.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + DownloadPicturesTable.TABLE_NAME);

    }

    private void deleteAllTable(SQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS " + AccountTable.TABLE_NAME);

        deleteAllTableExceptAccount(db);

    }


    private void upgrade34To35(SQLiteDatabase db) {
        db.execSQL(CREATE_NOTIFICATION_TABLE_SQL);
    }

}
