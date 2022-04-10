package com.g7.framework.monitor.reactive.robot.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author dreamyao
 * @title
 * @date 2019-05-23 14:29
 * @since 1.0.0
 */
@ConfigurationProperties(prefix = "robot.webhook")
public class RobotProperties {

    /**
     * webhook地址（从飞书机器人获取）
     */
    private String url;

    /**
     * 是否启动新版本发送消息
     */
    private Boolean openNewVersion = false;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Boolean getOpenNewVersion() {
        return openNewVersion;
    }

    public void setOpenNewVersion(Boolean openNewVersion) {
        this.openNewVersion = openNewVersion;
    }
}
