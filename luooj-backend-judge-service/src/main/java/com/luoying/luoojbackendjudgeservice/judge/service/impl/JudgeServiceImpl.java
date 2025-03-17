package com.luoying.luoojbackendjudgeservice.judge.service.impl;


import cn.hutool.json.JSONUtil;
import com.luoying.luoojbackendcommon.common.ErrorCode;
import com.luoying.luoojbackendcommon.exception.BusinessException;
import com.luoying.luoojbackendjudgeservice.judge.sandbox.CodeSandBox;
import com.luoying.luoojbackendjudgeservice.judge.sandbox.factory.CodeSandBoxFactory;
import com.luoying.luoojbackendjudgeservice.judge.sandbox.proxy.CodeSandBoxProxy;
import com.luoying.luoojbackendjudgeservice.judge.service.JudgeService;
import com.luoying.luoojbackendjudgeservice.judge.strategy.context.JudgeContext;
import com.luoying.luoojbackendjudgeservice.judge.strategy.manager.JudgeManager;
import com.luoying.luoojbackendmodel.codesanbox.ExecuteCodeRequest;
import com.luoying.luoojbackendmodel.codesanbox.ExecuteCodeResponse;
import com.luoying.luoojbackendmodel.dto.contest_result.ContestQuestionSubmit;
import com.luoying.luoojbackendmodel.dto.question.QuestionJudgeCase;
import com.luoying.luoojbackendmodel.dto.question_submit.QuestionSubmitJudgeInfo;
import com.luoying.luoojbackendmodel.entity.Question;
import com.luoying.luoojbackendmodel.entity.QuestionSubmit;
import com.luoying.luoojbackendmodel.enums.QuestionSubmitStatusEnum;
import com.luoying.luoojbackendmodel.vo.QuestionSubmitVO;
import com.luoying.luoojbackendserviceclient.service.QuestionFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author 落樱的悔恨
 * 判题服务实现
 */
@Service
@Slf4j
public class JudgeServiceImpl implements JudgeService {
    @Resource
    private QuestionFeignClient questionFeignClient;

    @Resource
    private JudgeManager judgeManager;

    @Value("${codesandbox.type:example}")
    private String type;

