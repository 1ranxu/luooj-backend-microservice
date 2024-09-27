package com.luoying.luoojbackendserviceclient.service;

import com.luoying.luoojbackendmodel.codesanbox.ExecuteCodeRequest;
import com.luoying.luoojbackendmodel.codesanbox.ExecuteCodeResponse;
import com.luoying.luoojbackendmodel.dto.contest_result.ContestQuestionSubmit;
import com.luoying.luoojbackendmodel.dto.question_submit.QuestionSubmitJudgeInfo;
import com.luoying.luoojbackendmodel.vo.QuestionSubmitVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author 落樱的悔恨
 * 判题服务
 */
@FeignClient(name = "luooj-backend-judge-service", path = "/api/judge/inner")
public interface JudgeFeignClient {
    /**
     * 普通判题
     *
     * @param questionSubmitId 题目提交id
     */
    @PostMapping("/common")
    QuestionSubmitVO commonJudge(@RequestParam("questionSubmitId") long questionSubmitId);

    /**
     * 竞赛判题
     * @param contestQuestionSubmit
     * @return
     */
    @PostMapping("/contest")
    public QuestionSubmitJudgeInfo contestJudge(@RequestBody ContestQuestionSubmit contestQuestionSubmit);

    /**
     * 在线运行
     * @param executeCodeRequest 执行代码请求
     */
    @PostMapping("/run")
    ExecuteCodeResponse runOnline(@RequestBody ExecuteCodeRequest executeCodeRequest);
}
