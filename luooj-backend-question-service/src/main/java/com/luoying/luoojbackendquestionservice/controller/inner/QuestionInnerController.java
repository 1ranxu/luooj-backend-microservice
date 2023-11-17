package com.luoying.luoojbackendquestionservice.controller.inner;

import com.luoying.luoojbackendmodel.entity.Question;
import com.luoying.luoojbackendmodel.entity.QuestionSubmit;
import com.luoying.luoojbackendserviceclient.service.QuestionFeignClient;
import com.luoying.luoojbackendquestionservice.service.QuestionService;
import com.luoying.luoojbackendquestionservice.service.QuestionSubmitService;

import javax.annotation.Resource;

/**
 * 该服务仅内部调用，不是给前端的
 */
public class QuestionInnerController implements QuestionFeignClient {

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Override
    public Question getQuestionById(long questionId) {
        return questionService.getById(questionId);
    }

    @Override
    public QuestionSubmit getQuestionSubmitById(long questionSubmitId) {
        return questionSubmitService.getById(questionSubmitId);
    }

    @Override
    public Boolean updateQuestionSubmitById(QuestionSubmit questionSubmit) {
        return questionSubmitService.updateById(questionSubmit);
    }
}
