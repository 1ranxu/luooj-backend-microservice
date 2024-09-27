package com.luoying.luoojbackendquestionservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luoying.luoojbackendcommon.annotation.AuthCheck;
import com.luoying.luoojbackendcommon.common.BaseResponse;
import com.luoying.luoojbackendcommon.common.ResultUtils;
import com.luoying.luoojbackendcommon.constant.UserConstant;
import com.luoying.luoojbackendmodel.codesanbox.RunCodeRequest;
import com.luoying.luoojbackendmodel.codesanbox.RunCodeResponse;
import com.luoying.luoojbackendmodel.dto.contest_result.ContestQuestionSubmit;
import com.luoying.luoojbackendmodel.dto.contest_result.ContestResultQueryRequest;
import com.luoying.luoojbackendmodel.dto.question_submit.QuestionSubmitJudgeInfo;
import com.luoying.luoojbackendmodel.entity.ContestResult;
import com.luoying.luoojbackendmodel.vo.ContestRank;
import com.luoying.luoojbackendquestionservice.service.ContestResultService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/17 13:27
 */
@RestController
@RequestMapping("/contest_result")
public class ContestResultController {
    @Resource
    private ContestResultService contestResultService;

    /**
     * 竞赛题目提交
     *
     * @param contestQuestionSubmit
     * @param request
     * @return
     */
    @PostMapping("/submit")
    public BaseResponse<QuestionSubmitJudgeInfo> contestQuestionSubmit(@RequestBody ContestQuestionSubmit contestQuestionSubmit, HttpServletRequest request) {
        return ResultUtils.success(contestResultService.contestQuestionSubmit(contestQuestionSubmit, request));
    }

    /**
     * 竞赛题目运行
     *
     * @param runCodeRequest
     * @return
     */
    @PostMapping("/run")
    public BaseResponse<RunCodeResponse> contestQuestionRun(@RequestBody RunCodeRequest runCodeRequest) {
        return ResultUtils.success(contestResultService.contestQuestionRun(runCodeRequest));
    }

    /**
     * 竞赛排名统计
     *
     * @param contestResultQueryRequest
     * @return
     */
    @PostMapping("/statistics")
    public BaseResponse<Page<ContestRank>> contestRankStatistics(@RequestBody ContestResultQueryRequest contestResultQueryRequest) {
        return ResultUtils.success(contestResultService.contestRankStatistics(contestResultQueryRequest));
    }

    /**
     * 删除
     *
     * @param id
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteContestResult(@RequestParam("id") Long id) {
        return ResultUtils.success(contestResultService.deleteContestResult(id));
    }

    /**
     * 获取竞赛结果
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<ContestResult> getContestResultById(@RequestParam("id") Long id) {
        return ResultUtils.success(contestResultService.getContestResultById(id));
    }

    /**
     * 分页获取竞赛结果
     *
     * @param contestResultQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ContestResult>> listContestResultByPage(@RequestBody ContestResultQueryRequest contestResultQueryRequest) {
        return ResultUtils.success(contestResultService.listContestResultByPage(contestResultQueryRequest));
    }


}
