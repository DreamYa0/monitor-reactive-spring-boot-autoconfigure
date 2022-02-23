package com.g7.framework.monitor.reactive.robot.text;

import java.io.Serializable;

/**
 * @author dreamyao
 * @title
 * @date 2020-04-02 14:57
 * @since 1.0.0
 */
public class BaseNotification implements Serializable {

    private static final long serialVersionUID = -5363063541347612174L;

    /**
     * 告警内容
     */
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
