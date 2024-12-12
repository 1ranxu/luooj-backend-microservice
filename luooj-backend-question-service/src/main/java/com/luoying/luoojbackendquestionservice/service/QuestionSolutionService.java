package com.luoying.luoojbackendquestionservice.service;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luoying.luoojbackendcommon.common.DeleteRequest;
import com.luoying.luoojbackendmodel.dto.question_solution.QuestionSolutionAddRequest;
import com.luoying.luoojbackendmodel.dto.question_solution.QuestionSolutionQueryRequest;
import com.luoying.luoojbackendmodel.dto.question_solution.QuestionSolutionUpdateRequest;
import com.luoying.luoojbackendmodel.entity.QuestionSolution;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 落樱的悔恨
 * @description 针对表【question_solution(题解表)】的数据库操作Service
 * @createDate 2024-09-02 14:39:32
 */
public interface QuestionSolutionService extends IService<QuestionSolution> {

    /**
     * 创建题解
     *
     * @param questionSolutionAddRequest
     * @param request
     * @return
     */
    Boolean addQuestionSolution(QuestionSolutionAddRequest questionSolutionAddRequest, HttpServletRequest request);

    /**
     * 删除题解
     *
     * @param deleteRequest,
     * @param request
     * @return
     */
    Boolean deleteQuestionSolution(DeleteRequest deleteRequest, HttpServletRequest request);

    /**
     * 点赞题解
     *
     * @param id
     * @param request
     * @return
     */
    Boolean likeQuestionSolution(Long id, HttpServletRequest request);

    /**
     * @param solutionId
     * @param userId
     * @return
     */
    Boolean isLiked(Long solutionId, Long userId);

    /**
     * 更新题解
     *
     * @param questionSolutionUpdateRequest
     * @param request
     * @return
     */
    Boolean updateQuestionSolution(QuestionSolutionUpdateRequest questionSolutionUpdateRequest, HttpServletRequest request);

    /**
     * 查询题解
     *
     * @param id
     * @return
     */
    QuestionSolution getQuestionSolutionById(Long id, HttpServletRequest request);

    /**
     * 获取条件构造器
     *
     * @param questionSolutionQueryRequest
     * @return
     */
    Wrapper<QuestionSolution> getQueryWrapper(QuestionSolutionQueryRequest questionSolutionQueryRequest);

    /**
     * 分页获取题解列表（仅管理员）
     *
     * @param questionSolutionQueryRequest
     * @return
     */
    Page<QuestionSolution> listQuestionSolutionByPage(QuestionSolutionQueryRequest questionSolutionQueryRequest);

    /**
     * 分页获取题解列表（普通用户）
     *
     * @param questionSolutionQueryRequest
     * @return
     */
    Page<QuestionSolution> listQuestionSolutionByPageUser(QuestionSolutionQueryRequest questionSolutionQueryRequest, HttpServletRequest request);

}
