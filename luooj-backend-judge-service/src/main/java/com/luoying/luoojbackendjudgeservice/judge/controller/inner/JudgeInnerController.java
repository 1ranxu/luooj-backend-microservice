package com.luoying.luoojbackendjudgeservice.judge.controller.inner;

import com.luoying.luoojbackendjudgeservice.judge.JudgeService;
import com.luoying.luoojbackendmodel.vo.QuestionSubmitVO;
import com.luoying.luoojbackendserviceclient.service.JudgeFeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 该服务仅内部调用，不是给前端的
 */
@RestController
@RequestMapping("/inner")
public class JudgeInnerController implements JudgeFeignClient {
    @Resource
    private JudgeService judgeService;

    @Override
    public QuestionSubmitVO doJudge(long questionSubmitId) {
        return judgeService.doJudge(questionSubmitId);
    }
}
