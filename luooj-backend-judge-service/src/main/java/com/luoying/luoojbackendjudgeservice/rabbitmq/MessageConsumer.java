package com.luoying.luoojbackendjudgeservice.rabbitmq;

import cn.hutool.core.util.StrUtil;
import com.luoying.luoojbackendcommon.common.ErrorCode;
import com.luoying.luoojbackendcommon.exception.BusinessException;
import com.luoying.luoojbackendjudgeservice.judge.JudgeService;
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
    public void receiveMessage(String message, Channel channel, @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag)
            throws IOException {
        log.info("receiveMessage message={}", message);
        long questionSubmitId = Long.parseLong(message);
        if (StringUtils.isBlank(message)) {
            // 消息为空，则拒绝消息（不重试），进入死信队列
            channel.basicNack(deliveryTag, false, false);
            throw new BusinessException(ErrorCode.NULL_ERROR, "消息为空");
        }
        try {
            // 判题
            judgeService.doJudge(questionSubmitId);
            // 判断提交状态是否为判题成功
            QuestionSubmit questionSubmit = questionFeignClient.getQuestionSubmitById(questionSubmitId);
            if (!QuestionSubmitStatusEnum.SUCCESS.getValue().equals(questionSubmit.getStatus())) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "判题失败");
            }

            // 判断题目是否通过
            QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
            if (!JudgeInfoMessagenum.ACCEPTED.getValue().equals(questionSubmitVO.getJudgeInfo().getMessage())) {
                channel.basicAck(deliveryTag, false);
                return;
            }
            // 设置通过数
            Long questionId = questionSubmit.getQuestionId();
            Question question = questionFeignClient.getQuestionById(questionId);
            Question updateQuestion = new Question();
            synchronized (this) {
                Integer acceptedNum = question.getAcceptedNum();
                acceptedNum = acceptedNum + 1;
                updateQuestion.setId(questionId);
                updateQuestion.setAcceptedNum(acceptedNum);
                boolean save = questionFeignClient.updateQuestionById(updateQuestion);
                if (!save) {
                    throw new BusinessException(ErrorCode.OPERATION_ERROR, "保存数据失败");
                }
            }
            // 设置个人题目通过表
            try {
                Long userId = questionSubmit.getUserId();
                String tableName = "accepted_question_" + userId;
                questionFeignClient.addAcceptedQuestion(tableName, questionId);
            } catch (Exception e) {
                log.info("该题目已通过，不用重复添加");
            }
            // 确认消息
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            channel.basicNack(deliveryTag, false, false);
        }
    }
}
