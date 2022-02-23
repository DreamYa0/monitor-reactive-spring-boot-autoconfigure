package com.g7.framework.monitor.reactive.robot.text;

/**
 * @author dreamyao
 * @title
 * @date 2020-04-02 14:55
 * @since 1.0.0
 */
public class Notification extends BaseNotification {

    private static final long serialVersionUID = 6424293265824427833L;

    /**
     * 标题
     */
    private String title;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
