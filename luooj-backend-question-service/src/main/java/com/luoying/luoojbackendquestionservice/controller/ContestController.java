package com.luoying.luoojbackendquestionservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luoying.luoojbackendcommon.annotation.AuthCheck;
import com.luoying.luoojbackendcommon.common.BaseResponse;
import com.luoying.luoojbackendcommon.common.ResultUtils;
import com.luoying.luoojbackendcommon.constant.UserConstant;
import com.luoying.luoojbackendmodel.dto.contest.ContestAddRequest;
import com.luoying.luoojbackendmodel.dto.contest.ContestQueryRequest;
import com.luoying.luoojbackendmodel.dto.contest.ContestUpdateRequest;
import com.luoying.luoojbackendmodel.entity.Contest;
import com.luoying.luoojbackendquestionservice.service.ContestService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/16 15:05
 */
@RestController
@RequestMapping("/contest")
public class ContestController {
    @Resource
    private ContestService contestService;

    /**
     * 创建竞赛
     *
     * @param contestAddRequest
     * @param request
     * @return
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> addContest(@RequestBody ContestAddRequest contestAddRequest, HttpServletRequest request) {
        return ResultUtils.success(contestService.addContest(contestAddRequest, request));
    }

    /**
     * 删除竞赛
     *
     * @param id
     * @return
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteContest(@RequestParam("id") Long id) {
        return ResultUtils.success(contestService.deleteContest(id));
    }

    /**
     * 更新竞赛
     *
     * @param contestUpdateRequest
     * @return
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateContest(@RequestBody ContestUpdateRequest contestUpdateRequest) {
        return ResultUtils.success(contestService.updateContest(contestUpdateRequest));
    }

    /**
     * 获取竞赛
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Contest> getContestById(@RequestParam("id") Long id) {
        return ResultUtils.success(contestService.getContestById(id));
    }

    /**
     * 分页获取获取竞赛
     *
     * @param contestQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<Contest>> listContestByPage(@RequestBody ContestQueryRequest contestQueryRequest, HttpServletRequest request) {
        return ResultUtils.success(contestService.listContestByPage(contestQueryRequest, request));
    }
}
