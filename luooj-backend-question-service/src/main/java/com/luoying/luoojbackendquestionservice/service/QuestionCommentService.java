package com.luoying.luoojbackendquestionservice.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luoying.luoojbackendcommon.common.DeleteRequest;
import com.luoying.luoojbackendmodel.dto.question_comment.QuestionCommentAddRequest;
import com.luoying.luoojbackendmodel.dto.question_comment.QuestionCommentQueryRequest;
import com.luoying.luoojbackendmodel.entity.QuestionComment;
import com.luoying.luoojbackendmodel.vo.QuestionCommentVO;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 落樱的悔恨
 * @description 针对表【question_comment(题目评论表)】的数据库操作Service
 * @createDate 2024-09-02 14:26:15
 */
public interface QuestionCommentService extends IService<QuestionComment> {

    /**
     * 发表评论
     * 回复评论
     *
     * @param questionCommentAddRequest
     * @param request
     * @return
     */
    Boolean publishQuestionComment(QuestionCommentAddRequest questionCommentAddRequest, HttpServletRequest request);

    /**
     * 删除评论
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    Boolean deleteQuestionComment(DeleteRequest deleteRequest, HttpServletRequest request);

    /**
     * 点赞某评论
     *
     * @param id
     * @param request
     * @return
     */
    Boolean likeQuestionComment(Long id, HttpServletRequest request);

    /**
     * 判断题解是否被点赞过
     *
     * @param commentId
     * @param userId
     * @return
     */
    Boolean isLiked(Long commentId, Long userId);

    /**
     * 根据id查询某条评论（仅管理员）
     *
     * @param id
     * @return
     */
    QuestionComment getQuestionCommentByid(Long id);

    /**
     * 获取条件构造器
     *
     * @param questionCommentQueryRequest
     * @return
     */
    Wrapper<QuestionComment> getQueryWrapper(QuestionCommentQueryRequest questionCommentQueryRequest);

    /**
     * 分页查询评论列表（仅管理员）
     *
     * @param questionCommentQueryRequest
     * @return
     */
    Page<QuestionComment> listQuestionCommentByPage(QuestionCommentQueryRequest questionCommentQueryRequest);

    /**
     * 根据题目id查询评论列表
     *
     * @param questionCommentQueryRequest
     * @param request
     * @return
     */
    QuestionCommentVO listQuestionComment(QuestionCommentQueryRequest questionCommentQueryRequest, HttpServletRequest request);
}
