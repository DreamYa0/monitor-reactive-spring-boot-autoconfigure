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

    public static <T> Mono<T> check(T o, Class<?>... groups) {
        // 通过jsr303规范的注解来校验参数
        return Mono.create(sink -> {
            final JSR303CheckException exception = doCheck(o, groups);
            if (exception != null) {
                sink.error(exception);
            } else {
                sink.success(o);
            }
        });
    }

    private static <T> JSR303CheckException doCheck(T o, Class<?>... groups) {
        Set<ConstraintViolation<Object>> constraintViolations = ParamValidatorFactory.INSTANCE.getValidator()
                .validate(o, groups);
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

    public static <T> Mono<JSR303CheckException> checkReturn(T o, Class<?>... groups) {
        return Mono.justOrEmpty(doCheck(o, groups));
    }
}
