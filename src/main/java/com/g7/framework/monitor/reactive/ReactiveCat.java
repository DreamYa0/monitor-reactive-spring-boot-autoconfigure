package com.g7.framework.monitor.reactive;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Trace;
import com.dianping.cat.message.Transaction;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.util.function.Supplier;

/**
 * @author dreamyao
 * @title 业务包装埋点监控
 * @date 2019/11/9 4:39 PM
 * @since 1.0.0
 */
public abstract class ReactiveCat {

    /**
     * 适合记录跨越系统边界的程序访问行为,比如远程调用，数据库调用，也适合执行时间较长的业务逻辑监控，
     * Transaction用来记录一段代码的执行时间和次数
     * @param name     埋点监控业务名称
     * @param supplier 生产者
     * @param <T>      类型
     * @return T
     */
    public static <T> Mono<T> transaction(String type, String name, Supplier<T> supplier) {
        return Mono.create(sink -> {
            Transaction transaction = Cat.newTransaction(type, name);
            doSupplier(sink, transaction, supplier);
        });
    }

    /**
     * 用来记录一件事发生的次数，比如记录系统异常，它和transaction相比缺少了时间的统计，开销比transaction要小
     * @param name     埋点监控业务名称
     * @param supplier 生产者
     * @param <T>      类型
     * @return T
     */
    public static <T> Mono<T> event(String type, String name, Supplier<T> supplier) {
        return Mono.create(sink -> {
            Event event = Cat.newEvent(type, name);
            doSupplier(sink, event, supplier);
        });
    }

    /**
     * 用于记录基本的trace信息，类似于log4j的info信息，这些信息仅用于查看一些相关信息
     * @param name     埋点监控业务名称
     * @param supplier 生产者
     * @param <T>      类型
     * @return T
     */
    public static <T> Mono<T> trace(String type, String name, Supplier<T> supplier) {
        return Mono.create(sink -> {
            Trace trace = Cat.newTrace(type, name);
            doSupplier(sink, trace, supplier);
        });
    }

    private static <T> void doSupplier(MonoSink<T> sink, Message message, Supplier<T> supplier) {
        try {
            T t = supplier.get();
            message.setStatus(Transaction.SUCCESS);
            sink.success(t);
        } catch (Exception e) {
            Cat.logError(e);
            sink.error(e);
        } finally {
            message.complete();
        }
    }
}
