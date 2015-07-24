package com.ysy.ysywb.support.settinghelper;

import android.content.Context;

import com.ysy.ysywb.support.utils.GlobalContext;
import com.ysy.ysywb.ui.preference.SettingActivity;

/**
 * Created by ggec5486 on 2015/7/23.
 */
public class SettingUtility {
    public static boolean getEnableBigAvatar() {
        return SettingHelper.getSharedPreferences(getContext(), SettingActivity.SHOW_BIG_AVATAR, false);
    }

    private static Context getContext() {
        return GlobalContext.getInstance();
    }
}
