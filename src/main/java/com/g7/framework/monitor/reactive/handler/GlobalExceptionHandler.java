package com.g7.framework.monitor.reactive.handler;

import com.g7.framework.common.dto.BaseResult;
import com.g7.framework.framwork.exception.BusinessException;
import com.g7.framework.framwork.exception.meta.CodeMeta;
import com.g7.framework.framwork.exception.meta.CommonErrorCode;
import com.g7.framework.monitor.reactive.checker.JSR303CheckException;
import com.g7.framwork.common.util.json.JsonUtils;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author dreamyao
 * @title 全局异常处理器
 * @date 2022/3/6 5:44 下午
 * @since 1.0.0
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({Throwable.class})
    @ResponseStatus(HttpStatus.OK)
    public Map<String, Object> exception(Throwable throwable, HttpServletRequest request) {
        return response(throwable, request);
    }

    private Map<String, Object> response(Throwable throwable, HttpServletRequest request) {
        final BaseResult result = onResult(throwable);
        Map<String, Object> map = new HashMap<>();
        map.put("sid", result.getSid());
        map.put("success", result.isSuccess());
        map.put("code", result.getCode());
        map.put("description", result.getDescription());
        final String path = request.getRequestURI();
        logger.info("{} result {}", path, JsonUtils.toJson(result));
        return map;
    }

    private BaseResult onResult(Throwable throwable) {

        BaseResult result;

        if (throwable instanceof WebExchangeBindException) {
            // Spring 框架参数校验异常
            logger.info("parameter error", throwable);
            result = onBindException((WebExchangeBindException) throwable);
        } else if (throwable instanceof JSR303CheckException) {
            // JSR303Checker 工具类校验异常
            logger.info("parameter error", throwable);
            result = onCheckException((JSR303CheckException) throwable);
        } else if (throwable instanceof BusinessException) {
            // 业务异常
            logger.info("business error", throwable);
            result = onBusinessException((BusinessException) throwable);
        } else if (throwable instanceof Exception) {
            // 未知异常
            logger.error("unknown error", throwable);
            result = onException((Exception) throwable);
        } else {
            // 未知错误
            logger.error("unknown error", throwable);
            result = getDefaultResult(throwable);
        }

        return result;
    }

    private BaseResult onBindException(WebExchangeBindException e) {
        final List<ObjectError> errors = e.getAllErrors();
        StringBuilder sb = new StringBuilder();
        for (ObjectError error : errors) {
            final FieldError fieldError = (FieldError) error;
            final String field = fieldError.getField();
            final String message = fieldError.getDefaultMessage();
            sb.append("[").append(field).append("]").append(" ").append(message).append("|");
        }
        sb.deleteCharAt(sb.length() - 1);
        return getBaseResult(CommonErrorCode.ILLEGAL_ARGUMENT.getCode(), sb.toString());
    }

    @NotNull
    private BaseResult onCheckException(JSR303CheckException e) {
        return getBaseResult(CommonErrorCode.ILLEGAL_ARGUMENT.getCode(), e.getMessage());
    }

    private BaseResult onException(Exception e) {

        BaseResult result;
        // 兼容老项目抛出的RouteException
        CodeMeta codeMeta = buildCodeMeta(e.getMessage());
        if (Objects.nonNull(codeMeta)) {
            result = getBaseResult(codeMeta.getCode(), codeMeta.getMsgZhCN());
        } else {
            result = getDefaultResult(e);
        }
        return result;
    }

    private BaseResult onBusinessException(BusinessException e) {

        BaseResult result;

        Boolean isShow = e.getShow();
        if (isShow) {
            result = getBaseResult(e.getErrorCode(), e.getMessage());
        } else {
            result = getBaseResult(e.getErrorCode(), "Business exception");
        }

        return result;
    }

    private BaseResult getDefaultResult(Throwable e) {
        String message = e.getMessage();
        if (Objects.isNull(message)) {
            message = "java.lang.NullPointerException";
        }
        return getBaseResult(CommonErrorCode.SYS_ERROR.getCode(), message);
    }

    private CodeMeta buildCodeMeta(String message) {

        if (StringUtils.hasText(message) && message.contains("[_") && message.contains("_]")) {

            int begin = message.indexOf("[_") + 2;
            int end = message.indexOf("_]");

            String errorMessage = message.substring(begin, end);
            String[] split = errorMessage.split(":");

            String code = split[0];
            if (split.length == 2) {
                return new CodeMeta(code, "ERROR", split[1]);
            }

            if (split.length > 2) {
                StringBuilder builder = new StringBuilder();
                for (int i = 1; i < split.length; i++) {
                    builder.append(split[i]);
                }
                return new CodeMeta(code, "ERROR", builder.toString());
            }

            return new CodeMeta(CommonErrorCode.REMOTE_SERVICE.getCode(), errorMessage);
        }

        return null;
    }

    private BaseResult getBaseResult(String code, String desc) {
        BaseResult result = new BaseResult();
        result.setSuccess(false);
        result.setCode(code);
        result.setDescription(desc);
        return result;
    }
}
