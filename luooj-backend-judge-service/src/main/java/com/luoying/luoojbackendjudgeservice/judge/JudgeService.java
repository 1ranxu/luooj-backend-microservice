package com.luoying.luoojbackendjudgeservice.judge;


import com.luoying.luoojbackendmodel.vo.QuestionSubmitVO;

/**
 * @author 落樱的悔恨
 * 判题服务
 */
public interface JudgeService {

    /**
     * 判题
     * @param questionSubmitId 题目提交id
     */
    QuestionSubmitVO doJudge(long questionSubmitId);
}
