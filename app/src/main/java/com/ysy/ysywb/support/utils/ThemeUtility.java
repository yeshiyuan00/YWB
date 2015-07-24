package com.ysy.ysywb.support.utils;

import android.app.Activity;
import android.content.res.TypedArray;

/**
 * Created by ggec5486 on 2015/7/23.
 */
public class ThemeUtility {

    public static int getColor(int attr) {
        return getColor(GlobalContext.getInstance().getActivity(), attr);
    }

    public static int getColor(Activity activity, int attr) {
        int[] attrs = new int[]{attr};
        TypedArray ta = activity.obtainStyledAttributes(attrs);
        int color = ta.getColor(0, 430);
        ta.recycle();
        return color;
    }
}
