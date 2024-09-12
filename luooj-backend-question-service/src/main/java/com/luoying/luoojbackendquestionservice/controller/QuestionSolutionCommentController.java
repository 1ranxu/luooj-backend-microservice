package com.luoying.luoojbackendquestionservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luoying.luoojbackendcommon.annotation.AuthCheck;
import com.luoying.luoojbackendcommon.common.BaseResponse;
import com.luoying.luoojbackendcommon.common.DeleteRequest;
import com.luoying.luoojbackendcommon.common.ResultUtils;
import com.luoying.luoojbackendcommon.constant.UserConstant;
import com.luoying.luoojbackendmodel.dto.question_solution_comment.QuestionSolutionCommentAddRequest;
import com.luoying.luoojbackendmodel.dto.question_solution_comment.QuestionSolutionCommentQueryRequest;
import com.luoying.luoojbackendmodel.entity.QuestionSolutionComment;
import com.luoying.luoojbackendmodel.vo.QuestionSolutionCommentVO;
import com.luoying.luoojbackendquestionservice.service.QuestionSolutionCommentService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/8 13:51
 */
@RestController
@RequestMapping("/question_solution_comment")
public class QuestionSolutionCommentController {
    @Resource
    private QuestionSolutionCommentService questionSolutionCommentService;

    /**
     * 发表评论
     * 回复评论
     *
     * @param questionSolutionCommentAddRequest
     * @param request
     * @return
     */
    @PostMapping("/publish")
    public BaseResponse<Boolean> publishQuestionSolutionComment(@RequestBody QuestionSolutionCommentAddRequest questionSolutionCommentAddRequest, HttpServletRequest request) {
        return ResultUtils.success(questionSolutionCommentService.publishQuestionSolutionComment(questionSolutionCommentAddRequest, request));
    }

    /**
     * 删除评论
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteQuestionSolutionComment(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        return ResultUtils.success(questionSolutionCommentService.deleteQuestionSolutionComment(deleteRequest, request));
    }

    /**
     * 点赞某评论
     *
     * @param id
     * @param request
     * @return
     */
    @PostMapping("/like")
    public BaseResponse<Boolean> likeQuestionSolutionComment(@RequestParam("id") Long id, HttpServletRequest request) {
        return ResultUtils.success(questionSolutionCommentService.likeQuestionSolutionComment(id, request));
    }

    /**
     * 根据id查询某条评论（仅管理员）
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<QuestionSolutionComment> getQuestionSolutionCommentByid(@RequestParam("id") Long id) {
        return ResultUtils.success(questionSolutionCommentService.getQuestionSolutionCommentByid(id));
    }

    /**
     * 分页查询评论列表（仅管理员）
     *
     * @param questionSolutionCommentQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<QuestionSolutionComment>> listQuestionSolutionCommentByPage(@RequestBody QuestionSolutionCommentQueryRequest questionSolutionCommentQueryRequest) {
        return ResultUtils.success(questionSolutionCommentService.listQuestionSolutionCommentByPage(questionSolutionCommentQueryRequest));
    }

    /**
     * 根据题解id查询评论列表
     * @param questionSolutionCommentQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/list")
    public BaseResponse<QuestionSolutionCommentVO> listQuestionSolutionComment(@RequestBody QuestionSolutionCommentQueryRequest questionSolutionCommentQueryRequest, HttpServletRequest request) {
        return ResultUtils.success(questionSolutionCommentService.listQuestionSolutionComment(questionSolutionCommentQueryRequest, request));
    }
}
