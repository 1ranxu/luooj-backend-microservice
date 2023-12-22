package com.luoying.luoojbackendserviceclient.service;


import com.luoying.luoojbackendmodel.entity.Question;
import com.luoying.luoojbackendmodel.entity.QuestionSubmit;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 题目服务
 */
@FeignClient(name = "luooj-backend-question-service", path = "/api/question/inner")
public interface QuestionFeignClient {
    @GetMapping("/get/id")
    Question getQuestionById(@RequestParam("questionId") long questionId);


    @GetMapping("/question_submit/get/id")
    QuestionSubmit getQuestionSubmitById(@RequestParam("questionSubmitId") long questionSubmitId);

    @PostMapping("/question_submit/update")
    Boolean updateQuestionSubmitById(@RequestBody QuestionSubmit questionSubmit);

    @PostMapping("/update")
    Boolean updateQuestionById(@RequestBody Question question);

    @PostMapping("/accepted_question/add")
    boolean addAcceptedQuestion(@RequestParam("tableName")String tableName, @RequestParam("questionId")long questionId);

    @GetMapping("/accepted_question/exist/table")
    boolean existAcceptedQuestionTable(@RequestParam("tableName") String tableName);


    @GetMapping("/accepted_question/drop/table")
    boolean dropAcceptedQuestionTable(@RequestParam("tableName") String tableName);


    @GetMapping("/accepted_question/create/table")
    boolean createAcceptedQuestionTable(@RequestParam("tableName") String tableName);


    @PostMapping("/question_submit/update/personal")
    boolean updateQuestionSubmit(@RequestParam("tableName") String tableName, @RequestBody QuestionSubmit questionSubmit);

    @GetMapping("/question_submit/exist/table")
    boolean existQuestionSubmitTable(@RequestParam("tableName") String tableName);


    @GetMapping("/question_submit/drop/table")
    boolean dropQuestionSubmitTable(@RequestParam("tableName") String tableName);


    @GetMapping("/question_submit/create/table")
    boolean createQuestionSubmitTable(@RequestParam("tableName") String tableName);

}
