package com.luoying.luoojbackendserviceclient.service;


import com.luoying.luoojbackendmodel.vo.QuestionSubmitVO;

/**
 * 判题服务
 */
public interface JudgeService {

    /**
     * 判题
     * @param questionSubmitId
     * @return
     */
    QuestionSubmitVO doJudge(long questionSubmitId);
}
