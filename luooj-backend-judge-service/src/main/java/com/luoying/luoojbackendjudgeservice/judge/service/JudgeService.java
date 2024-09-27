package com.luoying.luoojbackendjudgeservice.judge.service;


import com.luoying.luoojbackendmodel.dto.contest_result.ContestQuestionSubmit;
import com.luoying.luoojbackendmodel.dto.question_submit.QuestionSubmitJudgeInfo;
import com.luoying.luoojbackendmodel.vo.QuestionSubmitVO;

/**
 * @author 落樱的悔恨
 * 判题服务
 */
public interface JudgeService {

    /**
     * 普通判题
     * @param questionSubmitId 题目提交id
     */
    QuestionSubmitVO doJudge(long questionSubmitId);


    /**
     * 竞赛判题
     * @param contestQuestionSubmit
     * @return
     */
    QuestionSubmitJudgeInfo doJudge(ContestQuestionSubmit contestQuestionSubmit);
}
