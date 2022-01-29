package com.g7.framework.monitor.reactive.dubbo;

import com.dianping.cat.Cat;
import com.g7.framwork.common.util.json.JsonUtils;
import com.g7.framwork.common.util.json.TypeReference;

import java.util.HashMap;
import java.util.Map;

/**
 * @author dreamyao
 * @title
 * @date 2020-03-23 10:25
 * @since 1.0.0
 */
class DubheJsonContext extends  DubheContext {

    /**
     */
    private static final long serialVersionUID = 1L;

    @Override
    public String encode(DubheContext context) {
        Map<String,Object> obj = new HashMap<>();
        if(context.maps.size()>0){
            for(String key:context.maps.keySet()){
                obj.put(key, context.maps.get(key));
            }
        }
        return JsonUtils.toJson(obj);
    }
    @Override
    public String encode() {
        Map<String,Object> obj = new HashMap<>();
        if(maps.size()>0){
            for(String key:maps.keySet()){
                obj.put(key, maps.get(key));
            }
        }
        return JsonUtils.toJson(obj);
    }

    @Override
    public DubheContext decode(String context) {
        DubheContext dubheContext = new DubheJsonContext();
        try {
            Map<String,String> obj = JsonUtils.fromJson(context, new TypeReference<Map<String, String>>() {
            }.getType());
            if(obj.size()>0){
                for(String key:obj.keySet()){
                    dubheContext.addProperty(key, obj.get(key));
                }
            }
        } catch (Exception e) {
            Cat.logError("context="+context,e);
        }
        return dubheContext;
    }
    @Override
    public void decodeSelf(String context) {
        try {
            Map<String,String> obj = JsonUtils.fromJson(context, new TypeReference<Map<String, String>>() {
            }.getType());
            if(obj.size()>0){
                for(String key:obj.keySet()){
                    addProperty(key, obj.get(key));
                }
            }
        } catch (Exception e) {
            Cat.logError("context="+context,e);
        }
    }
    @Override
    public String encodeSelf() {
        Map<String,Object> obj = new HashMap<>();
        if(maps.size()>0){
            for(String key:maps.keySet()){
                obj.put(key, maps.get(key));
            }
        }
        return JsonUtils.toJson(obj);
    }
}
