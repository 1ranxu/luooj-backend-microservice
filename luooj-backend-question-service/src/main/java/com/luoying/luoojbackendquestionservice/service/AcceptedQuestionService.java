package com.luoying.luoojbackendquestionservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luoying.luoojbackendcommon.common.DeleteRequest;
import com.luoying.luoojbackendmodel.dto.accepted_question.AcceptedQuestionQueryRequest;
import com.luoying.luoojbackendmodel.entity.AcceptedQuestion;
import com.luoying.luoojbackendmodel.vo.AcceptedQuestionDetailVO;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 落樱的悔恨
 * @description 针对表【accepted_question(题目通过表)】的数据库操作Service
 * @createDate 2024-09-02 14:54:22
 */
public interface AcceptedQuestionService extends IService<AcceptedQuestion> {

    /**
     * 删除通过记录（仅管理员）
     * @param deleteRequest
     * @param request
     * @return
     */
    Boolean deleteAcceptedQuestion(DeleteRequest deleteRequest, HttpServletRequest request);

    /**
     * 获取查询条件
     *
     * @param acceptedQuestionQueryRequest 通过记录请求
     */
    QueryWrapper<AcceptedQuestion> getQueryWrapper(AcceptedQuestionQueryRequest acceptedQuestionQueryRequest);

    /**
     * 分页获取通过记录（仅管理员）
     * @param acceptedQuestionQueryRequest
     * @return
     */
    Page<AcceptedQuestion> listAcceptedQuestionByPage(AcceptedQuestionQueryRequest acceptedQuestionQueryRequest);

    /**
     * 获取用户通过题目的详情
     *
     * @param request
     * @return
     */
    AcceptedQuestionDetailVO getAcceptedQuestionDetail(Long userId,HttpServletRequest request);

    /**
     * 获取用户的排名（通过题目数量）
     * @param userId
     * @param request
     * @return
     */
    Long getAcceptedQuestionRanking(Long userId,HttpServletRequest request);

    /**
     * 判断当前用户是否通过了某道题目
     *
     * @param questionId
     * @param request
     * @return
     */
    Boolean isAccepted(Long questionId, HttpServletRequest request);
}
