package com.luoying.luoojbackendquestionservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luoying.luoojbackendcommon.annotation.AuthCheck;
import com.luoying.luoojbackendcommon.common.BaseResponse;
import com.luoying.luoojbackendcommon.common.ResultUtils;
import com.luoying.luoojbackendcommon.constant.UserConstant;
import com.luoying.luoojbackendmodel.dto.comment_report.CommentDeleteRequest;
import com.luoying.luoojbackendmodel.dto.comment_report.CommentReportAddRequest;
import com.luoying.luoojbackendmodel.dto.comment_report.CommentReportQueryRequest;
import com.luoying.luoojbackendmodel.entity.CommentReport;
import com.luoying.luoojbackendquestionservice.service.CommentReportService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/10 16:38
 */
@RestController
@RequestMapping("/comment_report")
public class CommentReportController {
    @Resource
    private CommentReportService commentReportService;

    /**
     * 新增评论举报
     *
     * @param commentReportAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    public BaseResponse<Boolean> addCommentReport(@RequestBody CommentReportAddRequest commentReportAddRequest, HttpServletRequest request) {
        return ResultUtils.success(commentReportService.addCommentReport(commentReportAddRequest, request));
    }

    /**
     * 删除评论举报
     *
     * @param id
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteCommentReport(@RequestParam("id") Long id) {
        return ResultUtils.success(commentReportService.deleteCommentReport(id));
    }

    /**
     * 确定违规，删除对应评论
     * @param commentDeleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete_comment/after_confim")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteCommentAfterConfirm(@RequestBody CommentDeleteRequest commentDeleteRequest, HttpServletRequest request) {
        return ResultUtils.success(commentReportService.deleteCommentAfterConfirm(commentDeleteRequest, request));
    }

    /**
     * 获取评论举报
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<CommentReport> getCommentReportById(@RequestParam("id") Long id) {
        return ResultUtils.success(commentReportService.getCommentReportById(id));
    }

    /**
     * 分页获取评论举报
     *
     * @param commentReportQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<CommentReport>> listCommentReportByPage(@RequestBody CommentReportQueryRequest commentReportQueryRequest) {
        return ResultUtils.success(commentReportService.listCommentReportByPage(commentReportQueryRequest));
    }

}
