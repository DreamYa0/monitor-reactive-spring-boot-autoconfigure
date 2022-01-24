package com.g7.framework.monitor.reactive.webflux;

import com.dianping.cat.Cat;
import com.dianping.cat.CatConstants;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import org.springframework.boot.web.reactive.filter.OrderedWebFilter;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.Objects;

/**
 * @author dreamyao
 * @title
 * @date 2022/1/22 5:27 下午
 * @since 1.0.0
 */
public class CatWebFluxWebFilter implements OrderedWebFilter {

    @Override
    public int getOrder() {
        return 0;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {

        final ServerHttpRequest request = exchange.getRequest();
        final String url = request.getPath().contextPath().value();
        Transaction t = Cat.newTransaction(CatConstants.TYPE_URL, url);

        try {

            final HttpMethod method = request.getMethod();

            Cat.logEvent("Service.method", Objects.nonNull(method) ? method.name() : "UNKNOW",
                    Message.SUCCESS, url);

            final InetSocketAddress remoteAddress = request.getRemoteAddress();
            if (Objects.nonNull(remoteAddress)) {
                Cat.logEvent("Service.client", remoteAddress.getHostString());
            }
            final Mono<Void> mono = chain.filter(exchange);
            t.setStatus(Transaction.SUCCESS);
            return mono;

        } finally {
            t.complete();
        }
    }
}
