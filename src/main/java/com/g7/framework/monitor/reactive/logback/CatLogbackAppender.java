package com.g7.framework.monitor.reactive.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.LogbackException;
import com.dianping.cat.Cat;

/**
 * @author dreamyao
 */
public class CatLogbackAppender extends AppenderBase<ILoggingEvent> {

    @Override
    protected void append(ILoggingEvent event) {
        try {
            Level level = event.getLevel();
            if (level.isGreaterOrEqual(Level.ERROR)) {
                this.logError(event);
            }

        } catch (Exception var3) {
            throw new LogbackException(event.getFormattedMessage(), var3);
        }
    }

    private void logError(ILoggingEvent event) {
        ThrowableProxy info = (ThrowableProxy)event.getThrowableProxy();
        if (info != null) {
            Throwable exception = info.getThrowable();
            Object message = event.getFormattedMessage();
            String mdcString = "";
            if (event.getMDCPropertyMap() !=  null) {
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
