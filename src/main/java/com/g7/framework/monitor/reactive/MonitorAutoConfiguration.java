package com.g7.framework.monitor.reactive;

import com.ctrip.framework.apollo.spring.annotation.EnableApolloConfig;
import com.g7.framework.monitor.reactive.filter.ParamWebFilter;
import com.g7.framework.monitor.reactive.handler.GlobalExceptionHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * @author dreamyao
 * @title
 * @date 2018/9/14 下午11:30
 * @since 1.0.0
 */
@EnableWebMvc
@EnableApolloConfig
@EnableEurekaClient
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class MonitorAutoConfiguration {

    @Bean
    @Primary
    @Order(-1)
    @ConditionalOnMissingBean(value = GlobalExceptionHandler.class)
    public GlobalExceptionHandler errorWebExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    @Bean
    @ConditionalOnMissingBean
    public ParamWebFilter paramWebFilter() {
        return new ParamWebFilter();
    }
}
