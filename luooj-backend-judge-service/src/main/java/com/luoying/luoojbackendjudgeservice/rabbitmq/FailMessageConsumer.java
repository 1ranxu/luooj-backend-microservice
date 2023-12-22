package com.luoying.luoojbackendjudgeservice.rabbitmq;

import com.luoying.luoojbackendcommon.common.ErrorCode;
import com.luoying.luoojbackendcommon.exception.BusinessException;
import com.luoying.luoojbackendmodel.entity.QuestionSubmit;
import com.luoying.luoojbackendmodel.enums.QuestionSubmitStatusEnum;
import com.luoying.luoojbackendserviceclient.service.QuestionFeignClient;
import com.rabbitmq.client.Channel;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 判题死信队列
 * @author 落樱的悔恨
 */
@Component
@Slf4j
public class FailMessageConsumer {

    @Resource
    private QuestionFeignClient questionFeignClient;

    private static final String DLX_QUEUE_NAME = "oj_dlx_queue";


    /**
     * 监听死信队列
     *
     * @param message
     * @param channel
     * @param deliveryTag
     */
    @SneakyThrows
    @RabbitListener(queues = {DLX_QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        // 接收到失败的信息
        log.info("死信队列接受到的消息：{}", message);
        if (StringUtils.isBlank(message)) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "消息为空");
        }
        long questionSubmitId = Long.parseLong(message);
        QuestionSubmit questionSubmit = questionFeignClient.getQuestionSubmitById(questionSubmitId);
        if (questionSubmit == null) {
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "提交的题目信息不存在");
        }
        // 把题目提交总表中的提交记录的提交状态修改为失败
        questionSubmit.setStatus(QuestionSubmitStatusEnum.FAILURE.getValue());

        boolean update = questionFeignClient.updateQuestionSubmitById(questionSubmit);
        if (!update) {
            log.info("处理死信队列消息失败,对应提交的题目id为:{}", questionSubmit.getId());
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "处理死信队列消息失败");
        }
        // 把题目提交个人表中的提交记录的提交状态修改为失败
        long userId = questionSubmit.getUserId();
        String tableName = "question_submit_" + userId;
        update = questionFeignClient.updateQuestionSubmit(tableName, questionSubmit);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "处理死信队列消息失败");
        }
        // 确认消息
        channel.basicAck(deliveryTag, false);
    }
}