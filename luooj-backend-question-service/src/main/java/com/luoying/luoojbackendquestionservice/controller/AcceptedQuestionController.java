package com.luoying.luoojbackendquestionservice.controller;

import com.luoying.luoojbackendcommon.common.BaseResponse;
import com.luoying.luoojbackendcommon.common.ResultUtils;
import com.luoying.luoojbackendquestionservice.service.AcceptedQuestionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/8 13:42
 */
@RestController
@RequestMapping("/accepted_question")
public class AcceptedQuestionController {
    @Resource
    private AcceptedQuestionService acceptedQuestionService;

    /**
     * 判断当前用户是否通过了某道题目
     * @param questionId
     * @param request
     * @return
     */
    @GetMapping("/isAccepted")
    public BaseResponse<Boolean> isisAccepted(@RequestParam("questionId") Long questionId, HttpServletRequest request){
        return ResultUtils.success(acceptedQuestionService.isisAccepted(questionId, request));
    }
}