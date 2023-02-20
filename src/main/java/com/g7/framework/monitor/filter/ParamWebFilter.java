package com.g7.framework.monitor.filter;

import com.g7.framwork.common.util.json.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
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
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        String method = request.getMethod();
        // 请求地址
        String path = request.getRequestURI();
        if (Objects.isNull(method) || (StringUtils.hasText(path) && path.contains("/actuator"))) {
            // 未知请求类型或健康检查接口不打印出入参记录
            filterChain.doFilter(request, response);
        } else {
            switch (method) {
                case "GET":
                    // 打印入参
                    logger.info("{} request is {}", path, JsonUtils.toJson(request.getParameterMap()));
                    break;
                case "POST":
                    logger.info("{} request is {}", path, getBodyString(request));
            }

            final long start = System.currentTimeMillis();
            ResponseWrapper responseWrapper = new ResponseWrapper(response);
            // 调用下游逻辑
            filterChain.doFilter(request, responseWrapper);
            String content = responseWrapper.getTextContent();
            //打印返回结果
            logger.info("{} result {} {} ms", path, content, System.currentTimeMillis() - start);
            response.getOutputStream().write(content.getBytes());
        }
    }

    private String getBodyString(HttpServletRequest request) {
        BufferedReader br = null;
        StringBuilder sb = new StringBuilder();
        try {
            br = request.getReader();
            String str;
            while ((str = br.readLine()) != null) {
                sb.append(str);
            }
            br.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != br) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return sb.toString();
    }
}