    /**
     * 普通判题
     * @param questionSubmitId 题目提交id
     */
    @Override
    public QuestionSubmitVO doJudge(long questionSubmitId) {
        // 1 传入题目的提交 id，获取到对应的题目、提交信息（包含代码、编程语言等）
        QuestionSubmit questionSubmit = questionFeignClient.getQuestionSubmitById(questionSubmitId);
        if (questionSubmit == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "提交记录不存在");
        }
        Long questionId = questionSubmit.getQuestionId();
        Question question = questionFeignClient.getQuestionById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        // 2 如果题目提交状态不为等待中，就不用重复执行了
        if (!questionSubmit.getStatus().equals(QuestionSubmitStatusEnum.WAITING.getValue())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "已在判题");
        }
        // 3 更改判题（题目提交表）的状态为 “判题中”，防止重复执行，也能让用户即时看到状态
        QuestionSubmit questionSubmitUpdate = new QuestionSubmit();
        questionSubmitUpdate.setId(questionSubmitId);
        questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.RUNNING.getValue());
        boolean update = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
        if (!update) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "判题状态更新失败");
        }
        // 4、调用沙箱，获取到执行结果
        String code = questionSubmit.getCode();
        // 获取输入用例
        List<QuestionJudgeCase> judgeCaseList = JSONUtil.toList(question.getJudgeCase(), QuestionJudgeCase.class);
        List<String> inputList = judgeCaseList.stream().map(QuestionJudgeCase::getInput).collect(Collectors.toList());
        // 获取编程语言
        String language = questionSubmit.getLanguage();
        // 使用代码沙箱工厂获取代码沙箱
        CodeSandBox codeSandBox = CodeSandBoxFactory.newInstance(type);
        // 使用代码沙箱代理增强代码沙箱
        CodeSandBoxProxy codeSandBoxProxy = new CodeSandBoxProxy(codeSandBox);
        // 构造执行代码请求
        ExecuteCodeRequest codeRequest = ExecuteCodeRequest.builder()
                .inputList(inputList)
                .code(code)
                .language(language)
                .build();
        // 执行代码
        ExecuteCodeResponse executeCodeResponse = codeSandBoxProxy.executeCode(codeRequest);
        List<String> outputList = executeCodeResponse.getOutputList();

        // 5、根据沙箱的执行结果，设置判题上下文
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setOutputList(outputList);
        judgeContext.setInputList(inputList);
        judgeContext.setJudgeCaseList(judgeCaseList);
        judgeContext.setQuestion(question);
        judgeContext.setExecuteCodeResponse(executeCodeResponse);
        judgeContext.setLanguage(questionSubmit.getLanguage());
        // 使用判题管理进行判题
        QuestionSubmitJudgeInfo judgeInfo = judgeManager.doJudge(judgeContext);
        // 修改提交记录表的判题状态和判题信息
        questionSubmitUpdate = new QuestionSubmit();
        if (executeCodeResponse.getOutputList() == null){
            questionSubmitUpdate.setId(questionSubmitId);
            questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.FAILURE.getValue());
            questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
            update = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
            if (!update) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "判题状态更新失败");
            }
        }else{
            questionSubmitUpdate.setId(questionSubmitId);
            questionSubmitUpdate.setStatus(QuestionSubmitStatusEnum.SUCCESS.getValue());
            questionSubmitUpdate.setJudgeInfo(JSONUtil.toJsonStr(judgeInfo));
            update = questionFeignClient.updateQuestionSubmitById(questionSubmitUpdate);
            if (!update) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "判题状态更新失败");
            }
        }

        return QuestionSubmitVO.objToVo(questionFeignClient.getQuestionSubmitById(questionSubmitId));
    }

    /**
     * 竞赛判题
     *
     * @param contestQuestionSubmit
     * @return
     */
    @Override
    public QuestionSubmitJudgeInfo doJudge(ContestQuestionSubmit contestQuestionSubmit) {
        // 1.获取题目
        Long questionId = contestQuestionSubmit.getQuestionId();
        Question question = questionFeignClient.getQuestionById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR, "题目不存在");
        }
        // 2、调用沙箱，获取到执行结果
        String code = contestQuestionSubmit.getCode();
        // 获取输入用例
        List<QuestionJudgeCase> judgeCaseList = JSONUtil.toList(question.getJudgeCase(), QuestionJudgeCase.class);
        List<String> inputList = judgeCaseList.stream().map(QuestionJudgeCase::getInput).collect(Collectors.toList());
        // 获取编程语言
        String language = contestQuestionSubmit.getLanguage();
        // 使用代码沙箱工厂获取代码沙箱
        CodeSandBox codeSandBox = CodeSandBoxFactory.newInstance(type);
        // 使用代码沙箱代理增强代码沙箱
        CodeSandBoxProxy codeSandBoxProxy = new CodeSandBoxProxy(codeSandBox);
        // 构造执行代码请求
        ExecuteCodeRequest codeRequest = ExecuteCodeRequest.builder()
                .inputList(inputList)
                .code(code)
                .language(language)
                .build();
        // 执行代码
        ExecuteCodeResponse executeCodeResponse = codeSandBoxProxy.executeCode(codeRequest);
        List<String> outputList = executeCodeResponse.getOutputList();

        // 5、根据沙箱的执行结果，设置判题上下文
        JudgeContext judgeContext = new JudgeContext();
        judgeContext.setOutputList(outputList);
        judgeContext.setInputList(inputList);
        judgeContext.setJudgeCaseList(judgeCaseList);
        judgeContext.setQuestion(question);
        judgeContext.setExecuteCodeResponse(executeCodeResponse);
        judgeContext.setLanguage(contestQuestionSubmit.getLanguage());
        // 使用判题管理进行判题
        return judgeManager.doJudge(judgeContext);
    }
}
