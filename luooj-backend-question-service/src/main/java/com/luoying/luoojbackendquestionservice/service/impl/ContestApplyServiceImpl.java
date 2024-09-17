package com.luoying.luoojbackendquestionservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luoying.luoojbackendcommon.constant.CommonConstant;
import com.luoying.luoojbackendcommon.constant.RedisKey;
import com.luoying.luoojbackendcommon.utils.SqlUtils;
import com.luoying.luoojbackendmodel.dto.contest_apply.ContestApplyAddRequest;
import com.luoying.luoojbackendmodel.dto.contest_apply.ContestApplyQueryRequest;
import com.luoying.luoojbackendmodel.entity.ContestApply;
import com.luoying.luoojbackendmodel.entity.User;
import com.luoying.luoojbackendquestionservice.mapper.ContestApplyMapper;
import com.luoying.luoojbackendquestionservice.service.ContestApplyService;
import com.luoying.luoojbackendserviceclient.service.UserFeignClient;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static com.luoying.luoojbackendcommon.constant.RedisKey.CONTEST_APPLY_KEY;

/**
 * @author 落樱的悔恨
 * @description 针对表【contest_apply(竞赛报名表)】的数据库操作Service实现
 * @createDate 2024-09-02 14:48:23
 */
@Service
public class ContestApplyServiceImpl extends ServiceImpl<ContestApplyMapper, ContestApply> implements ContestApplyService {
    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 报名/取消报名
     *
     * @param contestApplyAddRequest
     * @param request
     * @return
     */
    @Override
    public Boolean applyContest(ContestApplyAddRequest contestApplyAddRequest, HttpServletRequest request) {
        // 获取登录用户
        User loginUser = userFeignClient.getLoginUser(request);
        String key = RedisKey.getKey(CONTEST_APPLY_KEY, contestApplyAddRequest.getContestId());
        if (BooleanUtil.isTrue(contestApplyAddRequest.getIsApply())) { // 报名
            // 拷贝
            ContestApply contestApply = BeanUtil.copyProperties(contestApplyAddRequest, ContestApply.class);
            contestApply.setApplicantId(loginUser.getId());
            // 写入
            boolean isSuccess = this.save(contestApply);
            if (isSuccess) {
                // 把参赛用户的id，放入redis的set集合
                stringRedisTemplate.opsForSet().add(key, loginUser.getId().toString());
            }
            return isSuccess;
        } else { // 取消报名
            // 删除
            LambdaQueryWrapper<ContestApply> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ContestApply::getContestId, contestApplyAddRequest.getContestId());
            queryWrapper.eq(ContestApply::getApplicantId, loginUser.getId());
            boolean isSuccess = this.remove(queryWrapper);
            if (isSuccess) {
                // 把参赛用户的id，移除redis的set集合
                stringRedisTemplate.opsForSet().remove(key, loginUser.getId().toString());
            }
            return isSuccess;
        }
    }

    /**
     * 判断是否报名
     *
     * @param contestId
     * @param userId
     * @return
     */
    @Override
    public Boolean isApply(Long contestId, Long userId) {
        // 1.查询缓存
        String key = RedisKey.getKey(CONTEST_APPLY_KEY, contestId);
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(key, userId.toString());
        if (BooleanUtil.isTrue(isMember)) { // 缓存存在直接返回
            return true;
        } else { // 不存在，查询数据库
            LambdaQueryWrapper<ContestApply> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ContestApply::getApplicantId, userId).eq(ContestApply::getContestId, contestId);
            ContestApply contestApply = this.getOne(queryWrapper);
            if (contestApply == null) {
                return false;
            } else {
                stringRedisTemplate.opsForSet().add(key, userId.toString());
                return true;
            }
        }
    }

    /**
     * 获取报名
     *
     * @param id
     * @return
     */
    @Override
    public ContestApply getContestApplyById(Long id) {
        return this.getById(id);
    }

    /**
     * 获取查询条件
     *
     * @param contestApplyQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<ContestApply> getQueryWrapper(ContestApplyQueryRequest contestApplyQueryRequest) {
        QueryWrapper<ContestApply> queryWrapper = new QueryWrapper<>();
        // 判空
        if (contestApplyQueryRequest == null) {
            return queryWrapper;
        }
        // 获取参数
        Long contestId = contestApplyQueryRequest.getContestId();
        Long applicantId = contestApplyQueryRequest.getApplicantId();
        String sortField = contestApplyQueryRequest.getSortField();
        String sortOrder = contestApplyQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq(ObjectUtils.isNotEmpty(contestId), "contestId", contestId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(applicantId), "applicantId", applicantId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        // 返回
        return queryWrapper;
    }

    /**
     * 分页获取报名
     *
     * @param contestApplyQueryRequest
     * @return
     */
    @Override
    public Page<ContestApply> listContestApplyByPage(ContestApplyQueryRequest contestApplyQueryRequest) {
        // 获取分页参数
        long current = contestApplyQueryRequest.getCurrent();
        long size = contestApplyQueryRequest.getPageSize();
        // 查询
        return this.page(new Page<>(current, size), this.getQueryWrapper(contestApplyQueryRequest));
    }
}




