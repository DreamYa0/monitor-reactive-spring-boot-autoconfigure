package com.g7.framework.monitor.reactive.robot.richtext;

import java.io.Serializable;

/**
 * @author luox
 * @title TODO
 * @date 2021/3/18 11:44 上午
 * @since 1.0.0
 */
public class RichTextNotificationPost implements Serializable {

    private static final long serialVersionUID = -5363263541347612174L;

    /**
     * 中文内容
     */
    private RichTextNotificationLanguage zhCn;

    public RichTextNotificationLanguage getZhCn() {
        return zhCn;
    }

    public void setZhCn(RichTextNotificationLanguage zhCn) {
        this.zhCn = zhCn;
    }
}
