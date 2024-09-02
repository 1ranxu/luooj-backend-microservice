package com.luoying.luoojbackendserviceclient.service;


import com.luoying.luoojbackendmodel.entity.AcceptedQuestion;
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
     * 添加通过记录
     * @param questionId
     * @param userId
     * @return
     */
    @PostMapping("/accepted_question/add")
    Boolean addAcceptedQuestion(@RequestParam("questionId") long questionId,@RequestParam("userId") long userId);

    /**
     * 查询通过记录
     * @param questionId
     * @param userId
     * @return
     */
    @PostMapping("/accepted_question/get")
    AcceptedQuestion getAcceptedQuestion(@RequestParam("questionId") long questionId, @RequestParam("userId") long userId);

}
