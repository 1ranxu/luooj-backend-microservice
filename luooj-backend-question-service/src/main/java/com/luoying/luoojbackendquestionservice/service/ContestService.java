package com.luoying.luoojbackendquestionservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luoying.luoojbackendmodel.dto.contest.ContestAddRequest;
import com.luoying.luoojbackendmodel.dto.contest.ContestQueryRequest;
import com.luoying.luoojbackendmodel.dto.contest.ContestUpdateRequest;
import com.luoying.luoojbackendmodel.entity.Contest;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 落樱的悔恨
 * @description 针对表【contest(竞赛表)】的数据库操作Service
 * @createDate 2024-09-02 14:46:23
 */
public interface ContestService extends IService<Contest> {

    /**
     * 创建竞赛
     *
     * @param contestAddRequest
     * @param request
     * @return
     */
    Boolean addContest(ContestAddRequest contestAddRequest, HttpServletRequest request);

    /**
     * 删除竞赛
     *
     * @param id
     * @return
     */
    Boolean deleteContest(Long id);

    /**
     * 更新竞赛
     *
     * @param contestUpdateRequest
     * @return
     */
    Boolean updateContest(ContestUpdateRequest contestUpdateRequest);

    /**
     * 获取竞赛
     *
     * @param id
     * @return
     */
    Contest getContestById(Long id);

    /**
     * 获取查询条件
     *
     * @param contestQueryRequest
     * @return
     */
    QueryWrapper<Contest> getQueryWrapper(ContestQueryRequest contestQueryRequest);

    /**
     * 分页获取获取竞赛
     *
     * @param contestQueryRequest
     * @return
     */
    Page<Contest> listContestByPage(ContestQueryRequest contestQueryRequest, HttpServletRequest request);
}
