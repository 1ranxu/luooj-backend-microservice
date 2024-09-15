package com.luoying.luoojbackendquestionservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luoying.luoojbackendcommon.annotation.AuthCheck;
import com.luoying.luoojbackendcommon.common.BaseResponse;
import com.luoying.luoojbackendcommon.common.ResultUtils;
import com.luoying.luoojbackendcommon.constant.UserConstant;
import com.luoying.luoojbackendmodel.dto.question_solution_report.QuestionSolutionReportAddRequest;
import com.luoying.luoojbackendmodel.dto.question_solution_report.QuestionSolutionReportQueryRequest;
import com.luoying.luoojbackendmodel.dto.question_solution_report.SolutionDeleteRequest;
import com.luoying.luoojbackendmodel.entity.QuestionSolutionReport;
import com.luoying.luoojbackendquestionservice.service.QuestionSolutionReportService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/15 17:34
 */
@RestController
@RequestMapping("/question_solution_report")
public class QuestionSolutionReportController {
    @Resource
    private QuestionSolutionReportService questionSolutionReportService;

    /**
     * 新增题解举报
     *
     * @param questionSolutionReportAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addQuestionSolutionReport(@RequestBody QuestionSolutionReportAddRequest questionSolutionReportAddRequest, HttpServletRequest request) {
        return ResultUtils.success(questionSolutionReportService.addQuestionSolutionReport(questionSolutionReportAddRequest, request));
    }

    /**
     * 删除题解举报
     *
     * @param id
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteQuestionSolutionReport(@RequestParam("id") Long id) {
        return ResultUtils.success(questionSolutionReportService.deleteQuestionSolutionReport(id));
    }

    /**
     * 确定违规，删除对应题解
     *
     * @param solutionDeleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete_solution/after_confim")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteQuestionSolutionAfterConfirm(@RequestBody SolutionDeleteRequest solutionDeleteRequest, HttpServletRequest request) {
        return ResultUtils.success(questionSolutionReportService.deleteQuestionSolutionAfterConfirm(solutionDeleteRequest, request));
    }

    /**
     * 获取题解举报
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<QuestionSolutionReport> getQuestionSolutionReportById(@RequestParam("id") Long id) {
        return ResultUtils.success(questionSolutionReportService.getQuestionSolutionReportById(id));
    }

    /**
     * 分页获取题解举报
     *
     * @param questionSolutionReportQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<QuestionSolutionReport>> listQuestionSolutionReportByPage(@RequestBody QuestionSolutionReportQueryRequest questionSolutionReportQueryRequest) {
        return ResultUtils.success(questionSolutionReportService.listQuestionSolutionReportByPage(questionSolutionReportQueryRequest));
    }
}
