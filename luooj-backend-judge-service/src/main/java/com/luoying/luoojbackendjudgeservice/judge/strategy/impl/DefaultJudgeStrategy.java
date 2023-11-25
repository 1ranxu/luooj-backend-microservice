package com.luoying.luoojbackendjudgeservice.judge.strategy.impl;

import cn.hutool.json.JSONUtil;
import com.luoying.luoojbackendjudgeservice.judge.strategy.JudgeContext;
import com.luoying.luoojbackendjudgeservice.judge.strategy.JudgeStrategy;
import com.luoying.luoojbackendmodel.dto.question.QuestionJudgeCase;
import com.luoying.luoojbackendmodel.dto.question.QuestionJudgeCconfig;
import com.luoying.luoojbackendmodel.dto.questionsubmit.QuestionSubmitJudgeInfo;
import com.luoying.luoojbackendmodel.entity.Question;
import com.luoying.luoojbackendmodel.enums.JudgeInfoMessagenum;

import java.util.List;
import java.util.Optional;

/**
 * 默认判题策略
 */
public class DefaultJudgeStrategy implements JudgeStrategy {
    /**
     * 执行判题
     * @param judgeContext
     * @return
     */
    @Override
    public QuestionSubmitJudgeInfo doJudge(JudgeContext judgeContext) {
        QuestionSubmitJudgeInfo judgeInfo = judgeContext.getJudgeInfo();
        List<String> inputList = judgeContext.getInputList();
        List<String> outputList = judgeContext.getOutputList();
        Question question = judgeContext.getQuestion();
        List<QuestionJudgeCase> judgeCaseList = judgeContext.getJudgeCaseList();

        // 5 根据沙箱的执行结果，设置题目的判题状态和信息
        JudgeInfoMessagenum judgeInfoMessagenum = JudgeInfoMessagenum.ACCEPTED;
        // 5.1 先判断沙箱执行的结果输出数量是否和预期输出数量相等
        if (outputList.size() != inputList.size()) {
            judgeInfoMessagenum = JudgeInfoMessagenum.WRONG_ANSWER;
        }
        // 5.2 依次判断每一项输出和预期输出是否相等
        for (int i = 0; i < judgeCaseList.size(); i++) {
            QuestionJudgeCase judgeCase = judgeCaseList.get(i);
            if (!judgeCase.getOutput().equals(outputList.get(i))) {
                judgeInfoMessagenum = JudgeInfoMessagenum.WRONG_ANSWER;
            }
        }
        // 5.3 判题题目的限制是否符合要求
        QuestionJudgeCconfig questionJudgeCconfig = JSONUtil.toBean(question.getJudgeConfig(), QuestionJudgeCconfig.class);
        Long timeLimit = questionJudgeCconfig.getTimeLimit();
        Long memoryLimit = questionJudgeCconfig.getMemoryLimit();

        Long memory = Optional.ofNullable(judgeInfo.getMemory()).orElse(500L);
        Long time = judgeInfo.getTime();
        if (memory > memoryLimit) {
            judgeInfoMessagenum = JudgeInfoMessagenum.MEMORY_LIMIT_EXCEEDED;
        }
        if (time > timeLimit) {
            judgeInfoMessagenum = JudgeInfoMessagenum.TIME_LIMIT_EXCEEDED;
        }
        QuestionSubmitJudgeInfo judgeInfoResponse = new QuestionSubmitJudgeInfo();
        judgeInfoResponse.setMessage(judgeInfoMessagenum.getValue());
        judgeInfoResponse.setMemory(memory);
        judgeInfoResponse.setTime(time);
        return judgeInfoResponse;
    }
}
