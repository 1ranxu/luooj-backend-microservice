package com.luoying.luoojbackendjudgeservice.rabbitmq;

import com.luoying.luoojbackendjudgeservice.judge.JudgeService;
import com.luoying.luoojbackendmodel.vo.QuestionSubmitVO;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;


@Component
@Slf4j
public class MessageConsumer {
    //指定程序监听的消息队列和确认机制
    @RabbitListener(queues = {"oj-queue"}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag)
            throws IOException {
        log.info("receiveMessage message={}", message);
        channel.basicAck(deliveryTag, false);

    }
}
