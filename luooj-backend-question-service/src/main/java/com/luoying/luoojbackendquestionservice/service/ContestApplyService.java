package com.luoying.luoojbackendquestionservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luoying.luoojbackendmodel.dto.contest_apply.ContestApplyAddRequest;
import com.luoying.luoojbackendmodel.dto.contest_apply.ContestApplyQueryRequest;
import com.luoying.luoojbackendmodel.entity.ContestApply;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 落樱的悔恨
 * @description 针对表【contest_apply(竞赛报名表)】的数据库操作Service
 * @createDate 2024-09-02 14:48:23
 */
public interface ContestApplyService extends IService<ContestApply> {

    /**
     * 报名/取消报名
     *
     * @param contestApplyAddRequest
     * @param request
     * @return
     */
    Boolean applyContest(ContestApplyAddRequest contestApplyAddRequest, HttpServletRequest request);

    /**
     * 判断是否报名
     *
     * @param contestId
     * @param userId
     * @return
     */
    Boolean isApply(Long contestId, Long userId);

    /**
     * 获取报名
     *
     * @param id
     * @return
     */
    ContestApply getContestApplyById(Long id);

    /**
     * 获取查询条件
     *
     * @param contestApplyQueryRequest
     */
    QueryWrapper<ContestApply> getQueryWrapper(ContestApplyQueryRequest contestApplyQueryRequest);

    /**
     * 分页获取报名
     *
     * @param contestApplyQueryRequest
     * @return
     */
    Page<ContestApply> listContestApplyByPage(ContestApplyQueryRequest contestApplyQueryRequest);
}
