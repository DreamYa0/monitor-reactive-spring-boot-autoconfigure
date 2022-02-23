package com.g7.framework.monitor.reactive.robot.richtext;

import java.io.Serializable;

/**
 * @author luox
 * @title TODO
 * @date 2021/3/18 11:44 上午
 * @since 1.0.0
 */
public class RichTextNotificationLanguageContent implements Serializable {

    private static final long serialVersionUID = -5263063541347612174L;

    /**
     * 内容类型 （text=文本,a=超链接）
     */
    private String tag;

    /**
     * 内容
     */
    private String text;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }
}
