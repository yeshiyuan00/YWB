package com.ysy.ysywb.support.settinghelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by ggec5486 on 2015/7/23.
 */
public class SettingHelper {

    private static SharedPreferences sharedPreferences = null;

    public static Boolean getSharedPreferences(Context paramContext, String paramString,
                                               Boolean paramBoolean) {
        return getSharedPreferencesObject(paramContext).getBoolean(paramString, paramBoolean);
    }

    private static SharedPreferences getSharedPreferencesObject(Context paramContext) {
        if (sharedPreferences == null) {
            sharedPreferences = PreferenceManager.getDefaultSharedPreferences(paramContext);
        }
        return sharedPreferences;
    }
}
