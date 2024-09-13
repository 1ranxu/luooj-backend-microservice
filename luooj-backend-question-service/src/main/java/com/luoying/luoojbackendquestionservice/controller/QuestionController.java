package com.luoying.luoojbackendquestionservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luoying.luoojbackendcommon.annotation.AuthCheck;
import com.luoying.luoojbackendcommon.common.BaseResponse;
import com.luoying.luoojbackendcommon.common.DeleteRequest;
import com.luoying.luoojbackendcommon.common.ErrorCode;
import com.luoying.luoojbackendcommon.common.ResultUtils;
import com.luoying.luoojbackendcommon.constant.UserConstant;
import com.luoying.luoojbackendcommon.exception.ThrowUtils;
import com.luoying.luoojbackendmodel.dto.question.QuestionAddRequest;
import com.luoying.luoojbackendmodel.dto.question.QuestionEditRequest;
import com.luoying.luoojbackendmodel.dto.question.QuestionQueryRequest;
import com.luoying.luoojbackendmodel.dto.question.QuestionUpdateRequest;
import com.luoying.luoojbackendmodel.entity.Question;
import com.luoying.luoojbackendmodel.enums.QuestionSubmitLanguageEnum;
import com.luoying.luoojbackendmodel.vo.QuestionVO;
import com.luoying.luoojbackendquestionservice.service.QuestionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 落樱的悔恨
 * 题目接口
 */
@RestController
@RequestMapping("/")
@Slf4j
public class QuestionController {
    @Resource
    private QuestionService questionService;

    /**
     * 创建题目（仅管理员）
     *
     * @param questionAddRequest 创建题目请求
     * @param request            {@link HttpServletRequest}
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest, HttpServletRequest request) {
        return ResultUtils.success(questionService.addQuestion(questionAddRequest, request));
    }

    /**
     * 删除题目（仅管理员）
     *
     * @param deleteRequest 删除请求
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest) {
        return ResultUtils.success(questionService.deleteQuestion(deleteRequest));
    }

    /**
     * 更新题目（仅管理员）
     *
     * @param questionUpdateRequest 题目更新请求
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest) {
        return ResultUtils.success(questionService.updateQuestion(questionUpdateRequest));
    }

    /**
     * 根据 id 获取题目（仅管理员）
     *
     * @param id 题目id
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Question> getQuestionById(long id) {
        return ResultUtils.success(questionService.getQuestionById(id));
    }

    /**
     * 根据 id 获取封装后的题目
     *
     * @param id 题目id
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionVO> getQuestionVOById(@RequestParam("id") long id, HttpServletRequest request) {
        return ResultUtils.success(questionService.getQuestionVOById(id, request));
    }

    /**
     * 分页获取题目列表（仅管理员）
     *
     * @param questionQueryRequest 题目查询请求
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest) {
        return ResultUtils.success(questionService.listQuestionByPage(questionQueryRequest));
    }

    /**
     * 分页获取题目列表（封装类）
     *
     * @param questionQueryRequest 题目查询请求
     * @param request              {@link HttpServletRequest}
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest, HttpServletRequest request) {
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 50, ErrorCode.PARAMS_ERROR);
        // 查询
        return ResultUtils.success(questionService.getQuestionVOPage(questionQueryRequest, request));
    }

    /**
     * 分页获取当前用户创建的题目列表
     *
     * @param questionQueryRequest 题目查询请求
     * @param request              {@link HttpServletRequest}
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listMyQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest, HttpServletRequest request) {
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 50, ErrorCode.PARAMS_ERROR);
        // 查询
        return ResultUtils.success(questionService.getQuestionVOPage(questionQueryRequest, request));
    }

    /**
     * 更新题目（仅管理员）
     *
     * @param questionEditRequest 题目更新请求
     * @param request             {@link HttpServletRequest}
     */
    @PostMapping("/edit")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> editQuestion(@RequestBody QuestionEditRequest questionEditRequest, HttpServletRequest request) {
        return ResultUtils.success(questionService.editQuestion(questionEditRequest,request));
    }

    /**
     * 获取支持的编程语言
     */
    @GetMapping("/get/language")
    public BaseResponse<List<String>> getCodeLanguage() {
        return ResultUtils.success(QuestionSubmitLanguageEnum.getValues());
    }

    /**
     * 获取上一道题目
     *
     * @param questionId 当前题目id
     */
    @GetMapping("/get/questionId/previous")
    public BaseResponse<Long> getPrevQuestion(@RequestParam("questionId") long questionId) {
        return ResultUtils.success(questionService.getPrevQuestion(questionId));
    }

    /**
     * 获取下一道题目
     *
     * @param questionId 当前题目id
     */
    @GetMapping("/get/questionId/next")
    public BaseResponse<Long> getNextQuestion(@RequestParam("questionId") long questionId) {
        return ResultUtils.success(questionService.getNextQuestion(questionId));
    }

    /**
     * 随机获取一道题目
     */
    @GetMapping("/get/questionId/random")
    public BaseResponse<Long> getRandomQuestion() {
        return ResultUtils.success(questionService.getRandomQuestion());
    }
}
