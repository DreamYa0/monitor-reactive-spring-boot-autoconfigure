package com.g7.framework.monitor.reactive;

import com.g7.framework.monitor.reactive.webflux.CatWebFluxWebFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MonitorReactiveAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MonitorReactiveAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public CatWebFluxWebFilter catWebFluxWebFilter() {
        return new CatWebFluxWebFilter();
    }
}
