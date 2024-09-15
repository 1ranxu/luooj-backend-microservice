package com.luoying.luoojbackendquestionservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luoying.luoojbackendmodel.dto.question_collect.QuestionCollectAddRequest;
import com.luoying.luoojbackendmodel.dto.question_collect.QuestionCollectDeleteRequest;
import com.luoying.luoojbackendmodel.dto.question_collect.QuestionCollectQueryRequest;
import com.luoying.luoojbackendmodel.entity.QuestionCollect;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 落樱的悔恨
 * @description 针对表【question_collect(题单收藏题目表)】的数据库操作Service
 * @createDate 2024-09-02 14:33:59
 */
public interface QuestionCollectService extends IService<QuestionCollect> {

    /**
     * 题单收藏题目
     *
     * @param questionCollectAddRequest
     * @param request
     * @return
     */
    Boolean addQuestionCollect(QuestionCollectAddRequest questionCollectAddRequest, HttpServletRequest request);

    /**
     * 题单取消收藏题目
     *
     * @param questionCollectDeleteRequest
     * @param request
     * @return
     */
    Boolean deleteQuestionCollect(QuestionCollectDeleteRequest questionCollectDeleteRequest, HttpServletRequest request);

    /**
     * 获取题目收藏记录
     *
     * @param id
     * @return
     */
    QuestionCollect getQuestionCollectById(Long id);

    /**
     * 获取查询条件
     *
     * @param questionCollectQueryRequest
     * @return
     */
    QueryWrapper<QuestionCollect> getQueryWrapper(QuestionCollectQueryRequest questionCollectQueryRequest);

    /**
     * 分页获取题目收藏（仅管理员）
     *
     * @param questionCollectQueryRequest
     * @return
     */
    Page<QuestionCollect> listQuestionCollectByPage(QuestionCollectQueryRequest questionCollectQueryRequest);

    /**
     * 分页获取题目收藏
     *
     * @param questionCollectQueryRequest
     * @return
     */
    Page<QuestionCollect> listQuestionCollectByPageUser(QuestionCollectQueryRequest questionCollectQueryRequest);
}
