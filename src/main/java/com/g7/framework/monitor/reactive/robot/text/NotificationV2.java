package com.g7.framework.monitor.reactive.robot.text;

import java.io.Serializable;

/**
 * @author luox
 * @title 机器人通知对象对象新版
 * @date 2020-04-02 14:55
 * @since 1.0.0
 */
public class NotificationV2 implements Serializable {

    private static final long serialVersionUID = 6424293265824427834L;

    /**
     * 消息类型:text（文本）/ post（富文本）/ image（图片）/ share_chat（分享群名片）/ interactive（消息卡片）
     */
    private String msg_type;


    public String getMsg_type() {
        return msg_type;
    }

    public void setMsg_type(String msg_type) {
        this.msg_type = msg_type;
    }
}
