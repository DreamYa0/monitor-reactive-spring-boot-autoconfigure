package com.g7.framework.monitor.reactive.dubbo;


import com.alibaba.dubbo.common.Constants;
import com.alibaba.dubbo.common.URL;
import com.alibaba.dubbo.common.extension.Activate;
import com.alibaba.dubbo.rpc.*;
import com.dianping.cat.Cat;
import com.dianping.cat.message.Event;
import com.dianping.cat.message.Message;
import com.dianping.cat.message.Transaction;
import com.dianping.cat.message.internal.AbstractMessage;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Activate(group = {Constants.PROVIDER, Constants.CONSUMER}, order = -9000)
public class CatTransaction implements Filter {

    private static final ThreadLocal<Cat.Context> CAT_CONTEXT = new ThreadLocal<>();

    @Override
    public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {

        URL url = invoker.getUrl();
        String sideKey = url.getParameter(Constants.SIDE_KEY);
        String loggerName = invoker.getInterface().getSimpleName() + "." + invocation.getMethodName();

        String type = CatConstants.CROSS_CONSUMER;
        if(Constants.PROVIDER_SIDE.equals(sideKey)){
            type= CatConstants.CROSS_SERVER;
        }

        Transaction transaction = Cat.newTransaction(type, loggerName);
        transaction.setStatus(Message.SUCCESS);
        try {
            Cat.Context context = getContext();
            DubheContext dubheContext = new DubheJsonContext();
            if(Constants.CONSUMER_SIDE.equals(sideKey)){
                createConsumerCross(url,transaction);
                Cat.logRemoteCallClient(context);

                // 兼容天枢旧的链路
                dubheContext.addProperty(Cat.Context.ROOT, context.getProperty(Cat.Context.ROOT));
                dubheContext.addProperty(Cat.Context.PARENT, context.getProperty(Cat.Context.PARENT));
                dubheContext.addProperty(Cat.Context.CHILD, context.getProperty(Cat.Context.CHILD));
                RpcContext.getContext().setAttachment(DubheContext.ID, dubheContext.encodeSelf());
            }else{
                createProviderCross(url,transaction);
                if (invocation.getAttachment(DubheContext.ID) == null) {
                    Cat.logRemoteCallServer(context);
                } else {
                    // 兼容天枢旧的链路
                    dubheContext.decodeSelf(invocation.getAttachment(DubheContext.ID));
                    Cat.logRemoteCallServer(dubheContext);
                    context.addProperty(Cat.Context.ROOT, dubheContext.getProperty(Cat.Context.ROOT));
                    context.addProperty(Cat.Context.PARENT, dubheContext.getProperty(Cat.Context.PARENT));
                    context.addProperty(Cat.Context.CHILD, dubheContext.getProperty(Cat.Context.CHILD));
                }
            }
            setAttachment(context);

            return invoker.invoke(invocation);
        } catch (Exception e) {
            Cat.logError(e);
            transaction.setStatus("ERROR");
            addCatData(invocation, transaction);
            throw e;
        } finally {
            transaction.complete();
            CAT_CONTEXT.remove();
        }
    }

    private void addCatData(Invocation invocation, Transaction t) {
        Object[] obj = invocation.getArguments();
        t.addData(CatConstants.ARGUMENTS, Arrays.toString(obj));
        Map<String, String> map = invocation.getAttachments();
        t.addData(CatConstants.ATTACHMENTS, map.toString());
    }

    static class DubboCatContext implements Cat.Context {

        private Map<String, String> properties = new HashMap<>();

        @Override
        public void addProperty(String key, String value) {
            properties.put(key, value);
        }

        @Override
        public String getProperty(String key) {
            return properties.get(key);
        }
    }

    private String getProviderAppName(URL url) {
        String appName = url.getParameter(CatConstants.PROVIDER_APPLICATION_NAME);
        if (StringUtils.isEmpty(appName)) {
            String interfaceName = url.getParameter(Constants.INTERFACE_KEY);
            appName = interfaceName.substring(0, interfaceName.lastIndexOf('.'));
        }
        return appName;
    }

    private void setAttachment(Cat.Context context) {
        RpcContext.getContext().setAttachment(Cat.Context.ROOT, context.getProperty(Cat.Context.ROOT));
        RpcContext.getContext().setAttachment(Cat.Context.CHILD, context.getProperty(Cat.Context.CHILD));
        RpcContext.getContext().setAttachment(Cat.Context.PARENT, context.getProperty(Cat.Context.PARENT));
    }

    private Cat.Context getContext() {
        Cat.Context context = CAT_CONTEXT.get();
        if (context == null) {
            context = initContext();
            CAT_CONTEXT.set(context);
        }
        return context;
    }

    private Cat.Context initContext() {
        Cat.Context context = new DubboCatContext();
        Map<String, String> attachments = RpcContext.getContext().getAttachments();
        if (attachments != null && attachments.size() > 0) {
            for (Map.Entry<String, String> entry : attachments.entrySet()) {
                if (Cat.Context.CHILD.equals(entry.getKey()) || Cat.Context.ROOT.equals(entry.getKey()) ||
                        Cat.Context.PARENT.equals(entry.getKey())) {
                    context.addProperty(entry.getKey(), entry.getValue());
                }
            }
        }
        return context;
    }

    private void createConsumerCross(URL url, Transaction transaction) {
        Event crossAppEvent = Cat.newEvent(CatConstants.CONSUMER_CALL_APP, getProviderAppName(url));
        Event crossServerEvent = Cat.newEvent(CatConstants.CONSUMER_CALL_SERVER, url.getHost());
        Event crossPortEvent = Cat.newEvent(CatConstants.CONSUMER_CALL_PORT, url.getPort() + "");
        crossAppEvent.setStatus(Event.SUCCESS);
        crossServerEvent.setStatus(Event.SUCCESS);
        crossPortEvent.setStatus(Event.SUCCESS);
        completeEvent(crossAppEvent);
        completeEvent(crossPortEvent);
        completeEvent(crossServerEvent);
        transaction.addChild(crossAppEvent);
        transaction.addChild(crossPortEvent);
        transaction.addChild(crossServerEvent);
    }

    private void completeEvent(Event event) {
        AbstractMessage message = (AbstractMessage) event;
        message.setCompleted(true);
    }

    private void createProviderCross(URL url, Transaction transaction) {
        String consumerAppName = RpcContext.getContext().getAttachment(Constants.APPLICATION_KEY);
        if (StringUtils.isEmpty(consumerAppName)) {
            consumerAppName = RpcContext.getContext().getRemoteHost() + ":" + RpcContext.getContext().getRemotePort();
        }
        Event crossAppEvent = Cat.newEvent(CatConstants.PROVIDER_CALL_APP, consumerAppName);
        Event crossServerEvent = Cat.newEvent(CatConstants.PROVIDER_CALL_SERVER, RpcContext.getContext().getRemoteHost());
        crossAppEvent.setStatus(Event.SUCCESS);
        crossServerEvent.setStatus(Event.SUCCESS);
        completeEvent(crossAppEvent);
        completeEvent(crossServerEvent);
        transaction.addChild(crossAppEvent);
        transaction.addChild(crossServerEvent);
    }
}
