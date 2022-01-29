package com.g7.framework.monitor.reactive.dubbo;

import com.dianping.cat.Cat;
import com.dianping.cat.util.Splitters;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dreamyao
 * @title
 * @date 2020-03-23 10:23
 * @since 1.0.0
 */
public abstract class DubheContext implements Cat.Context, Serializable {

    private static final long serialVersionUID = 1L;
    public static String ID = "DubheContext";
    protected Map<String, String> maps = new HashMap<>();

    @Override
    public void addProperty(String key, String value) {
        maps.put(key, value);
    }

    @Override
    public String getProperty(String key) {
        return maps.get(key);
    }

    /**
     * @return
     * @see
     */
    @Deprecated
    public abstract String encode();

    public abstract String encodeSelf();

    @Deprecated
    public abstract String encode(DubheContext context);

    @Deprecated
    public abstract DubheContext decode(String context);

    public abstract void decodeSelf(String context);

    public String getAppName() {
        try {
            String parentId = getProperty(Cat.Context.PARENT);
            if (parentId != null) {
                return parseReturnDomain(parentId);
            }
        } catch (Exception e) {
            Cat.logError("getAppName", e);
        }
        return "unknown";
    }

    private static String parseReturnDomain(String messageId) {
        List<String> list = Splitters.by('-').split(messageId);
        int len = list.size();
        String domain;
        if (len > 4) { // allow domain contains '-'
            StringBuilder sb = new StringBuilder();

            for (int i = 0; i < len - 3; i++) {
                if (i > 0) {
                    sb.append('-');
                }

                sb.append(list.get(i));
            }

            domain = sb.toString();
        } else {
            domain = list.get(0);
        }
        return domain;
    }
}
