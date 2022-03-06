package com.g7.framework.monitor.reactive.handler;

import com.alibaba.dubbo.rpc.RpcException;
import com.g7.framework.common.dto.BaseResult;
import com.g7.framework.framwork.exception.BusinessException;
import com.g7.framework.framwork.exception.meta.CodeMeta;
import com.g7.framework.framwork.exception.meta.CommonErrorCode;
import com.g7.framework.monitor.reactive.checker.JSR303CheckException;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.util.StringUtils;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.reactive.function.server.*;

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
public class GlobalExceptionHandler extends DefaultErrorWebExceptionHandler {

    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    public GlobalExceptionHandler(ErrorAttributes errorAttributes,
                                  WebProperties.Resources resources,
                                  ErrorProperties errorProperties,
                                  ApplicationContext applicationContext) {

        super(errorAttributes, resources, errorProperties,
                applicationContext);
    }

    @Override
    protected Map<String, Object> getErrorAttributes(ServerRequest request, ErrorAttributeOptions options) {
        return response(request);
    }

    @Override
    protected RouterFunction<ServerResponse> getRoutingFunction(final ErrorAttributes errorAttributes) {
        return RouterFunctions.route(RequestPredicates.all(), this::renderErrorResponse);
    }

    @Override
    protected int getHttpStatus(final Map<String, Object> errorAttributes) {
        return HttpStatus.INTERNAL_SERVER_ERROR.value();
    }

    private Map<String, Object> response(final ServerRequest request) {
        Throwable throwable = getError(request);
        final BaseResult result = onResult(throwable);
        Map<String, Object> map = new HashMap<>();
        map.put("sid", result.getSid());
        map.put("success", result.isSuccess());
        map.put("code", result.getCode());
        map.put("description", result.getDescription());
        return map;
    }

    private BaseResult onResult(Throwable throwable) {

        BaseResult result;

        if (throwable instanceof WebExchangeBindException) {
            // Spring 框架参数校验异常
            result = onBindException((WebExchangeBindException) throwable);
        } else if (throwable instanceof JSR303CheckException) {
            // JSR303Checker 工具类校验异常
            result = onCheckException((JSR303CheckException) throwable);
        } else if (throwable instanceof BusinessException) {
            // 业务异常
            result = onBusinessException((BusinessException) throwable);
        } else if (throwable instanceof RpcException) {
            // Dubbo Rpc调用异常
            result = onRpcException((RpcException) throwable);
        } else if (throwable instanceof Exception) {
            // 未知异常
            result = onException((Exception) throwable);
        } else {
            // 未知错误
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
        return getBaseResult(false, CommonErrorCode.ILLEGAL_ARGUMENT.getCode(), sb.toString());
    }

    @NotNull
    private BaseResult onCheckException(JSR303CheckException e) {
        return getBaseResult(false, CommonErrorCode.ILLEGAL_ARGUMENT.getCode(), e.getMessage());
    }

    private BaseResult onException(Exception e) {

        BaseResult result;
        // 兼容老项目抛出的RouteException
        CodeMeta codeMeta = buildCodeMeta(e.getMessage());
        if (Objects.nonNull(codeMeta)) {
            result = getBaseResult(false, codeMeta.getCode(), codeMeta.getMsgZhCN());
        } else {
            result = getDefaultResult(e);
        }
        return result;
    }

    private BaseResult onBusinessException(BusinessException e) {

        BaseResult result;

        Boolean isShow = e.getShow();
        if (isShow) {
            result = getBaseResult(false, e.getErrorCode(), e.getMessage());
        } else {
            result = getBaseResult(false, e.getErrorCode(), "Business exception");
        }

        return result;
    }

    private BaseResult onRpcException(RpcException rpc) {
        BaseResult result;
        // dubbo 调用异常
        if (rpc.isTimeout()) {
            result = getBaseResult(false,
                    CommonErrorCode.BUSY_SERVICE.getCode(),
                    CommonErrorCode.BUSY_SERVICE.getMsgZhCN());
        } else if (rpc.isNetwork()) {
            result = getBaseResult(false,
                    CommonErrorCode.NETWORK_CONNECT_FAILED.getCode(),
                    CommonErrorCode.NETWORK_CONNECT_FAILED.getMsgZhCN());
        } else if (rpc.isSerialization()) {
            result = getBaseResult(false,
                    CommonErrorCode.SERIALIZATION_EXCEPTION.getCode(),
                    CommonErrorCode.SERIALIZATION_EXCEPTION.getMsgZhCN());
        } else if (rpc.isForbidded()) {
            result = getBaseResult(false,
                    CommonErrorCode.FORBIDDEN_EXCEPTION.getCode(),
                    CommonErrorCode.FORBIDDEN_EXCEPTION.getMsgZhCN());
        } else {
            result = getBaseResult(false,
                    CommonErrorCode.RPC_CALL_EXCEPTION.getCode(),
                    CommonErrorCode.RPC_CALL_EXCEPTION.getMsgZhCN());
        }
        return result;
    }

    private BaseResult getDefaultResult(Throwable e) {
        String message = e.getMessage();
        if (Objects.isNull(message)) {
            message = "java.lang.NullPointerException";
        }
        return getBaseResult(false, CommonErrorCode.SYS_ERROR.getCode(), message);
    }

    private CodeMeta buildCodeMeta(String message) {

        if (Boolean.FALSE.equals(StringUtils.isEmpty(message)) &&
                message.contains("[_") && message.contains("_]")) {

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

    private BaseResult getBaseResult(boolean success, String code, String desc) {
        BaseResult result = new BaseResult();
        result.setSuccess(success);
        result.setCode(code);
        result.setDescription(desc);
        return result;
    }
}
