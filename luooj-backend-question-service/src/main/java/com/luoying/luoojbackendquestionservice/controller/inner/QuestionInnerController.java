package com.luoying.luoojbackendquestionservice.controller.inner;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.luoying.luoojbackendmodel.entity.AcceptedQuestion;
import com.luoying.luoojbackendmodel.entity.Question;
import com.luoying.luoojbackendmodel.entity.QuestionSubmit;
import com.luoying.luoojbackendquestionservice.service.AcceptedQuestionService;
import com.luoying.luoojbackendquestionservice.service.QuestionService;
import com.luoying.luoojbackendquestionservice.service.QuestionSubmitService;
import com.luoying.luoojbackendserviceclient.service.QuestionFeignClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * @author 落樱的悔恨
 * 该服务仅内部调用，不是给前端的
 */
@RestController
@RequestMapping("/inner")
public class QuestionInnerController implements QuestionFeignClient {

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private AcceptedQuestionService acceptedQuestionService;

    /**
     * 根据questionId获取Question
     *
     * @param questionId 题目id
     */
    @Override
    @GetMapping("/get/id")
    public Question getQuestionById(@RequestParam("questionId") long questionId) {
        return questionService.getById(questionId);
    }

    /**
     * 根据questionSubmitId获取QuestionSubmit
     *
     * @param questionSubmitId 题目提交id
     */
    @Override
    @GetMapping("/question_submit/get/id")
    public QuestionSubmit getQuestionSubmitById(@RequestParam("questionSubmitId") long questionSubmitId) {
        return questionSubmitService.getById(questionSubmitId);
    }

    /**
     * 更新QuestionSubmit
     *
     * @param questionSubmit {@link QuestionSubmit}
     */
    @Override
    @PostMapping("/question_submit/update")
    public Boolean updateQuestionSubmitById(@RequestBody QuestionSubmit questionSubmit) {
        return questionSubmitService.updateById(questionSubmit);
    }

    /**
     * 更新Question
     *
     * @param question {@link Question}
     */
    @Override
    @PostMapping("/update")
    public Boolean updateQuestionById(@RequestBody Question question) {
        return questionService.updateById(question);
    }

    /**
     * 添加通过记录
     *
     * @param questionId
     * @param userId
     * @return
     */
    @Override
    @PostMapping("/accepted_question/add")
    public Boolean addAcceptedQuestion(long questionId, long userId) {
        AcceptedQuestion acceptedQuestion = new AcceptedQuestion();
        acceptedQuestion.setQuestionId(questionId);
        acceptedQuestion.setUserId(userId);
        return acceptedQuestionService.save(acceptedQuestion);
    }

    /**
     * 查询通过记录
     *
     * @param questionId
     * @param userId
     * @return
     */
    @PostMapping("/accepted_question/get")
    public AcceptedQuestion getAcceptedQuestion(@RequestParam("questionId") long questionId, @RequestParam("userId") long userId) {
        LambdaQueryWrapper<AcceptedQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AcceptedQuestion::getQuestionId, questionId).eq(AcceptedQuestion::getUserId, userId);
        return acceptedQuestionService.getOne(queryWrapper);
    }
}
