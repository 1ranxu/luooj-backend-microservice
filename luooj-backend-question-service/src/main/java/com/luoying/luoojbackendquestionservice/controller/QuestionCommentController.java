package com.luoying.luoojbackendquestionservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luoying.luoojbackendcommon.annotation.AuthCheck;
import com.luoying.luoojbackendcommon.common.BaseResponse;
import com.luoying.luoojbackendcommon.common.DeleteRequest;
import com.luoying.luoojbackendcommon.common.ResultUtils;
import com.luoying.luoojbackendcommon.constant.UserConstant;
import com.luoying.luoojbackendmodel.dto.question_comment.QuestionCommentAddRequest;
import com.luoying.luoojbackendmodel.dto.question_comment.QuestionCommentQueryRequest;
import com.luoying.luoojbackendmodel.entity.QuestionComment;
import com.luoying.luoojbackendmodel.vo.QuestionCommentVO;
import com.luoying.luoojbackendquestionservice.service.QuestionCommentService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/13 17:06
 */
@RestController
@RequestMapping("/question_comment")
public class QuestionCommentController {
    @Resource
    private QuestionCommentService questionCommentService;

    /**
     * 发表评论
     * 回复评论
     *
     * @param questionCommentAddRequest
     * @param request
     * @return
     */
    @PostMapping("/publish")
    public BaseResponse<Boolean> publishQuestionComment(@RequestBody QuestionCommentAddRequest questionCommentAddRequest, HttpServletRequest request) {
        return ResultUtils.success(questionCommentService.publishQuestionComment(questionCommentAddRequest, request));
    }

    /**
     * 删除评论
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestionComment(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        return ResultUtils.success(questionCommentService.deleteQuestionComment(deleteRequest, request));
    }

    /**
     * 点赞某评论
     *
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/like")
    public BaseResponse<Boolean> likeQuestionComment(@RequestParam("id") Long id, HttpServletRequest request) {
        return ResultUtils.success(questionCommentService.likeQuestionComment(id, request));
    }

    /**
     * 根据id查询某条评论（仅管理员）
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<QuestionComment> getQuestionCommentByid(@RequestParam("id") Long id) {
        return ResultUtils.success(questionCommentService.getQuestionCommentByid(id));
    }

    /**
     * 分页查询评论列表（仅管理员）
     *
     * @param questionCommentQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<QuestionComment>> listQuestionCommentByPage(@RequestBody QuestionCommentQueryRequest questionCommentQueryRequest) {
        return ResultUtils.success(questionCommentService.listQuestionCommentByPage(questionCommentQueryRequest));
    }

    /**
     * 根据题目id查询评论列表
     *
     * @param questionCommentQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list")
    public BaseResponse<QuestionCommentVO> listQuestionComment(@RequestBody QuestionCommentQueryRequest questionCommentQueryRequest, HttpServletRequest request) {
        return ResultUtils.success(questionCommentService.listQuestionComment(questionCommentQueryRequest, request));
    }
}
