package com.luoying.luoojbackendquestionservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luoying.luoojbackendcommon.annotation.AuthCheck;
import com.luoying.luoojbackendcommon.common.BaseResponse;
import com.luoying.luoojbackendcommon.common.ResultUtils;
import com.luoying.luoojbackendcommon.constant.UserConstant;
import com.luoying.luoojbackendmodel.dto.contest_apply.ContestApplyAddRequest;
import com.luoying.luoojbackendmodel.dto.contest_apply.ContestApplyQueryRequest;
import com.luoying.luoojbackendmodel.entity.ContestApply;
import com.luoying.luoojbackendquestionservice.service.ContestApplyService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/16 17:16
 */
@RestController
@RequestMapping("/contest_apply")
public class ContestApplyController {
    @Resource
    private ContestApplyService contestApplyService;

    /**
     * 报名/取消报名
     *
     * @param contestApplyAddRequest
     * @param request
     * @return
     */
    @PostMapping("/apply")
    public BaseResponse<Boolean> applyContest(@RequestBody ContestApplyAddRequest contestApplyAddRequest, HttpServletRequest request) {
        return ResultUtils.success(contestApplyService.applyContest(contestApplyAddRequest, request));
    }

    /**
     * 获取报名
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<ContestApply> getContestApplyById(@RequestParam("id") Long id) {
        return ResultUtils.success(contestApplyService.getContestApplyById(id));
    }

    /**
     * 分页获取报名
     *
     * @param contestApplyQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<ContestApply>> listContestApplyByPage(@RequestBody ContestApplyQueryRequest contestApplyQueryRequest) {
        return ResultUtils.success(contestApplyService.listContestApplyByPage(contestApplyQueryRequest));
    }
}
