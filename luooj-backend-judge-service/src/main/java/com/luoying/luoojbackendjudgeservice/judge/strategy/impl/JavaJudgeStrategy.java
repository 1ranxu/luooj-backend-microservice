package com.luoying.luoojbackendjudgeservice.judge.strategy.impl;

import cn.hutool.json.JSONUtil;
import com.luoying.luoojbackendjudgeservice.judge.strategy.JudgeStrategy;
import com.luoying.luoojbackendjudgeservice.judge.strategy.context.JudgeContext;
import com.luoying.luoojbackendmodel.codesanbox.ExecuteCodeResponse;
import com.luoying.luoojbackendmodel.dto.question.QuestionJudgeCase;
import com.luoying.luoojbackendmodel.dto.question.QuestionJudgeCconfig;
import com.luoying.luoojbackendmodel.dto.question_submit.QuestionSubmitJudgeInfo;
import com.luoying.luoojbackendmodel.entity.Question;
import com.luoying.luoojbackendmodel.enums.JudgeInfoMessagenum;

import java.util.List;
import java.util.Optional;

/**
 * @author 落樱的悔恨
 * Java判题策略
 */
public class JavaJudgeStrategy implements JudgeStrategy {
    /**
     * 执行判题
     *
     * @param judgeContext 判题上下文信息
     */
    @Override
    public QuestionSubmitJudgeInfo doJudge(JudgeContext judgeContext) {
        // 获取上下文信息
        ExecuteCodeResponse executeCodeResponse = judgeContext.getExecuteCodeResponse();
        QuestionSubmitJudgeInfo judgeInfo = executeCodeResponse.getJudgeInfo();
        List<String> inputList = judgeContext.getInputList();
        List<String> outputList = judgeContext.getOutputList();
        Question question = judgeContext.getQuestion();
        List<QuestionJudgeCase> judgeCaseList = judgeContext.getJudgeCaseList();

        // 5 根据沙箱的执行结果，设置题目的判题状态和信息
        JudgeInfoMessagenum judgeInfoMessagenum = JudgeInfoMessagenum.ACCEPTED;
        // 如果judgeInfo.getMessage()不为空，说明代码沙箱执行代码的过程中有异常
        if (judgeInfo.getMessage() != null) {
            judgeInfoMessagenum = JudgeInfoMessagenum.getEnumByValue(executeCodeResponse.getMessage());
            QuestionSubmitJudgeInfo judgeInfoResponse = new QuestionSubmitJudgeInfo();
            judgeInfoResponse.setMessage(judgeInfoMessagenum == null ? judgeInfo.getMessage() : judgeInfoMessagenum.getValue());
            judgeInfoResponse.setMemory(Optional.ofNullable(judgeInfo.getMemory()).orElse(-1L));
            judgeInfoResponse.setTime(Optional.ofNullable(judgeInfo.getTime()).orElse(-1L));
            return judgeInfoResponse;
        }
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

        Long memory = judgeInfo.getMemory();
        Long time = judgeInfo.getTime();
        if (memory > memoryLimit) {
            judgeInfoMessagenum = JudgeInfoMessagenum.MEMORY_LIMIT_EXCEEDED;
        }
        // java程序需要额外执行1秒钟
        long JAVA_EXTRA_TIME_COST = 1000L;
        if (time - JAVA_EXTRA_TIME_COST > timeLimit) {
            judgeInfoMessagenum = JudgeInfoMessagenum.TIME_LIMIT_EXCEEDED;
        }
        QuestionSubmitJudgeInfo judgeInfoResponse = new QuestionSubmitJudgeInfo();
        judgeInfoResponse.setMessage(judgeInfoMessagenum.getValue());
        judgeInfoResponse.setMemory(memory);
        judgeInfoResponse.setTime(time);
        return judgeInfoResponse;
    }
}
