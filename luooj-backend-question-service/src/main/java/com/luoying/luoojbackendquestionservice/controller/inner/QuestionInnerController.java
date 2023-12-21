package com.luoying.luoojbackendquestionservice.controller.inner;

import com.luoying.luoojbackendmodel.entity.Question;
import com.luoying.luoojbackendmodel.entity.QuestionSubmit;
import com.luoying.luoojbackendquestionservice.mapper.AcceptedQuestionMapper;
import com.luoying.luoojbackendquestionservice.mapper.QuestionSubmitMapper;
import com.luoying.luoojbackendquestionservice.service.QuestionService;
import com.luoying.luoojbackendquestionservice.service.QuestionSubmitService;
import com.luoying.luoojbackendserviceclient.service.QuestionFeignClient;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
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
    private AcceptedQuestionMapper acceptedQuestionMapper;

    @Resource
    private QuestionSubmitMapper questionSubmitMapper;

    @Override
    @GetMapping("/get/id")

    public Question getQuestionById(long questionId) {
        return questionService.getById(questionId);
    }

    @Override
    @GetMapping("/question_submit/get/id")
    public QuestionSubmit getQuestionSubmitById(long questionSubmitId) {
        return questionSubmitService.getById(questionSubmitId);
    }

    @Override
    @PostMapping("/question_submit/update")
    public Boolean updateQuestionSubmitById(QuestionSubmit questionSubmit) {
        return questionSubmitService.updateById(questionSubmit);
    }

    @Override
    @PostMapping("/update")
    public Boolean updateQuestionById(@RequestBody Question question) {
        return questionService.updateById(question);
    }

    @Override
    @GetMapping("/accepted_question/exist/table")
    public boolean existAcceptedQuestionTable(String tableName) {
        return acceptedQuestionMapper.existAcceptedQuestionTable(tableName) == 1;
    }

    @Override
    @GetMapping("/accepted_question/drop/table")
    public boolean dropAcceptedQuestionTable(String tableName) {
        return acceptedQuestionMapper.dropAcceptedQuestionTable(tableName) == 0;
    }

    @Override
    @GetMapping("/accepted_question/create/table")
    public boolean createAcceptedQuestionTable(String tableName) {
        return acceptedQuestionMapper.createAcceptedQuestionTable(tableName) == 0;
    }

    @Override
    @GetMapping("/question_submit/exist/table")
    public boolean existQuestionSubmitTable(String tableName) {
        return questionSubmitMapper.existQuestionSubmitTable(tableName) == 1;
    }

    @Override
    @GetMapping("/question_submit/drop/table")
    public boolean dropQuestionSubmitTable(String tableName) {
        return questionSubmitMapper.dropQuestionSubmitTable(tableName) == 0;
    }

    @Override
    @GetMapping("/question_submit/create/table")
    public boolean createQuestionSubmitTable(String tableName) {
        return questionSubmitMapper.createQuestionSubmitTable(tableName) == 0;
    }

}
