package com.ysy.ysywb.support.utils;

import android.widget.AbsListView;

import java.lang.reflect.Field;

/**
 * User: ysy
 * Date: 2015/8/4
 */
public class JavaReflectionUtility {

    public static <T> T getValue(AbsListView view, String name) {
        final Field field;
        try {
            field = AbsListView.class.getDeclaredField(name);
            field.setAccessible(true);
            return (T) field.get(view);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;
    }
}
