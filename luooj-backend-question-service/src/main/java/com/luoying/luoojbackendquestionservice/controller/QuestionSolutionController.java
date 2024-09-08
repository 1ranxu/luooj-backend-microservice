package com.luoying.luoojbackendquestionservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luoying.luoojbackendcommon.annotation.AuthCheck;
import com.luoying.luoojbackendcommon.common.BaseResponse;
import com.luoying.luoojbackendcommon.common.DeleteRequest;
import com.luoying.luoojbackendcommon.common.ResultUtils;
import com.luoying.luoojbackendcommon.constant.UserConstant;
import com.luoying.luoojbackendmodel.dto.question_solution.QuestionSolutionAddRequest;
import com.luoying.luoojbackendmodel.dto.question_solution.QuestionSolutionQueryRequest;
import com.luoying.luoojbackendmodel.dto.question_solution.QuestionSolutionUpdateRequest;
import com.luoying.luoojbackendmodel.entity.QuestionSolution;
import com.luoying.luoojbackendquestionservice.service.QuestionSolutionService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/7 20:13
 */
@RequestMapping("/question_solution")
@RestController
public class QuestionSolutionController {
    @Resource
    private QuestionSolutionService questionSolutionService;

    /**
     * 创建题解
     *
     * @param questionSolutionAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addQuestionSolution(@RequestBody QuestionSolutionAddRequest questionSolutionAddRequest, HttpServletRequest request) {
        return ResultUtils.success(questionSolutionService.addQuestionSolution(questionSolutionAddRequest, request));
    }


    /**
     * 删除题解
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestionSolution(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        return ResultUtils.success(questionSolutionService.deleteQuestionSolution(deleteRequest, request));
    }

    /**
     * 更新题解
     *
     * @param questionSolutionUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateQuestionSolution(@RequestBody QuestionSolutionUpdateRequest questionSolutionUpdateRequest, HttpServletRequest request) {
        return ResultUtils.success(questionSolutionService.updateQuestionSolution(questionSolutionUpdateRequest, request));
    }

    /**
     * 查询题解
     *
     * @param id
     * @return
     */
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    @PostMapping("/get")
    public BaseResponse<QuestionSolution> getQuestionSolutionById(@RequestParam("id") Long id) {
        return ResultUtils.success(questionSolutionService.getQuestionSolutionById(id));
    }

    /**
     * 分页获取题解列表（仅管理员）
     *
     * @param questionSolutionQueryRequest 题解查询请求
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<QuestionSolution>> listQuestionSolutionByPage(@RequestBody QuestionSolutionQueryRequest questionSolutionQueryRequest) {
        return ResultUtils.success(questionSolutionService.listQuestionSolutionByPage(questionSolutionQueryRequest));
    }

    /**
     * 分页获取题解列表（普通用户）
     * 只传userId，就可实现分页获取当前用户创建的题解列表
     * 只传questionId，就可实现分页获取某题目下的题解列表
     * 也可以根据点赞数，评论数，收藏数，创建时间排序
     * @param questionSolutionQueryRequest 题解查询请求
     */
    @PostMapping("/list/page/user")
    public BaseResponse<Page<QuestionSolution>> listQuestionSolutionByPageUser(@RequestBody QuestionSolutionQueryRequest questionSolutionQueryRequest) {
        return ResultUtils.success(questionSolutionService.listQuestionSolutionByPageUser(questionSolutionQueryRequest));
    }

}
