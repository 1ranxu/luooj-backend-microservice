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
    private AcceptedQuestionMapper acceptedQuestionMapper;

    @Resource
    private QuestionSubmitMapper questionSubmitMapper;

    /**
     * 根据questionId获取Question
     * @param questionId 题目id
     */
    @Override
    @GetMapping("/get/id")
    public Question getQuestionById(long questionId) {
        return questionService.getById(questionId);
    }

    /**
     * 根据questionSubmitId获取QuestionSubmit
     * @param questionSubmitId 题目提交id
     */
    @Override
    @GetMapping("/question_submit/get/id")
    public QuestionSubmit getQuestionSubmitById(long questionSubmitId) {
        return questionSubmitService.getById(questionSubmitId);
    }

    /**
     * 更新QuestionSubmit
     * @param questionSubmit {@link QuestionSubmit}
     */
    @Override
    @PostMapping("/question_submit/update")
    public Boolean updateQuestionSubmitById(QuestionSubmit questionSubmit) {
        return questionSubmitService.updateById(questionSubmit);
    }

    /**
     * 更新Question
     * @param question {@link Question}
     */
    @Override
    @PostMapping("/update")
    public Boolean updateQuestionById(@RequestBody Question question) {
        return questionService.updateById(question);
    }

    /**
     * 个人通过表新增记录
     * @param tableName 表名
     * @param questionId 题目id
     */
    @Override
    @PostMapping("/accepted_question/add")
    public boolean addAcceptedQuestion(String tableName, long questionId) {
        return acceptedQuestionMapper.addAcceptedQuestion(tableName, questionId) == 1;
    }

    /**
     * 是否存在个人通过表
     * @param tableName 表名
     */
    @Override
    @GetMapping("/accepted_question/exist/table")
    public boolean existAcceptedQuestionTable(String tableName) {
        return acceptedQuestionMapper.existAcceptedQuestionTable(tableName) == 1;
    }

    /**
     * 删除个人通过表
     * @param tableName 表名
     */
    @Override
    @GetMapping("/accepted_question/drop/table")
    public boolean dropAcceptedQuestionTable(String tableName) {
        return acceptedQuestionMapper.dropAcceptedQuestionTable(tableName) == 0;
    }

    /**
     * 创建个人通过表
     * @param tableName 表名
     */
    @Override
    @GetMapping("/accepted_question/create/table")
    public boolean createAcceptedQuestionTable(String tableName) {
        return acceptedQuestionMapper.createAcceptedQuestionTable(tableName) == 0;
    }

    /**
     * 更新个人提交表记录
     * @param tableName 表名
     * @param questionSubmit 提交记录
     */
    @Override
    @PostMapping("/question_submit/update/personal")
    public boolean updateQuestionSubmit(String tableName, QuestionSubmit questionSubmit) {
        return questionSubmitMapper.updateQuestionSubmit(tableName, questionSubmit) == 1;
    }

    /**
     * 是否存在个人提交表
     * @param tableName 表名
     */
    @Override
    @GetMapping("/question_submit/exist/table")
    public boolean existQuestionSubmitTable(String tableName) {
        return questionSubmitMapper.existQuestionSubmitTable(tableName) == 1;
    }

    /**
     * 删除个人提交表
     * @param tableName 表名
     */
    @Override
    @GetMapping("/question_submit/drop/table")
    public boolean dropQuestionSubmitTable(String tableName) {
        return questionSubmitMapper.dropQuestionSubmitTable(tableName) == 0;
    }

    /**
     * 创建个人提交表
     * @param tableName 表名
     */
    @Override
    @GetMapping("/question_submit/create/table")
    public boolean createQuestionSubmitTable(String tableName) {
        return questionSubmitMapper.createQuestionSubmitTable(tableName) == 0;
    }

}
