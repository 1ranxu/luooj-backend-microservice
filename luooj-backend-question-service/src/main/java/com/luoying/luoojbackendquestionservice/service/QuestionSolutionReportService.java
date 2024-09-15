package com.luoying.luoojbackendquestionservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luoying.luoojbackendmodel.dto.question_solution_report.QuestionSolutionReportAddRequest;
import com.luoying.luoojbackendmodel.dto.question_solution_report.QuestionSolutionReportQueryRequest;
import com.luoying.luoojbackendmodel.dto.question_solution_report.SolutionDeleteRequest;
import com.luoying.luoojbackendmodel.entity.QuestionSolutionReport;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 落樱的悔恨
 * @description 针对表【question_solution_report(题解举报表)】的数据库操作Service
 * @createDate 2024-09-09 20:44:51
 */
public interface QuestionSolutionReportService extends IService<QuestionSolutionReport> {

    /**
     * 新增题解举报
     *
     * @param questionSolutionReportAddRequest
     * @param request
     * @return
     */
    Boolean addQuestionSolutionReport(QuestionSolutionReportAddRequest questionSolutionReportAddRequest, HttpServletRequest request);

    /**
     * 删除题解举报
     *
     * @param id
     * @return
     */
    Boolean deleteQuestionSolutionReport(Long id);

    /**
     * 确定违规，删除对应题解
     *
     * @param solutionDeleteRequest
     * @param request
     * @return
     */
    Boolean deleteQuestionSolutionAfterConfirm(SolutionDeleteRequest solutionDeleteRequest, HttpServletRequest request);

    /**
     * 获取题解举报
     *
     * @param id
     * @return
     */
    QuestionSolutionReport getQuestionSolutionReportById(Long id);

    /**
     * 获取查询条件
     *
     * @param questionSolutionReportQueryRequest
     * @return
     */
    QueryWrapper<QuestionSolutionReport> getQueryWrapper(QuestionSolutionReportQueryRequest questionSolutionReportQueryRequest);

    /**
     * 分页获取题解举报
     *
     * @param questionSolutionReportQueryRequest
     * @return
     */
    Page<QuestionSolutionReport> listQuestionSolutionReportByPage(QuestionSolutionReportQueryRequest questionSolutionReportQueryRequest);
}
