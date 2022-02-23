package com.g7.framework.monitor.reactive.robot.richtext;

import java.io.Serializable;
import java.util.List;

/**
 * @author luox
 * @title
 * @date 2021/3/18 11:44 上午
 * @since 1.0.0
 */
public class RichTextNotificationLanguage implements Serializable {

    private static final long serialVersionUID = -1740856196985405648L;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容集合
     */
    private List<List<RichTextNotificationLanguageContent>> content;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }


    public List<List<RichTextNotificationLanguageContent>> getContent() {
        return content;
    }

    public void setContent(List<List<RichTextNotificationLanguageContent>> content) {
        this.content = content;
    }
}
