package com.luoying.luoojbackendquestionservice.controller;

import com.luoying.luoojbackendcommon.common.BaseResponse;
import com.luoying.luoojbackendcommon.common.ResultUtils;
import com.luoying.luoojbackendmodel.dto.questionsubmit.QuestionSubmitDetail;
import com.luoying.luoojbackendquestionservice.service.QuestionSubmitService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/5 19:07
 */
@RestController
@RequestMapping("/question_submit")
@Slf4j
public class QuestionSubmitController {
    @Resource
    private QuestionSubmitService questionSubmitService;

    /**
     * 获取个人提交详情
     * @param request
     * @return
     */
    @GetMapping("/get/person_submit/detail")
    public BaseResponse<QuestionSubmitDetail> getPersonSubmitDetail(HttpServletRequest request){
        return ResultUtils.success(questionSubmitService.getPersonSubmitDetail(request));
    }
}
