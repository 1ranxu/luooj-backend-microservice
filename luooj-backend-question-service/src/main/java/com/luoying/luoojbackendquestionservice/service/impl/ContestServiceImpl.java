package com.luoying.luoojbackendquestionservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.luoying.luoojbackendcommon.common.ErrorCode;
import com.luoying.luoojbackendcommon.constant.CommonConstant;
import com.luoying.luoojbackendcommon.constant.RedisKey;
import com.luoying.luoojbackendcommon.exception.ThrowUtils;
import com.luoying.luoojbackendcommon.utils.SqlUtils;
import com.luoying.luoojbackendmodel.dto.contest.ContestAddRequest;
import com.luoying.luoojbackendmodel.dto.contest.ContestQueryRequest;
import com.luoying.luoojbackendmodel.dto.contest.ContestUpdateRequest;
import com.luoying.luoojbackendmodel.entity.Contest;
import com.luoying.luoojbackendmodel.entity.ContestApply;
import com.luoying.luoojbackendmodel.entity.ContestResult;
import com.luoying.luoojbackendmodel.entity.User;
import com.luoying.luoojbackendquestionservice.mapper.ContestMapper;
import com.luoying.luoojbackendquestionservice.service.ContestApplyService;
import com.luoying.luoojbackendquestionservice.service.ContestResultService;
import com.luoying.luoojbackendquestionservice.service.ContestService;
import com.luoying.luoojbackendserviceclient.service.UserFeignClient;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static com.luoying.luoojbackendcommon.constant.RedisKey.CONTEST_APPLY_KEY;
import static com.luoying.luoojbackendcommon.constant.RedisKey.CONTEST_RANK_KEY;

/**
 * @author 落樱的悔恨
 * @description 针对表【contest(竞赛表)】的数据库操作Service实现
 * @createDate 2024-09-02 14:46:23
 */
@Service
public class ContestServiceImpl extends ServiceImpl<ContestMapper, Contest> implements ContestService {
    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private ContestApplyService contestApplyService;

    @Resource
    @Lazy
    private ContestResultService contestResultService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final Gson GSON = new Gson();

    /**
     * 创建竞赛
     *
     * @param contestAddRequest
     * @param request
     * @return
     */
    @Override
    public Boolean addContest(ContestAddRequest contestAddRequest, HttpServletRequest request) {
        // 获取登录用户
        User loginUser = userFeignClient.getLoginUser(request);
        // 拷贝
        Contest contest = BeanUtil.copyProperties(contestAddRequest, Contest.class);
        if (contestAddRequest.getQuestions() != null) {
            contest.setQuestions(GSON.toJson(contestAddRequest.getQuestions()));
        }
        contest.setUserId(loginUser.getId());
        // 写入
        return this.save(contest);
    }

    /**
     * 删除竞赛
     *
     * @param id
     * @return
     */
    @Override
    public Boolean deleteContest(Long id) {
        // 删除报名信息
        LambdaQueryWrapper<ContestApply> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(ContestApply::getContestId, id);
        boolean isSuccess1 = contestApplyService.remove(queryWrapper1);
        if (isSuccess1) {// 删除缓存
            String key = RedisKey.getKey(CONTEST_APPLY_KEY, id);
            stringRedisTemplate.delete(key);
        }
        // 删除比赛结果
        LambdaQueryWrapper<ContestResult> queryWrapper2 = new LambdaQueryWrapper<>();
        queryWrapper2.eq(ContestResult::getContestId, id);
        boolean isSuccess2 = contestResultService.remove(queryWrapper2);
        if (isSuccess2) {// 删除缓存
            String key = RedisKey.getKey(CONTEST_RANK_KEY, id);
            stringRedisTemplate.delete(key);
        }
        // 删除竞赛
        return this.removeById(id);
    }

    /**
     * 更新竞赛
     *
     * @param contestUpdateRequest
     * @return
     */
    @Override
    public Boolean updateContest(ContestUpdateRequest contestUpdateRequest) {
        // 拷贝
        Contest contest = BeanUtil.copyProperties(contestUpdateRequest, Contest.class);
        if (contestUpdateRequest.getQuestions() != null) {
            contest.setQuestions(GSON.toJson(contestUpdateRequest.getQuestions()));
        }
        // 更新
        return this.updateById(contest);
    }

    /**
     * 获取竞赛
     *
     * @param id
     * @return
     */
    @Override
    public Contest getContestById(Long id) {
        return this.getById(id);
    }

    /**
     * 获取查询条件
     *
     * @param contestQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Contest> getQueryWrapper(ContestQueryRequest contestQueryRequest) {
        QueryWrapper<Contest> queryWrapper = new QueryWrapper<>();
        // 判空
        if (contestQueryRequest == null) {
            return queryWrapper;
        }
        // 获取参数
        Long id = contestQueryRequest.getId();
        String title = contestQueryRequest.getTitle();
        String award = contestQueryRequest.getAward();
        String tips = contestQueryRequest.getTips();
        Integer status = contestQueryRequest.getStatus();
        String sortField = contestQueryRequest.getSortField();
        String sortOrder = contestQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.like(StrUtil.isNotBlank(title), "title", title);
        queryWrapper.like(StrUtil.isNotBlank(award), "award", award);
        queryWrapper.like(StrUtil.isNotBlank(tips), "tips", tips);
        queryWrapper.eq(ObjectUtils.isNotEmpty(status), "status", status);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        // 返回
        return queryWrapper;
    }

    /**
     * 分页获取获取竞赛
     *
     * @param contestQueryRequest
     * @return
     */
    @Override
    public Page<Contest> listContestByPage(ContestQueryRequest contestQueryRequest, HttpServletRequest request) {
        // 获取分页参数
        long current = contestQueryRequest.getCurrent();
        long size = contestQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 50, ErrorCode.PARAMS_ERROR);
        // 查询
        Page<Contest> page = this.page(new Page<>(current, size), this.getQueryWrapper(contestQueryRequest));
        User loginUser = userFeignClient.getLoginUser(request);
        for (Contest contest : page.getRecords()) {
            contest.setIsApply(contestApplyService.isApply(contest.getId(), loginUser.getId()));
        }
        return page;
    }
}




