package com.g7.framework.monitor.reactive.robot.text;

/**
 * @author dreamyao
 * @title 新版文本对象
 * @date 2020-04-02 14:57
 * @since 1.0.0
 */
public class TextNotification extends NotificationV2 {

    private static final long serialVersionUID = 8994650791300573412L;

    /**
     * 告警内容
     */
    private BaseNotificationV2 content;

    public BaseNotificationV2 getContent() {
        return content;
    }

    public void setContent(BaseNotificationV2 content) {
        this.content = content;
    }
}
