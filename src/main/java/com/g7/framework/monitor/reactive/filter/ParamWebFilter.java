package com.g7.framework.monitor.reactive.filter;

import com.g7.framwork.common.util.json.JsonUtils;
import org.jetbrains.annotations.NotNull;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.util.MultiValueMap;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * @author dreamyao
 * @title 出入参记录过滤器
 * @date 2022/3/6 5:03 下午
 * @since 1.0.0
 */
public class ParamWebFilter implements WebFilter {

    private static final Logger logger = LoggerFactory.getLogger(ParamWebFilter.class);

    @NotNull
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, @NotNull WebFilterChain chain) {
        final ServerHttpRequest request = exchange.getRequest();
        final HttpMethod method = request.getMethod();
        final String path = request.getURI().getPath();
        if (Objects.isNull(method) || "/actuator/health".equalsIgnoreCase(path)) {
            // 未知请求类型或健康检查接口不打印出入参记录
            return chain.filter(exchange);
        }

        final long start = System.currentTimeMillis();

        switch (method) {

            case GET:

                ServerHttpRequestDecorator requestDecorator = new ServerHttpRequestDecorator(request) {
                    @NotNull
                    @Override
                    public MultiValueMap<String, String> getQueryParams() {
                        final MultiValueMap<String, String> params = super.getQueryParams();
                        logger.info("{} request is {}", path, JsonUtils.toJson(params.toSingleValueMap()));
                        return params;
                    }
                };

                return response(exchange, chain, requestDecorator, start);

            case POST:

                ServerHttpRequestDecorator decorator = new ServerHttpRequestDecorator(request) {
                    @NotNull
                    @Override
                    public Flux<DataBuffer> getBody() {
                        return Flux.from(DataBufferUtils.join(super.getBody())
                                .doOnNext(dataBuffer -> {
                                    String request = dataBuffer.toString(StandardCharsets.UTF_8);
                                    //输出body
                                    logger.info("{} request is {}", path, request);
                                }));
                    }
                };

                return response(exchange, chain, decorator, start);

            default:
                return chain.filter(exchange);
        }
    }

    private Mono<Void> response(ServerWebExchange exchange,
                                WebFilterChain chain,
                                ServerHttpRequestDecorator requestDecorator,
                                long start) {

        ServerHttpResponse originalResponse = exchange.getResponse();
        ServerHttpRequest request = exchange.getRequest();
        final String path = request.getURI().getPath();
        ServerHttpResponseDecorator responseDecorator = new ServerHttpResponseDecorator(originalResponse) {
            @NotNull
            @Override
            public Mono<Void> writeWith(@NotNull Publisher<? extends DataBuffer> body) {
                //输出返回结果
                return super.writeWith(DataBufferUtils.join(body)
                        .doOnNext(dataBuffer -> {
                            String result = dataBuffer.toString(StandardCharsets.UTF_8);
                            //打印返回结果
                            logger.info("{} result {} {} ms", path, result, System.currentTimeMillis() - start);
                        }));
            }
        };

        return chain.filter(exchange.mutate()
                .request(requestDecorator)
                .response(responseDecorator)
                .build());
    }
}
