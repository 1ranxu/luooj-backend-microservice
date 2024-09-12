package com.luoying.luoojbackendquestionservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luoying.luoojbackendcommon.annotation.AuthCheck;
import com.luoying.luoojbackendcommon.common.BaseResponse;
import com.luoying.luoojbackendcommon.common.ResultUtils;
import com.luoying.luoojbackendcommon.constant.UserConstant;
import com.luoying.luoojbackendmodel.dto.question_solution_collect.QuestionSolutionCollectAddRequest;
import com.luoying.luoojbackendmodel.dto.question_solution_collect.QuestionSolutionCollectQueryRequest;
import com.luoying.luoojbackendmodel.entity.QuestionSolutionCollect;
import com.luoying.luoojbackendquestionservice.service.QuestionSolutionCollectService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/10 19:50
 */
@RestController
@RequestMapping("/question_solution_collect")
public class QuestionSolutionCollectController {
    @Resource
    private QuestionSolutionCollectService questionSolutionCollectService;

    /**
     * 收藏题解
     * @param questionSolutionCollectAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addQuestionSolutionCollect(@RequestBody QuestionSolutionCollectAddRequest questionSolutionCollectAddRequest, HttpServletRequest request) {
        return ResultUtils.success(questionSolutionCollectService.addQuestionSolutionCollect(questionSolutionCollectAddRequest, request));
    }

    /**
     * 取消收藏题解
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestionSolutionCollect(@RequestParam("id") Long id, HttpServletRequest request) {
        return ResultUtils.success(questionSolutionCollectService.deleteQuestionSolutionCollect(id, request));
    }

    /**
     * 分页获取题解收藏（仅管理员）
     * @param questionSolutionCollectQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<QuestionSolutionCollect>> listQuestionSolutionCollectByPage(@RequestBody QuestionSolutionCollectQueryRequest questionSolutionCollectQueryRequest) {
        return ResultUtils.success(questionSolutionCollectService.listQuestionSolutionCollectByPage(questionSolutionCollectQueryRequest));
    }

    /**
     * 分页获取题解收藏
     * @param questionSolutionCollectQueryRequest
     * @return
     */
    @PostMapping("/list/page/user")
    public BaseResponse<Page<QuestionSolutionCollect>> listQuestionSolutionCollectByPageUser(@RequestBody QuestionSolutionCollectQueryRequest questionSolutionCollectQueryRequest) {
        return ResultUtils.success(questionSolutionCollectService.listQuestionSolutionCollectByPageUser(questionSolutionCollectQueryRequest));
    }
}
