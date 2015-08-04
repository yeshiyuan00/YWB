package com.ysy.ysywb.bean.android;

import com.ysy.ysywb.bean.MessageListBean;

/**
 * User: ysy
 * Date: 2015/8/4
 */
public class MessageTimeLineData {
    public MessageListBean msgList;
    public TimeLinePosition position;
    public String groupId;

    public MessageTimeLineData(String groupId, MessageListBean msgList, TimeLinePosition position) {
        this.groupId = groupId;
        this.msgList = msgList;
        this.position = position;
    }
}
