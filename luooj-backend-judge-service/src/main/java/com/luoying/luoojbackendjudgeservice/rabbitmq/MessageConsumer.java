package com.luoying.luoojbackendjudgeservice.rabbitmq;

import com.luoying.luoojbackendcommon.common.ErrorCode;
import com.luoying.luoojbackendcommon.exception.BusinessException;
import com.luoying.luoojbackendjudgeservice.judge.service.JudgeService;
import com.luoying.luoojbackendmodel.entity.Question;
import com.luoying.luoojbackendmodel.entity.QuestionSubmit;
import com.luoying.luoojbackendmodel.enums.JudgeInfoMessagenum;
import com.luoying.luoojbackendmodel.enums.QuestionSubmitStatusEnum;
import com.luoying.luoojbackendmodel.vo.QuestionSubmitVO;
import com.luoying.luoojbackendserviceclient.service.QuestionFeignClient;
import com.rabbitmq.client.Channel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author 落樱的悔恨
 * 消息消费者
 */
@Component
@Slf4j
public class MessageConsumer {

    @Resource
    private JudgeService judgeService;

    @Resource
    private QuestionFeignClient questionFeignClient;

    private static final String QUEUE_NAME = "oj_queue";

    //指定程序监听的消息队列和确认机制
    @RabbitListener(queues = {QUEUE_NAME}, ackMode = "MANUAL")
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            if (StringUtils.isBlank(message)) {
                // 消息为空，则拒绝消息（不重试），进入死信队列
                log.info("消息为空，拒绝消息，消息进入死信队列 message={}", message);
                channel.basicNack(deliveryTag, false, false);
                return;
            }
            log.info("receiveMessage message={}", message);
            long questionSubmitId = Long.parseLong(message);

            // 判题
            judgeService.doJudge(questionSubmitId);

            // 判断提交状态是否为判题成功
            QuestionSubmit questionSubmit = questionFeignClient.getQuestionSubmitById(questionSubmitId);
            log.info("判题后的questionSubmit:{}", questionSubmit);
            if (!QuestionSubmitStatusEnum.SUCCESS.getValue().equals(questionSubmit.getStatus())) {
                log.info("判题失败，确认消息，message={}", message);
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 判断题目是否通过
            QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
            if (!JudgeInfoMessagenum.ACCEPTED.getValue().equals(questionSubmitVO.getJudgeInfo().getMessage())) {
                log.info("未通过，确认消息，message={}", message);
                channel.basicAck(deliveryTag, false);
                return;
            }

            // 设置通过数
            Long questionId = questionSubmit.getQuestionId();
            Question question = questionFeignClient.getQuestionById(questionId);
            question.setAcceptedNum(question.getAcceptedNum() + 1);
            boolean save = questionFeignClient.updateQuestionById(question);
            if (!save) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "设置通过数失败");
            }
            // 添加通过记录
            Long userId = questionSubmit.getUserId();
            if(questionFeignClient.getAcceptedQuestion(questionId, userId) == null){
                questionFeignClient.addAcceptedQuestion(questionId, userId);
            }
            // 确认消息
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            try {
                log.info("消费消息失败，拒绝消息，message={}，Exception={}",message,e);
                channel.basicNack(deliveryTag, false, false);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
}
