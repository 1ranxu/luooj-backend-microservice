package com.luoying.luoojbackendjudgeservice.judge.controller.inner;

import com.luoying.luoojbackendjudgeservice.judge.sandbox.CodeSandBox;
import com.luoying.luoojbackendjudgeservice.judge.sandbox.factory.CodeSandBoxFactory;
import com.luoying.luoojbackendjudgeservice.judge.sandbox.proxy.CodeSandBoxProxy;
import com.luoying.luoojbackendjudgeservice.judge.service.JudgeService;
import com.luoying.luoojbackendmodel.codesanbox.ExecuteCodeRequest;
import com.luoying.luoojbackendmodel.codesanbox.ExecuteCodeResponse;
import com.luoying.luoojbackendmodel.dto.contest_result.ContestQuestionSubmit;
import com.luoying.luoojbackendmodel.dto.question_submit.QuestionSubmitJudgeInfo;
import com.luoying.luoojbackendmodel.vo.QuestionSubmitVO;
import com.luoying.luoojbackendserviceclient.service.JudgeFeignClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author 落樱的悔恨
 * 该服务仅内部调用，不是给前端的
 */
@RestController
@RequestMapping("/inner")
public class JudgeInnerController implements JudgeFeignClient {
    @Resource
    private JudgeService judgeService;

    @Value("${codesandbox.type:example}")
    private String type;

    /**
     * 普通判题
     *
     * @param questionSubmitId 题目提交id
     */
    @Override
    @PostMapping("/common")
    public QuestionSubmitVO commonJudge(@RequestParam("questionSubmitId") long questionSubmitId) {
        return judgeService.doJudge(questionSubmitId);
    }

    /**
     * 竞赛判题
     * @param contestQuestionSubmit
     * @return
     */
    @Override
    @PostMapping("/contest")
    public QuestionSubmitJudgeInfo contestJudge(@RequestBody ContestQuestionSubmit contestQuestionSubmit) {
        return judgeService.doJudge(contestQuestionSubmit);
    }

    /**
     * 在线运行
     *
     * @param executeCodeRequest 执行代码请求
     */
    @Override
    @PostMapping("/run")
    public ExecuteCodeResponse runOnline(@RequestBody ExecuteCodeRequest executeCodeRequest) {
        CodeSandBox codeSandBox = CodeSandBoxFactory.newInstance(type);
        CodeSandBoxProxy codeSandBoxProxy = new CodeSandBoxProxy(codeSandBox);
        ExecuteCodeResponse executeCodeResponse = codeSandBoxProxy.executeCode(executeCodeRequest);
        return executeCodeResponse;
    }
}
