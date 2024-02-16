package com.luoying.luoojbackendserviceclient.service;


import com.luoying.luoojbackendmodel.entity.Question;
import com.luoying.luoojbackendmodel.entity.QuestionSubmit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * @author 落樱的悔恨
 * 题目服务
 */
@FeignClient(name = "luooj-backend-question-service", path = "/api/question/inner")
public interface QuestionFeignClient {
    /**
     * 根据questionId获取Question
     * @param questionId 题目id
     */
    @GetMapping("/get/id")
    Question getQuestionById(@RequestParam("questionId") long questionId);

    /**
     * 根据questionSubmitId获取QuestionSubmit
     * @param questionSubmitId 题目提交id
     */
    @GetMapping("/question_submit/get/id")
    QuestionSubmit getQuestionSubmitById(@RequestParam("questionSubmitId") long questionSubmitId);

    /**
     * 更新QuestionSubmit
     * @param questionSubmit {@link QuestionSubmit}
     */
    @PostMapping("/question_submit/update")
    Boolean updateQuestionSubmitById(@RequestBody QuestionSubmit questionSubmit);

    /**
     * 更新Question
     * @param question {@link Question}
     */
    @PostMapping("/update")
    Boolean updateQuestionById(@RequestBody Question question);

    /**
     * 个人通过表新增记录
     * @param tableName 表名
     * @param questionId 题目id
     */
    @PostMapping("/accepted_question/add")
    boolean addAcceptedQuestion(@RequestParam("tableName")String tableName, @RequestParam("questionId")long questionId);

    /**
     * 是否存在个人通过表
     * @param tableName 表名
     */
    @GetMapping("/accepted_question/exist/table")
    boolean existAcceptedQuestionTable(@RequestParam("tableName") String tableName);

    /**
     * 删除个人通过表
     * @param tableName 表名
     */
    @GetMapping("/accepted_question/drop/table")
    boolean dropAcceptedQuestionTable(@RequestParam("tableName") String tableName);

    /**
     * 创建个人通过表
     * @param tableName 表名
     */
    @GetMapping("/accepted_question/create/table")
    boolean createAcceptedQuestionTable(@RequestParam("tableName") String tableName);

    /**
     * 更新个人提交表记录
     * @param tableName 表名
     * @param questionSubmit 提交记录
     */
    @PostMapping("/question_submit/update/personal")
    boolean updateQuestionSubmit(@RequestParam("tableName") String tableName, @RequestBody QuestionSubmit questionSubmit);

    /**
     * 是否存在个人提交表
     * @param tableName 表名
     */
    @GetMapping("/question_submit/exist/table")
    boolean existQuestionSubmitTable(@RequestParam("tableName") String tableName);

    /**
     * 删除个人提交表
     * @param tableName 表名
     */
    @GetMapping("/question_submit/drop/table")
    boolean dropQuestionSubmitTable(@RequestParam("tableName") String tableName);

    /**
     * 创建个人提交表
     * @param tableName 表名
     */
    @GetMapping("/question_submit/create/table")
    boolean createQuestionSubmitTable(@RequestParam("tableName") String tableName);

}
