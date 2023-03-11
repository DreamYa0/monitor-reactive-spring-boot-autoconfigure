package com.g7.framework.monitor.filter;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.Objects;

/**
 * @author dreamyao
 * @title 出入参记录过滤器
 * @date 2022/3/6 5:03 下午
 * @since 1.0.0
 */
public class ParamWebFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(ParamWebFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {

        String method = request.getMethod();
        // 请求地址
        String path = request.getRequestURI();
        if (Objects.isNull(method) || (StringUtils.hasText(path) && path.contains("/actuator"))) {
            // 未知请求类型或健康检查接口不打印出入参记录
            filterChain.doFilter(request, response);
        } else {

            final long start = System.currentTimeMillis();

            ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
            ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
            // 调用下游逻辑
            filterChain.doFilter(requestWrapper, responseWrapper);

            switch (method) {
                case "GET":
                    // 打印入参
                    String queryString = requestWrapper.getQueryString();
                    logger.info("{} request is {}", path, StringUtils.hasText(queryString) ?
                            URLDecoder.decode(queryString, "UTF-8") : null);
                    break;
                case "POST":
                    logger.info("{} request is {}", path, new String(requestWrapper.getContentAsByteArray()));
                    break;
            }

            //打印返回结果
            logger.info("{} result {} {} ms", path, new String(responseWrapper.getContentAsByteArray()),
                    System.currentTimeMillis() - start);
            responseWrapper.copyBodyToResponse();
        }
    }
}
