package com.luoying.luoojbackendjudgeservice.judge;


import com.luoying.luoojbackendmodel.vo.QuestionSubmitVO;

/**
 * 怕媒体服务
 */
public interface JudgeService {

    /**
     * 判题
     * @param questionSubmitId
     * @return
     */
    QuestionSubmitVO doJudge(long questionSubmitId);
}
