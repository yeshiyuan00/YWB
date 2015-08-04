package com.ysy.ysywb.support.utils;

import android.graphics.Bitmap;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.text.style.URLSpan;
import android.text.util.Linkify;

import com.ysy.ysywb.bean.MessageBean;
import com.ysy.ysywb.bean.UserBean;
import com.ysy.ysywb.support.lib.MyURLSpan;
import com.ysy.ysywb.support.lib.WeiboPatterns;

import java.util.regex.Matcher;

/**
 * User: ysy
 * Date: 2015/8/4
 */
public class TimeLineUtility {

    private TimeLineUtility() {
    }

    public static void addJustHighLightLinks(MessageBean bean) {
        bean.setListViewSpannableString(convertNormalStringToSpannableString(bean.getText()));
        bean.getSourceString();

        if (bean.getRetweeted_status() != null) {
            bean.getRetweeted_status().setListViewSpannableString(
                    buildOriWeiboSpannalString(bean.getRetweeted_status()));
            bean.getRetweeted_status().getSourceString();
        }
    }

    private static SpannableString buildOriWeiboSpannalString(MessageBean oriMsg) {
        String name = "";
        UserBean oriUser = oriMsg.getUser();
        if (oriUser != null) {
            name = oriUser.getScreen_name();
            if (TextUtils.isEmpty(name)) {
                name = oriUser.getId();
            }
        }

        SpannableString value;

        if (!TextUtils.isEmpty(name)) {
            value = TimeLineUtility
                    .convertNormalStringToSpannableString("@" + name + "：" + oriMsg.getText());
        } else {
            value = TimeLineUtility.convertNormalStringToSpannableString(oriMsg.getText());
        }
        return value;
    }

    public static SpannableString convertNormalStringToSpannableString(String txt) {
        //hack to fix android imagespan bug,see http://stackoverflow.com/questions/3253148/imagespan-is-cut-off-incorrectly-aligned
        //if string only contains emotion tags,add a empty char to the end
        String hackTxt;
        if (txt.startsWith("[") && txt.endsWith("]")) {
            hackTxt = txt + " ";
        } else {
            hackTxt = txt;
        }
        SpannableString value = SpannableString.valueOf(hackTxt);
        Linkify.addLinks(value, WeiboPatterns.MENTION_URL, WeiboPatterns.MENTION_SCHEME);
        Linkify.addLinks(value, WeiboPatterns.WEB_URL, WeiboPatterns.WEB_SCHEME);
        Linkify.addLinks(value, WeiboPatterns.TOPIC_URL, WeiboPatterns.TOPIC_SCHEME);

        URLSpan[] urlSpans = value.getSpans(0, value.length(), URLSpan.class);
        MyURLSpan weiboSpan = null;
        for (URLSpan urlSpan : urlSpans) {
            weiboSpan = new MyURLSpan(urlSpan.getURL());
            int start = value.getSpanStart(urlSpan);
            int end = value.getSpanEnd(urlSpan);
            value.removeSpan(urlSpan);
            value.setSpan(weiboSpan, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        TimeLineUtility.addEmotions(value);
        return value;
    }

    private static void addEmotions(SpannableString value) {
        Matcher localMatcher = WeiboPatterns.EMOTION_URL.matcher(value);
        while (localMatcher.find()) {
            String str2 = localMatcher.group(0);
            int k = localMatcher.start();
            int m = localMatcher.end();
            if (m - k < 8) {
                Bitmap bitmap = GlobalContext.getInstance().getEmotionsPics().get(str2);
                if (bitmap == null) {
                    bitmap = GlobalContext.getInstance().getHuahuaPics().get(str2);
                }
                if (bitmap != null) {
                    ImageSpan localImageSpan = new ImageSpan(
                            GlobalContext.getInstance().getActivity(), bitmap,
                            ImageSpan.ALIGN_BASELINE);
                    value.setSpan(localImageSpan, k, m, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                }
            }
        }
    }
}
