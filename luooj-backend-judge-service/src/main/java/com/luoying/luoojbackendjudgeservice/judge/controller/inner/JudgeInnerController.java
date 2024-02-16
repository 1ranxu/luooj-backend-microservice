package com.luoying.luoojbackendjudgeservice.judge.controller.inner;

import com.luoying.luoojbackendjudgeservice.judge.JudgeService;
import com.luoying.luoojbackendjudgeservice.judge.sandbox.CodeSandBox;
import com.luoying.luoojbackendjudgeservice.judge.sandbox.CodeSandBoxFactory;
import com.luoying.luoojbackendjudgeservice.judge.sandbox.CodeSandBoxProxy;
import com.luoying.luoojbackendmodel.codesanbox.ExecuteCodeRequest;
import com.luoying.luoojbackendmodel.codesanbox.ExecuteCodeResponse;
import com.luoying.luoojbackendmodel.vo.QuestionSubmitVO;
import com.luoying.luoojbackendserviceclient.service.JudgeFeignClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
     * 判题
     * @param questionSubmitId 题目提交id
     */
    @Override
    @PostMapping("/do")
    public QuestionSubmitVO doJudge(long questionSubmitId) {
        return judgeService.doJudge(questionSubmitId);
    }

    /**
     * 在线运行
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
