package com.g7.framework.monitor.reactive.tracing;

import brave.Tracing;
import brave.propagation.StrictScopeDecorator;
import brave.propagation.ThreadLocalCurrentTraceContext;
import brave.rpc.RpcTracing;
import brave.sampler.Sampler;
import com.alibaba.dubbo.common.extension.ExtensionFactory;

/**
 * @author dreamyao
 * @title
 * @date 2022/1/26 5:57 下午
 * @since 1.0.0
 */
public class TracingExtensionFactory implements ExtensionFactory {

    @Override
    public <T> T getExtension(Class<T> type, String name) {
        if (type != RpcTracing.class) return null;
        return (T) RpcTracing.newBuilder(Tracing.newBuilder()
                .localServiceName("tracing")
                .currentTraceContext(ThreadLocalCurrentTraceContext.newBuilder()
                        .addScopeDecorator(StrictScopeDecorator.create())
                        .build())
                .sampler(Sampler.NEVER_SAMPLE)
                .build());
    }
}
