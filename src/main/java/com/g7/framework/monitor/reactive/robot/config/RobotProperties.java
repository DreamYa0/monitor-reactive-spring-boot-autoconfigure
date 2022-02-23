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
     * 启用状态：true=启用,false=不启用
     */
    private Boolean enabled = true;

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

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public Boolean getOpenNewVersion() {
        return openNewVersion;
    }

    public void setOpenNewVersion(Boolean openNewVersion) {
        this.openNewVersion = openNewVersion;
    }
}
