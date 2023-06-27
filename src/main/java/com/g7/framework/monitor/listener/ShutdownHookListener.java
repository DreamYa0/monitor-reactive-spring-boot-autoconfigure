package com.g7.framework.monitor.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.netflix.eureka.serviceregistry.EurekaAutoServiceRegistration;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextClosedEvent;

/**
 * @author dreamyao
 * @title
 * @date 2023/6/27 15:09
 * @since 1.0.0
 */
public class ShutdownHookListener implements ApplicationListener<ContextClosedEvent> {

    private static final Logger logger = LoggerFactory.getLogger(ShutdownHookListener.class);

    private final EurekaAutoServiceRegistration eurekaAutoServiceRegistration;

    public ShutdownHookListener(EurekaAutoServiceRegistration eurekaAutoServiceRegistration) {
        this.eurekaAutoServiceRegistration = eurekaAutoServiceRegistration;
    }

    @Override
    public void onApplicationEvent(ContextClosedEvent event) {
        try {
            logger.info("starting eureka relieve register");
            eurekaAutoServiceRegistration.stop();
            Thread.sleep(30 * 1000);
            logger.info("eureka relieve register completed");
        } catch (InterruptedException e) {
            logger.error("eureka relieve register failed", e);
        }
    }
}
