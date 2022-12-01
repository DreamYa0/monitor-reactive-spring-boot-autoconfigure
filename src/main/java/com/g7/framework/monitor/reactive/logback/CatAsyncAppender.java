package com.g7.framework.monitor.reactive.logback;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import com.dianping.cat.Cat;
import com.g7.framework.framwork.exception.BusinessException;
import org.springframework.util.StringUtils;

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
            this.logError(eventObject);
        }
    }

    private void logError(ILoggingEvent event) {
        ThrowableProxy info = (ThrowableProxy) event.getThrowableProxy();
        if (info != null) {
            Throwable throwable = info.getThrowable();
            if (throwable instanceof BusinessException) {
                BusinessException businessException = (BusinessException) throwable;
                final String code = businessException.getErrorCode();
                if (StringUtils.hasText(code) && code.length() > 4) {
                    // 业务异常不记录到CAT
                    return;
                }
            }

            Object message = event.getFormattedMessage();
            String mdcString = "";
            if (event.getMDCPropertyMap() != null) {
                mdcString = event.getMDCPropertyMap().toString();
            }
            if (message != null) {
                Cat.logError(mdcString + " " + message, throwable);
            } else {
                Cat.logError(mdcString, throwable);
            }
        }
    }
}
