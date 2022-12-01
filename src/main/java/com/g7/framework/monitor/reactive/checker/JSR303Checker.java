package com.g7.framework.monitor.reactive.checker;

import javax.validation.ConstraintViolation;
import java.util.Set;

/**
 * @author dreamyao
 * @title
 * @date 2018/8/25 下午2:00
 * @since 1.0.0
 */
public class JSR303Checker {

    public static <T> T check(T o, Class<?>... groups) {
        // 通过jsr303规范的注解来校验参数
        final JSR303CheckException exception = doCheck(o, groups);
        if (exception != null) {
            throw exception;
        } else {
            return o;
        }
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

    public static <T> JSR303CheckException checkReturn(T o, Class<?>... groups) {
        return doCheck(o, groups);
    }
}
