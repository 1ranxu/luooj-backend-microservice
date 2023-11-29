package com.luoying.luoojbackendjudgeservice.rabbitmq;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
@Slf4j
public class InitRabbitMq {
    private static final String EXCHANGE_NAME = "oj_exchange";
    private static final String QUEUE_NAME = "oj_queue";
    private static final String ROUTING_KEY = "oj_routingKey";
    private static final String DLX_EXCHANGE_NAME = "oj_dlx_exchange";
    private static final String DLX_QUEUE_NAME = "oj_dlx_queue";
    private static final String DLX_ROUTING_KEY = "oj_dlx_routingKey";

    public static void doInit() {
        try {
            ConnectionFactory connectionFactory = new ConnectionFactory();
            connectionFactory.setHost("localhost");
            Connection connection = connectionFactory.newConnection();
            Channel channel = connection.createChannel();
            // 指定死信参数
            Map<String, Object> args = new HashMap<>();
            // 指定死信交换机
            args.put("x-dead-letter-exchange", DLX_EXCHANGE_NAME);
            // 指定死信要转发到哪个队列
            args.put("x-dead-letter-routing-key", DLX_ROUTING_KEY);
            // 创建死信交换机
            channel.exchangeDeclare(DLX_EXCHANGE_NAME, "direct");

            // 创建工作交换机
            channel.exchangeDeclare(EXCHANGE_NAME, "direct");
            // 创建工作队列
            channel.queueDeclare(QUEUE_NAME, true, false, false, args);
            // 工作队列绑定工作交换机
            channel.queueBind(QUEUE_NAME, EXCHANGE_NAME, ROUTING_KEY);

            // 创建死信队列
            channel.queueDeclare(DLX_QUEUE_NAME, true, false, false, null);
            // 绑定死信交换机，指定路由键
            channel.queueBind(DLX_QUEUE_NAME, DLX_EXCHANGE_NAME, DLX_ROUTING_KEY);

            log.info("消息队列启动成功");
        } catch (Exception e) {
            log.error("消息队列启动失败");
        }
    }

    public static void main(String[] args) {
        doInit();
    }
}
