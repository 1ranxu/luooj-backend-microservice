package com.luoying.luoojbackendquestionservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luoying.luoojbackendcommon.annotation.AuthCheck;
import com.luoying.luoojbackendcommon.common.BaseResponse;
import com.luoying.luoojbackendcommon.common.ResultUtils;
import com.luoying.luoojbackendcommon.constant.UserConstant;
import com.luoying.luoojbackendmodel.dto.question_list_collect.QuestionListCollectAddRequest;
import com.luoying.luoojbackendmodel.dto.question_list_collect.QuestionListCollectDeleteRequest;
import com.luoying.luoojbackendmodel.dto.question_list_collect.QuestionListCollectQueryRequest;
import com.luoying.luoojbackendmodel.entity.QuestionListCollect;
import com.luoying.luoojbackendquestionservice.service.QuestionListCollectService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/15 17:07
 */
@RestController
@RequestMapping("question_list_collect")
public class QuestionListCollectController {
    @Resource
    private QuestionListCollectService questionListCollectService;

    /**
     * 收藏题单
     *
     * @param questionListCollectAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addQuestionListCollect(@RequestBody QuestionListCollectAddRequest questionListCollectAddRequest, HttpServletRequest request) {
        return ResultUtils.success(questionListCollectService.addQuestionListCollect(questionListCollectAddRequest, request));
    }

    /**
     * 取消收藏题单
     *
     * @param questionListCollectDeleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestionListCollect(@RequestBody QuestionListCollectDeleteRequest questionListCollectDeleteRequest, HttpServletRequest request) {
        return ResultUtils.success(questionListCollectService.deleteQuestionListCollect(questionListCollectDeleteRequest, request));
    }

    /**
     * 获取题单收藏
     *
     * @param id
     * @return
     */
    @PostMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<QuestionListCollect> getQuestionListCollectById(@RequestParam("id") Long id) {
        return ResultUtils.success(questionListCollectService.getQuestionListCollectById(id));
    }

    /**
     * 分页获取题单收藏（仅管理员）
     *
     * @param questionListCollectQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<QuestionListCollect>> listQuestionListCollectByPage(@RequestBody QuestionListCollectQueryRequest questionListCollectQueryRequest) {
        return ResultUtils.success(questionListCollectService.listQuestionListCollectByPage(questionListCollectQueryRequest));
    }

    /**
     * 分页获取题单收藏
     *
     * @param questionListCollectQueryRequest
     * @return
     */
    @PostMapping("/list/page/user")
    public BaseResponse<Page<QuestionListCollect>> listQuestionListCollectByPageUser(@RequestBody QuestionListCollectQueryRequest questionListCollectQueryRequest) {
        return ResultUtils.success(questionListCollectService.listQuestionListCollectByPageUser(questionListCollectQueryRequest));
    }

}
