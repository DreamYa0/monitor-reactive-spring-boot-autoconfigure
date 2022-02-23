package com.g7.framework.monitor.reactive.robot.richtext;

import com.g7.framework.monitor.reactive.robot.text.NotificationV2;

/**
 * @author luox
 * @title 富文本框消息对象
 * @date 2020-04-02 14:57
 * @since 1.0.0
 */
public class RichTextNotification extends NotificationV2 {

    private static final long serialVersionUID = -7136314652949370089L;

    /**
     * 告警内容
     */
    private RichTextNotificationContent content;

    public RichTextNotificationContent getContent() {
        return content;
    }

    public void setContent(RichTextNotificationContent content) {
        this.content = content;
    }
}
