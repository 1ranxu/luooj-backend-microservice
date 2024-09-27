package com.luoying.luoojbackendquestionservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luoying.luoojbackendmodel.codesanbox.RunCodeRequest;
import com.luoying.luoojbackendmodel.codesanbox.RunCodeResponse;
import com.luoying.luoojbackendmodel.dto.contest_result.ContestQuestionSubmit;
import com.luoying.luoojbackendmodel.dto.contest_result.ContestResultQueryRequest;
import com.luoying.luoojbackendmodel.dto.question_submit.QuestionSubmitJudgeInfo;
import com.luoying.luoojbackendmodel.entity.ContestResult;
import com.luoying.luoojbackendmodel.vo.ContestRank;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 落樱的悔恨
 * @description 针对表【contest_result(竞赛成绩表)】的数据库操作Service
 * @createDate 2024-09-02 14:50:13
 */
public interface ContestResultService extends IService<ContestResult> {

    /**
     * 竞赛题目提交
     *
     * @param contestQuestionSubmit
     * @param request
     * @return
     */
    QuestionSubmitJudgeInfo contestQuestionSubmit(ContestQuestionSubmit contestQuestionSubmit, HttpServletRequest request);

    /**
     * 竞赛题目运行
     *
     * @param runCodeRequest
     * @return
     */
    RunCodeResponse contestQuestionRun(RunCodeRequest runCodeRequest);

    /**
     * 获取查询条件
     *
     * @param contestResultQueryRequest
     * @return
     */
    QueryWrapper<ContestResult> getQueryWrapper(ContestResultQueryRequest contestResultQueryRequest);

    /**
     * 竞赛排名统计
     *
     * @param contestResultQueryRequest
     * @return
     */
    Page<ContestRank> contestRankStatistics(ContestResultQueryRequest contestResultQueryRequest);

    /**
     * 删除
     * @param id
     * @return
     */
    Boolean deleteContestResult(Long id);

    /**
     * 获取竞赛结果
     * @param id
     * @return
     */
    ContestResult getContestResultById(Long id);

    /**
     * 分页获取竞赛结果
     * @param contestResultQueryRequest
     * @return
     */
    Page<ContestResult> listContestResultByPage(ContestResultQueryRequest contestResultQueryRequest);
}
