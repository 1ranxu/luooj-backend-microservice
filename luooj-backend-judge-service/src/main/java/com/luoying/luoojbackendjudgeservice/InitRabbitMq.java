package com.luoying.luoojbackendjudgeservice;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
@Slf4j
public class InitRabbitMq {
    private static final String EXCHANGE_NAME = "oj_exchange";
    private static final String QUEUE_NAME = "oj_queue";

    public static void doInit() {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost("localhost");
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            // 创建交换机
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");
            // 创建队列
            channel.queueDeclare(QUEUE_NAME, true, false, false, null);
            // 绑定交换机
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, "oj_routingKey");
            log.info("消息队列启动成功");
        } catch (Exception e) {
            log.error("消息队列启动失败");
        }
    }

    public static void main(String[] args) {
        doInit();

    }
}
