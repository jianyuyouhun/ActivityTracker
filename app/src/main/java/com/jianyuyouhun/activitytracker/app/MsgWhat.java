package com.jianyuyouhun.activitytracker.app;

/**
 * 消息what
 * Created by wangyu on 2017/9/12.
 */

public enum MsgWhat {

    RECEIVE_INFO_MSG_WHAT(1),
    RECEIVE_SERVICE_COMMAND(2);

    int value;

    MsgWhat(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
