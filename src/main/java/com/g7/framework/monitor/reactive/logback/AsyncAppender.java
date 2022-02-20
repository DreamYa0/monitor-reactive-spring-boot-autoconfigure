/*
 * Copyright (c) 2011-2017 Pivotal Software Inc, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.g7.framework.monitor.reactive.logback;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.ThrowableProxy;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.LogbackException;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.*;
import com.dianping.cat.Cat;
import org.reactivestreams.Subscription;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.extra.processor.WorkQueueProcessor;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * A Logback {@literal Appender} implementation that uses a Reactor {@link
 * reactor.extra.processor.TopicProcessor} internally to queue events to a single-writer
 * thread. This implementation doesn't do any actually appending itself, it just delegates
 * to a "real" appender but it uses the efficient queueing mechanism of the {@literal
 * RingBuffer} to do so.
 * @author Jon Brisbin
 * @author Stephane Maldini
 */
public class AsyncAppender extends ContextAwareBase
        implements Appender<ILoggingEvent>, AppenderAttachable<ILoggingEvent>,
        CoreSubscriber<ILoggingEvent> {

    private final AppenderAttachableImpl<ILoggingEvent> aai = new AppenderAttachableImpl<>();
    private final FilterAttachableImpl<ILoggingEvent> fai = new FilterAttachableImpl<>();
    private final AtomicReference<Appender<ILoggingEvent>> delegate = new AtomicReference<>();
    private String name;
    private WorkQueueProcessor<ILoggingEvent> processor;
    private int backlog = 1024 * 1024;
    private boolean includeCallerData = false;
    private boolean started = false;
    private static final Scheduler SCHEDULER = Schedulers.parallel();

    public int getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public boolean isIncludeCallerData() {
        return includeCallerData;
    }

    public void setIncludeCallerData(final boolean includeCallerData) {
        this.includeCallerData = includeCallerData;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public void doAppend(ILoggingEvent evt) throws LogbackException {
        if (getFilterChainDecision(evt) == FilterReply.DENY) {
            return;
        }
        evt.prepareForDeferredProcessing();
        if (includeCallerData) {
            evt.getCallerData();
        }
        try {
            queueLoggingEvent(evt);
        } catch (Throwable t) {
            addError(t.getMessage(), t);
        }
    }

    @Override
    public void start() {
        startDelegateAppender();

        processor = WorkQueueProcessor.<ILoggingEvent>builder().name("logger")
                .bufferSize(backlog)
                .autoCancel(false)
                .build();
        processor.subscribe(this);
    }

    @Override
    public void onSubscribe(Subscription s) {
        try {
            doStart();
        } catch (Throwable t) {
            addError(t.getMessage(), t);
        } finally {
            started = true;
            s.request(Long.MAX_VALUE);
        }
    }

    @Override
    public void onNext(ILoggingEvent iLoggingEvent) {
        aai.appendLoopOnAppenders(iLoggingEvent);
    }

    @Override
    public void onError(Throwable t) {
        addError(t.getMessage(), t);
    }

    @Override
    public void onComplete() {
        try {
            Appender<ILoggingEvent> appender = delegate.getAndSet(null);
            if (appender != null) {
                doStop();
                appender.stop();
                aai.detachAndStopAllAppenders();
            }
        } catch (Throwable t) {
            addError(t.getMessage(), t);
        } finally {
            started = false;
        }
    }

    private void startDelegateAppender() {
        Appender<ILoggingEvent> delegateAppender = delegate.get();
        if (null != delegateAppender && !delegateAppender.isStarted()) {
            delegateAppender.start();
        }
    }

    @Override
    public void stop() {
        processor.onComplete();
    }

    @Override
    public void addFilter(Filter<ILoggingEvent> newFilter) {
        fai.addFilter(newFilter);
    }

    @Override
    public void clearAllFilters() {
        fai.clearAllFilters();
    }

    @Override
    public List<Filter<ILoggingEvent>> getCopyOfAttachedFiltersList() {
        return fai.getCopyOfAttachedFiltersList();
    }

    @Override
    public FilterReply getFilterChainDecision(ILoggingEvent event) {
        return fai.getFilterChainDecision(event);
    }

    @Override
    public void addAppender(Appender<ILoggingEvent> newAppender) {
        if (delegate.compareAndSet(null, newAppender)) {
            aai.addAppender(newAppender);
        } else {
            throw new IllegalArgumentException(delegate.get() + " already attached.");
        }
    }

    @Override
    public Iterator<Appender<ILoggingEvent>> iteratorForAppenders() {
        return aai.iteratorForAppenders();
    }

    @Override
    public Appender<ILoggingEvent> getAppender(String name) {
        return aai.getAppender(name);
    }

    @Override
    public boolean isAttached(Appender<ILoggingEvent> appender) {
        return aai.isAttached(appender);
    }

    @Override
    public void detachAndStopAllAppenders() {
        aai.detachAndStopAllAppenders();
    }

    @Override
    public boolean detachAppender(Appender<ILoggingEvent> appender) {
        return aai.detachAppender(appender);
    }

    @Override
    public boolean detachAppender(String name) {
        return aai.detachAppender(name);
    }

    protected AppenderAttachableImpl<ILoggingEvent> getAppenderImpl() {
        return aai;
    }

    protected void doStart() {
    }

    protected void doStop() {
    }

    private void queueLoggingEvent(ILoggingEvent evt) {
        if (null != delegate.get()) {
            processor.onNext(evt);
            Mono.create(sink -> sink.success(evt)).doOnNext(event -> {
                if (evt.getLevel().isGreaterOrEqual(Level.ERROR)) {
                    logError(evt);
                }
            }).subscribeOn(SCHEDULER).subscribe();

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
