package com.luoying.luoojbackendquestionservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luoying.luoojbackendcommon.common.DeleteRequest;
import com.luoying.luoojbackendcommon.constant.CommonConstant;
import com.luoying.luoojbackendcommon.utils.SqlUtils;
import com.luoying.luoojbackendmodel.dto.comment_report.CommentDeleteRequest;
import com.luoying.luoojbackendmodel.dto.comment_report.CommentReportAddRequest;
import com.luoying.luoojbackendmodel.dto.comment_report.CommentReportQueryRequest;
import com.luoying.luoojbackendmodel.entity.CommentReport;
import com.luoying.luoojbackendmodel.entity.User;
import com.luoying.luoojbackendquestionservice.mapper.CommentReportMapper;
import com.luoying.luoojbackendquestionservice.service.CommentReportService;
import com.luoying.luoojbackendquestionservice.service.QuestionCommentService;
import com.luoying.luoojbackendquestionservice.service.QuestionSolutionCommentService;
import com.luoying.luoojbackendserviceclient.service.UserFeignClient;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static com.luoying.luoojbackendmodel.enums.CommentTypeEnum.QUESTION_COMMENT;
import static com.luoying.luoojbackendmodel.enums.CommentTypeEnum.QUESTION_SOLUTION_COMMENT;

/**
 * @author 落樱的悔恨
 * @description 针对表【comment_report(评论举报表)】的数据库操作Service实现
 * @createDate 2024-09-09 20:40:07
 */
@Service
public class CommentReportServiceImpl extends ServiceImpl<CommentReportMapper, CommentReport> implements CommentReportService {
    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private QuestionSolutionCommentService questionSolutionCommentService;

    @Resource
    private QuestionCommentService questionCommentService;

    /**
     * 新增评论举报
     *
     * @param commentReportAddRequest
     * @param request
     * @return
     */
    @Override
    public Boolean addCommentReport(CommentReportAddRequest commentReportAddRequest, HttpServletRequest request) {
        // 获取登录用户
        User loginUser = userFeignClient.getLoginUser(request);
        // 拷贝
        CommentReport commentReport = BeanUtil.copyProperties(commentReportAddRequest, CommentReport.class);
        commentReport.setUserId(loginUser.getId());
        // 写入
        return this.save(commentReport);
    }

    /**
     * 删除评论举报
     *
     * @param id
     * @return
     */
    @Override
    public Boolean deleteCommentReport(Long id) {
        // 删除
        return this.removeById(id);
    }

    /**
     * 确定违规，删除对应评论
     *
     * @param commentDeleteRequest
     * @param request
     * @return
     */
    @Override
    public Boolean deleteCommentAfterConfirm(CommentDeleteRequest commentDeleteRequest, HttpServletRequest request) {
        Long commentId = commentDeleteRequest.getCommentId();
        boolean isSuccess = false;
        if (QUESTION_SOLUTION_COMMENT.getValue().equals(commentDeleteRequest.getCommentType())) {// 删除题解评论
            DeleteRequest deleteRequest = new DeleteRequest();
            deleteRequest.setId(commentId);
            isSuccess = questionSolutionCommentService.deleteQuestionSolutionComment(deleteRequest, request);
        } else if (QUESTION_COMMENT.getValue().equals(commentDeleteRequest.getCommentType())) {// 删除题目评论
            // todo questionCommentService中需具体实现
            isSuccess = questionCommentService.removeById(commentId);
        }
        // 删除本条评论举报记录
        return this.removeById(commentDeleteRequest.getId()) && isSuccess;
    }

    /**
     * 获取评论举报
     *
     * @param id
     * @return
     */
    @Override
    public CommentReport getCommentReportById(Long id) {
        return this.getById(id);
    }

    /**
     * @param commentReportQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<CommentReport> getQueryWrapper(CommentReportQueryRequest commentReportQueryRequest) {
        QueryWrapper<CommentReport> queryWrapper = new QueryWrapper<>();
        // 判空
        if (commentReportQueryRequest == null) {
            return queryWrapper;
        }
        // 获取参数
        Long id = commentReportQueryRequest.getId();
        Long userId = commentReportQueryRequest.getUserId();
        Integer commentType = commentReportQueryRequest.getCommentType();
        Long commentId = commentReportQueryRequest.getCommentId();
        Long reportedUserId = commentReportQueryRequest.getReportedUserId();
        String sortField = commentReportQueryRequest.getSortField();
        String sortOrder = commentReportQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(commentType), "commentType", commentType);
        queryWrapper.eq(ObjectUtils.isNotEmpty(commentId), "commentId", commentId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(reportedUserId), "reportedUserId", reportedUserId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        // 返回
        return queryWrapper;
    }

    @Override
    public Page<CommentReport> listCommentReportByPage(CommentReportQueryRequest commentReportQueryRequest) {
        // 获取分页参数
        long current = commentReportQueryRequest.getCurrent();
        long size = commentReportQueryRequest.getPageSize();
        // 查询
        return this.page(new Page<>(current, size), this.getQueryWrapper(commentReportQueryRequest));
    }
}




