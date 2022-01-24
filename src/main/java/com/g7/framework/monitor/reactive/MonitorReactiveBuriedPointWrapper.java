package com.g7.framework.monitor.reactive;

import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Trace;
import com.dianping.cat.message.Transaction;

import java.util.function.Supplier;

/**
 * @author dreamyao
 * @title 业务包装埋点监控
 * @date 2019/11/9 4:39 PM
 * @since 1.0.0
 */
public abstract class MonitorReactiveBuriedPointWrapper {

    /**
     * 适合记录跨越系统边界的程序访问行为,比如远程调用，数据库调用，也适合执行时间较长的业务逻辑监控，
     * Transaction用来记录一段代码的执行时间和次数
     * @param businessName 埋点监控业务名称
     * @param supplier     生产者
     * @param <T>          类型
     * @return T
     */
    public static <T> T transactionWrapper(String businessName, Supplier<T> supplier) {
        Transaction transaction = Cat.newTransaction("Transaction", businessName);
        try {
            T t = supplier.get();
            transaction.setStatus(Transaction.SUCCESS);
            return t;
        } catch (Exception e) {
            Cat.logError(e);
            throw e;
        } finally {
            transaction.complete();
        }
    }

    /**
     * 用来记录一件事发生的次数，比如记录系统异常，它和transaction相比缺少了时间的统计，开销比transaction要小
     * @param businessName 埋点监控业务名称
     * @param supplier 生产者
     * @param <T> 类型
     * @return T
     */
    public static <T> T eventWrapper(String businessName, Supplier<T> supplier) {
        Event event = Cat.newEvent("Event", businessName);
        try {
            T t = supplier.get();
            event.setStatus(Transaction.SUCCESS);
            return t;
        } catch (Exception e) {
            Cat.logError(e);
            throw e;
        } finally {
            event.complete();
        }
    }

    /**
     * 用于记录基本的trace信息，类似于log4j的info信息，这些信息仅用于查看一些相关信息
     * @param businessName 埋点监控业务名称
     * @param supplier 生产者
     * @param <T> 类型
     * @return T
     */
    public static <T> T traceWrapper(String businessName, Supplier<T> supplier) {
        Trace trace = Cat.newTrace("Trace", businessName);
        try {
            T t = supplier.get();
            trace.setStatus(Transaction.SUCCESS);
            return t;
        } catch (Exception e) {
            Cat.logError(e);
            throw e;
        } finally {
            trace.complete();
        }
    }
}
