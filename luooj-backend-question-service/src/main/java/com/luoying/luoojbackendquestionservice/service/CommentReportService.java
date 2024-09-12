package com.luoying.luoojbackendquestionservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luoying.luoojbackendmodel.dto.comment_report.CommentDeleteRequest;
import com.luoying.luoojbackendmodel.dto.comment_report.CommentReportAddRequest;
import com.luoying.luoojbackendmodel.dto.comment_report.CommentReportQueryRequest;
import com.luoying.luoojbackendmodel.entity.CommentReport;

import javax.servlet.http.HttpServletRequest;

/**
* @author 落樱的悔恨
* @description 针对表【comment_report(评论举报表)】的数据库操作Service
* @createDate 2024-09-09 20:40:07
*/
public interface CommentReportService extends IService<CommentReport> {

    /**
     * 新增评论举报
     * @param commentReportAddRequest
     * @param request
     * @return
     */
    Boolean addCommentReport(CommentReportAddRequest commentReportAddRequest, HttpServletRequest request);

    /**
     * 删除评论举报
     * @param id
     * @return
     */
    Boolean deleteCommentReport(Long id);

    /**
     * 确定违规，删除对应评论
     * @param commentDeleteRequest
     * @param request
     * @return
     */
    Boolean deleteCommentAfterConfirm(CommentDeleteRequest commentDeleteRequest, HttpServletRequest request);

    /**
     * 获取评论举报
     * @param id
     * @return
     */
    CommentReport getCommentReportById(Long id);

    /**
     * 获取查询条件
     *
     * @param commentReportQueryRequest
     * @return
     */
    QueryWrapper<CommentReport> getQueryWrapper(CommentReportQueryRequest commentReportQueryRequest);

    /**
     * 分页获取评论举报
     * @param commentReportQueryRequest
     * @return
     */
    Page<CommentReport> listCommentReportByPage(CommentReportQueryRequest commentReportQueryRequest);
}
