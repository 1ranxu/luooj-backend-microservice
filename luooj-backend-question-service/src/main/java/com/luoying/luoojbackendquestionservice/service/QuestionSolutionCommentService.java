package com.luoying.luoojbackendquestionservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luoying.luoojbackendcommon.common.DeleteRequest;
import com.luoying.luoojbackendmodel.dto.question_solution_comment.QuestionSolutionCommentAddRequest;
import com.luoying.luoojbackendmodel.dto.question_solution_comment.QuestionSolutionCommentQueryRequest;
import com.luoying.luoojbackendmodel.entity.QuestionSolutionComment;
import com.luoying.luoojbackendmodel.vo.QuestionSolutionCommentVO;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 落樱的悔恨
 * @description 针对表【question_solution_comment(题解评论表)】的数据库操作Service
 * @createDate 2024-09-02 14:41:44
 */
public interface QuestionSolutionCommentService extends IService<QuestionSolutionComment> {

    /**
     * 发表评论
     * 回复评论
     *
     * @param questionSolutionCommentAddRequest
     * @param request
     * @return
     */
    Boolean publishQuestionSolutionComment(QuestionSolutionCommentAddRequest questionSolutionCommentAddRequest, HttpServletRequest request);

    /**
     * 删除评论
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    Boolean deleteQuestionSolutionComment(DeleteRequest deleteRequest, HttpServletRequest request);

    /**
     * 点赞某评论
     *
     * @param id
     * @param request
     * @return
     */
    Boolean likeQuestionSolutionComment(Long id, HttpServletRequest request);

    /**
     * 判断评论是否被点赞过
     * @param commentId
     * @param userId
     * @return
     */
    Boolean isLiked(Long commentId,Long userId);

    /**
     * 根据id查询某条评论（仅管理员）
     *
     * @param id
     * @return
     */
    QuestionSolutionComment getQuestionSolutionCommentByid(Long id);

    /**
     * 获取查询条件
     *
     * @param questionSolutionCommentQueryRequest
     * @return
     */
    QueryWrapper<QuestionSolutionComment> getQueryWrapper(QuestionSolutionCommentQueryRequest questionSolutionCommentQueryRequest);

    /**
     * @param questionSolutionCommentQueryRequest
     * @return
     */
    Page<QuestionSolutionComment> listQuestionSolutionCommentByPage(QuestionSolutionCommentQueryRequest questionSolutionCommentQueryRequest);

    /**
     * 根据题解id查询评论列表
     * @param questionSolutionCommentQueryRequest
     * @param request
     * @return
     */
    QuestionSolutionCommentVO listQuestionSolutionComment(QuestionSolutionCommentQueryRequest questionSolutionCommentQueryRequest, HttpServletRequest request);


}
