package com.ysy.ysywb.dao;

import com.ysy.ysywb.support.utils.Utility;

/**
 * Created by ggec5486 on 2015/6/10.
 */
public class URLHelper {

    //base url
    private static final String URL_SINA_WEIBO = "https://api.weibo.com/2/";

    //login
    public static final String UID = URL_SINA_WEIBO + "account/get_uid.json";
    public static final String URL_OAUTH2_ACCESS_AUTHORIZE = "https://api.weibo.com/oauth2/authorize";

    //1065511513
    public static final String APP_KEY = Utility.rot47("`_edd``d`b");

    //df428e88aae8bd31f20481d149c856ed
    public static final String APP_SECRET = Utility.rot47("57cag6gg226g35b`7a_cg`5`ch4gde65");

    //https://api.weibo.com/oauth2/default.html
    public static final String DIRECT_URL = Utility
            .rot47("9EEADi^^2A:]H6:3@]4@>^@2FE9a^5672F=E]9E>=");
}
