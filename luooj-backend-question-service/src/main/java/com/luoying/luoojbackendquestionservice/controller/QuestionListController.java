package com.luoying.luoojbackendquestionservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luoying.luoojbackendcommon.annotation.AuthCheck;
import com.luoying.luoojbackendcommon.common.BaseResponse;
import com.luoying.luoojbackendcommon.common.ResultUtils;
import com.luoying.luoojbackendcommon.constant.UserConstant;
import com.luoying.luoojbackendmodel.dto.question_list.QuestionListAddRequest;
import com.luoying.luoojbackendmodel.dto.question_list.QuestionListQueryRequest;
import com.luoying.luoojbackendmodel.dto.question_list.QuestionListUpdateRequest;
import com.luoying.luoojbackendmodel.entity.QuestionList;
import com.luoying.luoojbackendmodel.vo.QuestionListVO;
import com.luoying.luoojbackendquestionservice.service.QuestionListService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/15 16:03
 */
@RestController
@RequestMapping("/question_list")
public class QuestionListController {
    @Resource
    private QuestionListService questionListService;

    /**
     * 创建题单
     *
     * @param questionListAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addQuestionList(@RequestBody QuestionListAddRequest questionListAddRequest, HttpServletRequest request) {
        return ResultUtils.success(questionListService.addQuestionList(questionListAddRequest, request));
    }

    /**
     * 删除题单
     *
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestionList(@RequestParam("id") Long id, HttpServletRequest request) {
        return ResultUtils.success(questionListService.deleteQuestionList(id, request));
    }

    /**
     * 更新题单
     *
     * @param questionListUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateQuestionList(@RequestBody QuestionListUpdateRequest questionListUpdateRequest, HttpServletRequest request) {
        return ResultUtils.success(questionListService.updateQuestionList(questionListUpdateRequest, request));
    }

    /**
     * 获取题单
     *
     * @param id
     * @return
     */
    @PostMapping("/get")
    public BaseResponse<QuestionList> getQuestionListById(@RequestParam("id") Long id) {
        return ResultUtils.success(questionListService.getQuestionListById(id));
    }

    /**
     * 分页获取题单（仅管理员）
     *
     * @param questionListQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<QuestionList>> listQuestionListByPage(@RequestBody QuestionListQueryRequest questionListQueryRequest) {
        return ResultUtils.success(questionListService.listQuestionListByPage(questionListQueryRequest));
    }

    /**
     * 分页获取题单
     *
     * @param questionListQueryRequest
     * @return
     */
    @PostMapping("/list/page/user")
    public BaseResponse<Page<QuestionListVO>> listQuestionListByPageUser(@RequestBody QuestionListQueryRequest questionListQueryRequest,HttpServletRequest request) {
        return ResultUtils.success(questionListService.listQuestionListByPageUser(questionListQueryRequest,request));
    }
}
