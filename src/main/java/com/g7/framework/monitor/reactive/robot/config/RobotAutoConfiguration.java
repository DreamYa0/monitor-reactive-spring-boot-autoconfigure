package com.g7.framework.monitor.reactive.robot.config;

import com.g7.framework.monitor.reactive.RobotNotificationService;
import com.g7.framework.reactive.common.util.http.ReactiveRest;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author dreamyao
 */
@AutoConfiguration
@EnableConfigurationProperties({RobotProperties.class})
public class RobotAutoConfiguration {

    private final RobotProperties robotProperties;
    private final ReactiveRest reactiveRest;

    public RobotAutoConfiguration(RobotProperties robotProperties, ReactiveRest reactiveRest) {
        this.robotProperties = robotProperties;
        this.reactiveRest = reactiveRest;
    }

    @Bean
    @ConditionalOnMissingBean(value = RobotNotificationService.class)
    public RobotNotificationService robotNotificationService() {
        return new RobotNotificationService(robotProperties, reactiveRest);
    }
}