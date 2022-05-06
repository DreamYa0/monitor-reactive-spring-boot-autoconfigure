package com.g7.framework.monitor.reactive.checker;

import reactor.core.publisher.Mono;

import javax.validation.ConstraintViolation;
import java.util.Set;

/**
 * @author dreamyao
 * @title
 * @date 2018/8/25 下午2:00
 * @since 1.0.0
 */
public class JSR303Checker {

    public static <T> Mono<T> check(T o) {
        // 通过jsr303规范的注解来校验参数
        return Mono.create(sink -> {
            final JSR303CheckException exception = checkReturn(o);
            if (exception != null) {
                sink.error(exception);
            } else {
                sink.success(o);
            }
        });
    }

    public static <T> JSR303CheckException checkReturn(T o) {
        Set<ConstraintViolation<Object>> constraintViolations = ParamValidatorFactory.INSTANCE.getValidator()
                .validate(o);
        JSR303CheckException exception = null;
        if (constraintViolations != null && !constraintViolations.isEmpty()) {
            exception = new JSR303CheckException();
            for (ConstraintViolation<Object> constraintViolation : constraintViolations) {
                exception.addError(constraintViolation.getPropertyPath().toString(),
                        constraintViolation.getMessage());
            }
        }

        return exception;
    }
}
