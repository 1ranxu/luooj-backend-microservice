package com.luoying.luoojbackendquestionservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luoying.luoojbackendmodel.dto.question_solution_collect.QuestionSolutionCollectAddRequest;
import com.luoying.luoojbackendmodel.dto.question_solution_collect.QuestionSolutionCollectQueryRequest;
import com.luoying.luoojbackendmodel.entity.QuestionSolutionCollect;

import javax.servlet.http.HttpServletRequest;

/**
* @author 落樱的悔恨
* @description 针对表【question_solution_collect(题解收藏表)】的数据库操作Service
* @createDate 2024-09-02 14:43:49
*/
public interface QuestionSolutionCollectService extends IService<QuestionSolutionCollect> {
    /**
     * 收藏题解
     * @param questionSolutionCollectAddRequest
     * @param request
     * @return
     */
    Boolean addQuestionSolutionCollect(QuestionSolutionCollectAddRequest questionSolutionCollectAddRequest, HttpServletRequest request);

    /**
     * 取消收藏题解
     * @param id
     * @param request
     * @return
     */
    Boolean deleteQuestionSolutionCollect(Long id, HttpServletRequest request);

    /**
     * 获取查询条件
     *
     * @param questionSolutionCollectQueryRequest
     * @return
     */
    QueryWrapper<QuestionSolutionCollect> getQueryWrapper(QuestionSolutionCollectQueryRequest questionSolutionCollectQueryRequest);

    /**
     * 分页获取题解收藏（仅管理员）
     * @param questionSolutionCollectQueryRequest
     * @return
     */
    Page<QuestionSolutionCollect> listQuestionSolutionCollectByPage(QuestionSolutionCollectQueryRequest questionSolutionCollectQueryRequest);

    /**
     * 分页获取题解收藏
     * @param questionSolutionCollectQueryRequest
     * @return
     */
    Page<QuestionSolutionCollect> listQuestionSolutionCollectByPageUser(QuestionSolutionCollectQueryRequest questionSolutionCollectQueryRequest);
}
