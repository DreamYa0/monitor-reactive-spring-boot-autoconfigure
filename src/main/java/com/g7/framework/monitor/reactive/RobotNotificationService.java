package com.g7.framework.monitor.reactive;

import com.g7.framework.monitor.reactive.robot.config.RobotProperties;
import com.g7.framework.monitor.reactive.robot.richtext.*;
import com.g7.framework.monitor.reactive.robot.text.BaseNotification;
import com.g7.framework.monitor.reactive.robot.text.BaseNotificationV2;
import com.g7.framework.monitor.reactive.robot.text.Notification;
import com.g7.framework.monitor.reactive.robot.text.TextNotification;
import com.g7.framework.reactive.common.util.http.ReactiveRest;
import com.g7.framwork.common.util.json.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;

/**
 * @author dreamyao
 */
public class RobotNotificationService {

    private static final Logger logger = LoggerFactory.getLogger(RobotNotificationService.class);
    private final RobotProperties robotProperties;
    private final ReactiveRest reactiveRest;

    public RobotNotificationService(RobotProperties robotProperties, ReactiveRest reactiveRest) {
        this.robotProperties = robotProperties;
        this.reactiveRest = reactiveRest;
    }

    /**
     * 发送文本消息(不带标题)
     * @param content 消息内容
     */
    public void sendText(String content) {
        final Mono<String> mono = Mono.create(sink -> {
            Assert.notNull(content, "机器人告警内容不能为空");
            String msg;
            if (robotProperties.getOpenNewVersion()) {
                TextNotification textNotification = buildText(content);
                msg = JsonUtils.toJson(textNotification);
            } else {
                BaseNotification baseNotification = new BaseNotification();
                baseNotification.setText(content);
                msg = JsonUtils.toJson(baseNotification);
            }
            sink.success(msg);
        });
        subscribe(mono);
    }

    /**
     * 发送文本消息(带标题)
     * @param content 消息内容
     * @param title   标题
     */
    public void sendText(String title, String content) {
        final Mono<String> mono = Mono.create(sink -> {
            Assert.notNull(content, "机器人告警内容不能为空");
            String msg;
            if (robotProperties.getOpenNewVersion()) {
                RichTextNotification richTextNotification = buildRichText(title, content);
                msg = JsonUtils.toJson(richTextNotification);
            } else {
                Notification notification = new Notification();
                notification.setTitle(title);
                notification.setText(content);
                msg = JsonUtils.toJson(notification);
            }
            sink.success(msg);
        });
        subscribe(mono);
    }

    private void subscribe(Mono<String> mono) {
        mono.flatMap(this::doSend)
                .onErrorContinue((throwable, msg) ->
                        logger.error("robot send message failed, message is {}", msg, throwable))
                .subscribeOn(Schedulers.single())
                .subscribe();
    }

    private Mono<String> doSend(String payload) {
        if (robotProperties.getEnabled()) {
            return reactiveRest.post(robotProperties.getUrl(), payload);

        } else {
            return Mono.empty();
        }
    }

    /**
     * 构建普通文本消息体（新版）
     * @param content
     * @return
     */
    private TextNotification buildText(String content) {
        // 普通文本内容
        BaseNotificationV2 baseNotificationV2 = new BaseNotificationV2();
        baseNotificationV2.setText(content);

        // 普通文本消息体
        TextNotification textNotification = new TextNotification();
        textNotification.setContent(baseNotificationV2);
        textNotification.setMsg_type("text");
        return textNotification;
    }

    /**
     * 构建富文本消息体（新版）
     * @param content
     * @return
     */
    private RichTextNotification buildRichText(String title, String content) {
        // 标题对应内容
        RichTextNotificationLanguageContent languageContent = new RichTextNotificationLanguageContent();
        languageContent.setText(content);
        languageContent.setTag("text");

        List<RichTextNotificationLanguageContent> languageContents = new ArrayList<>();
        languageContents.add(languageContent);

        List<List<RichTextNotificationLanguageContent>> languageContentList = new ArrayList<>();
        languageContentList.add(languageContents);

        // 富文本语言
        RichTextNotificationLanguage language = new RichTextNotificationLanguage();
        language.setTitle(title);
        language.setContent(languageContentList);

        // 富文本设置语言属性
        RichTextNotificationPost notificationPost = new RichTextNotificationPost();
        notificationPost.setZhCn(language);

        RichTextNotificationContent notificationContent = new RichTextNotificationContent();
        notificationContent.setPost(notificationPost);

        // 富文本消息体
        RichTextNotification richTextNotification = new RichTextNotification();
        richTextNotification.setContent(notificationContent);
        richTextNotification.setMsg_type("post");
        return richTextNotification;
    }
}
