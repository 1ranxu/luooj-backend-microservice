package com.luoying.luoojbackendjudgeservice.judge.strategy;


import com.luoying.luoojbackendmodel.dto.question_submit.QuestionSubmitJudgeInfo;

/**
 * @author 落樱的悔恨
 * 判题策略
 */
public interface JudgeStrategy {
    /**
     * 执行判题
     * @param judgeContext 判题上下文
     */
    QuestionSubmitJudgeInfo doJudge(JudgeContext judgeContext);
}
