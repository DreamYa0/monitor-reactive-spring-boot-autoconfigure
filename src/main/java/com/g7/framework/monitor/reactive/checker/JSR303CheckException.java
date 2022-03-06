package com.g7.framework.monitor.reactive.checker;


import java.util.HashMap;
import java.util.Map;

/**
 * @author dreamyao
 * @title 参数校验失败异常
 * @date 2018/05/28 下午5:46
 * @since 1.0.0
 */
public class JSR303CheckException extends IllegalArgumentException {

    private static final long serialVersionUID = 1L;

    private Map<String, String> errorMap = new HashMap<String, String>();

    private String msg;

    public JSR303CheckException() {
        super();
    }

    public JSR303CheckException(String message) {
        super(message);
    }

    public JSR303CheckException(Throwable cause) {
        super(cause);
    }

    public Map<String, String> getErrorMap() {
        return errorMap;
    }

    /**
     * 增加参数错误信息
     * @param parameter 校验失败参数
     * @param msg       参数信息
     */
    public void addError(String parameter, String msg) {
        this.errorMap.put(parameter, msg);
        this.msg = null;
    }

    @Override
    public String getMessage() {
        if (msg == null) {
            if (errorMap.isEmpty()) {
                msg = "";
            } else {
                StringBuilder sb = new StringBuilder();
                for (Map.Entry entry : errorMap.entrySet()) {
                    sb.append("[").append(entry.getKey()).append("]").append(" ").append(entry.getValue())
                            .append("|");
                }
                sb.deleteCharAt(sb.length() - 1);
                msg = sb.toString();
            }
        }
        return msg;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
