package com.luoying.luoojbackendquestionservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luoying.luoojbackendcommon.annotation.AuthCheck;
import com.luoying.luoojbackendcommon.common.BaseResponse;
import com.luoying.luoojbackendcommon.common.ResultUtils;
import com.luoying.luoojbackendcommon.constant.UserConstant;
import com.luoying.luoojbackendmodel.dto.question_collect.QuestionCollectAddRequest;
import com.luoying.luoojbackendmodel.dto.question_collect.QuestionCollectDeleteRequest;
import com.luoying.luoojbackendmodel.dto.question_collect.QuestionCollectQueryRequest;
import com.luoying.luoojbackendmodel.entity.QuestionCollect;
import com.luoying.luoojbackendquestionservice.service.QuestionCollectService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/15 16:02
 */
@RestController
@RequestMapping("/question_collect")
public class QuestionCollectController {
    @Resource
    private QuestionCollectService questionCollectService;

    /**
     * 题单收藏题目
     *
     * @param questionCollectAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addQuestionCollect(@RequestBody QuestionCollectAddRequest questionCollectAddRequest, HttpServletRequest request) {
        return ResultUtils.success(questionCollectService.addQuestionCollect(questionCollectAddRequest, request));
    }

    /**
     * 题单取消收藏题目
     *
     * @param questionCollectDeleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestionCollect(@RequestBody QuestionCollectDeleteRequest questionCollectDeleteRequest, HttpServletRequest request) {
        return ResultUtils.success(questionCollectService.deleteQuestionCollect(questionCollectDeleteRequest, request));
    }

    /**
     * 获取题目收藏记录
     *
     * @param id
     * @return
     */
    @PostMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<QuestionCollect> getQuestionCollectById(@RequestParam("id") Long id) {
        return ResultUtils.success(questionCollectService.getQuestionCollectById(id));
    }

    /**
     * 分页获取题目收藏（仅管理员）
     *
     * @param questionCollectQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<QuestionCollect>> listQuestionCollectByPage(@RequestBody QuestionCollectQueryRequest questionCollectQueryRequest) {
        return ResultUtils.success(questionCollectService.listQuestionCollectByPage(questionCollectQueryRequest));
    }

    /**
     * 分页获取题目收藏
     *
     * @param questionCollectQueryRequest
     * @return
     */
    @PostMapping("/list/page/user")
    public BaseResponse<Page<QuestionCollect>> listQuestionCollectByPageUser(@RequestBody QuestionCollectQueryRequest questionCollectQueryRequest) {
        return ResultUtils.success(questionCollectService.listQuestionCollectByPageUser(questionCollectQueryRequest));
    }
}
