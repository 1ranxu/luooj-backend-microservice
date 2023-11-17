package com.luoying.luoojbackendserviceclient.service;

import com.luoying.luoojbackendmodel.vo.QuestionSubmitVO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 判题服务
 */
@FeignClient(name = "luooj-backend-judge-service", path = "/api/judge/inner")
public interface JudgeFeignClient {
    @PostMapping("/do")
    QuestionSubmitVO doJudge(@RequestParam("questionSubmitId") long questionSubmitId);
}
