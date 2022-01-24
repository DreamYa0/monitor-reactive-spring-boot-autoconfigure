package com.g7.framework.monitor.reactive;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author dreamyao
 * @title 开启Cat监控
 * @date 2019/3/5 11:42 AM
 * @since 1.0.0
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Configuration
@Import(MonitorReactiveAutoConfiguration.class)
public @interface EnableReactiveMonitor {

}
