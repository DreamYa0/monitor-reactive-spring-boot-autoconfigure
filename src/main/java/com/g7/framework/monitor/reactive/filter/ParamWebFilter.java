package com.g7.framework.monitor.reactive.filter;

import com.g7.framwork.common.util.json.JsonUtils;
import com.google.common.base.Splitter;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.codec.HttpMessageReader;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.web.reactive.function.server.HandlerStrategies;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author dreamyao
 * @title 出入参记录过滤器
 * @date 2022/3/6 5:03 下午
 * @since 1.0.0
 */
public class ParamWebFilter implements WebFilter {

    private static final Logger logger = LoggerFactory.getLogger(ParamWebFilter.class);
    private final List<HttpMessageReader<?>> messageReaders = HandlerStrategies.withDefaults().messageReaders();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        final ServerHttpRequest request = exchange.getRequest();
        final HttpMethod method = request.getMethod();
        if (Objects.isNull(method)) {
            return chain.filter(exchange);
        }

        final long start = System.currentTimeMillis();
        final String path = request.getURI().getPath();

        switch (method) {

            case GET:
                logger.info("{} request is {}", path,
                        JsonUtils.toJson(request.getQueryParams().toSingleValueMap()));
                return response(exchange, chain, start);

            case POST:
                ServerRequest serverRequest = ServerRequest.create(exchange, messageReaders);
                return serverRequest.bodyToMono(String.class).flatMap(body -> {
                    // 获取请求body类型
                    MediaType mediaType = request.getHeaders().getContentType();
                    if (MediaType.APPLICATION_JSON.isCompatibleWith(mediaType)) {
                        logger.info("{} request is {}", path, body);
                    } else if (MediaType.APPLICATION_FORM_URLENCODED.isCompatibleWith(mediaType)) {
                        // 解析 application/x-www-form-urlencoded 格式 body
                        // 处理前端传入的表单参数
                        final Map<String, String> paramMap = new HashMap<>();
                        Splitter.on("&").split(body).forEach(param -> {
                            List<String> keyValue = Splitter.on("=").splitToList(param);
                            if (keyValue.size() != 2) {
                                paramMap.put(keyValue.get(0), null);
                            } else {
                                paramMap.put(keyValue.get(0), keyValue.get(1));
                            }
                        });

                        logger.info("{} request is {}", path, JsonUtils.toJson(paramMap));
                    }
                    return response(exchange, chain, start);
                });

            default:
                return chain.filter(exchange);
        }
    }

    private Mono<Void> response(ServerWebExchange exchange, WebFilterChain chain, long start) {
        ServerHttpResponse originalResponse = exchange.getResponse();
        ServerHttpRequest request = exchange.getRequest();
        final String path = request.getURI().getPath();
        ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {
            @Override
            public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                //输出返回结果
                return super.writeWith(DataBufferUtils.join(body)
                        .doOnNext(dataBuffer -> {
                            String result = dataBuffer.toString(StandardCharsets.UTF_8);
                            //输出body
                            logger.info("{} result {} {} ms", path, result, System.currentTimeMillis() - start);
                        }));
            }
        };
        return chain.filter(exchange.mutate().response(decoratedResponse).build());
    }
}
