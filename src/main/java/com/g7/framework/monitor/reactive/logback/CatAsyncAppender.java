package com.g7.framework.monitor.reactive.logback;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import com.dianping.cat.Cat;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * @author dreamyao
 * @title
 * @date 2022/3/29 2:00 PM
 * @since 1.0.0
 */
public class CatAsyncAppender extends AsyncAppender {

    @Override
    protected void preprocess(ILoggingEvent eventObject) {
        super.preprocess(eventObject);
        Level level = eventObject.getLevel();
        if (level.isGreaterOrEqual(Level.ERROR)) {
            Mono.just(eventObject)
                    .doOnNext(this::logError)
                    .subscribeOn(Schedulers.parallel())
                    .subscribe();
        }
    }

    private void logError(ILoggingEvent event) {
        ThrowableProxy info = (ThrowableProxy) event.getThrowableProxy();
        if (info != null) {
            Throwable exception = info.getThrowable();
            Object message = event.getFormattedMessage();
            String mdcString = "";
            if (event.getMDCPropertyMap() != null) {
                mdcString = event.getMDCPropertyMap().toString();
            }
            if (message != null) {
                Cat.logError(mdcString + " " + message, exception);
            } else {
                Cat.logError(mdcString, exception);
            }
        }
    }
}
