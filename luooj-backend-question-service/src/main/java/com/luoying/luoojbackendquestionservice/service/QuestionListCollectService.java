package com.luoying.luoojbackendquestionservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luoying.luoojbackendmodel.dto.question_list_collect.QuestionListCollectAddRequest;
import com.luoying.luoojbackendmodel.dto.question_list_collect.QuestionListCollectDeleteRequest;
import com.luoying.luoojbackendmodel.dto.question_list_collect.QuestionListCollectQueryRequest;
import com.luoying.luoojbackendmodel.entity.QuestionListCollect;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 落樱的悔恨
 * @description 针对表【question_list_collect(题单收藏表)】的数据库操作Service
 * @createDate 2024-09-02 14:36:48
 */
public interface QuestionListCollectService extends IService<QuestionListCollect> {

    /**
     * 收藏题单
     *
     * @param questionListCollectAddRequest
     * @param request
     * @return
     */
    Boolean addQuestionListCollect(QuestionListCollectAddRequest questionListCollectAddRequest, HttpServletRequest request);

    /**
     * 取消收藏题单
     *
     * @param questionListCollectDeleteRequest
     * @param request
     * @return
     */
    Boolean deleteQuestionListCollect(QuestionListCollectDeleteRequest questionListCollectDeleteRequest, HttpServletRequest request);

    /**
     * 获取题单收藏
     *
     * @param id
     * @return
     */
    QuestionListCollect getQuestionListCollectById(Long id);

    /**
     * 获取查询条件
     *
     * @param questionListCollectQueryRequest
     * @return
     */
    QueryWrapper<QuestionListCollect> getQueryWrapper(QuestionListCollectQueryRequest questionListCollectQueryRequest);

    /**
     * 分页获取题单收藏（仅管理员）
     *
     * @param questionListCollectQueryRequest
     * @return
     */
    Page<QuestionListCollect> listQuestionListCollectByPage(QuestionListCollectQueryRequest questionListCollectQueryRequest);

    /**
     * 分页获取题单收藏
     *
     * @param questionListCollectQueryRequest
     * @return
     */
    Page<QuestionListCollect> listQuestionListCollectByPageUser(QuestionListCollectQueryRequest questionListCollectQueryRequest);
}
