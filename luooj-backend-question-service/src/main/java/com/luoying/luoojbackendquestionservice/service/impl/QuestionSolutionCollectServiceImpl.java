package com.luoying.luoojbackendquestionservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luoying.luoojbackendcommon.common.ErrorCode;
import com.luoying.luoojbackendcommon.constant.CommonConstant;
import com.luoying.luoojbackendcommon.constant.UserConstant;
import com.luoying.luoojbackendcommon.exception.BusinessException;
import com.luoying.luoojbackendcommon.exception.ThrowUtils;
import com.luoying.luoojbackendcommon.utils.SqlUtils;
import com.luoying.luoojbackendmodel.dto.question_solution_collect.QuestionSolutionCollectAddRequest;
import com.luoying.luoojbackendmodel.dto.question_solution_collect.QuestionSolutionCollectQueryRequest;
import com.luoying.luoojbackendmodel.entity.QuestionSolutionCollect;
import com.luoying.luoojbackendmodel.entity.User;
import com.luoying.luoojbackendquestionservice.mapper.QuestionSolutionCollectMapper;
import com.luoying.luoojbackendquestionservice.service.QuestionSolutionCollectService;
import com.luoying.luoojbackendserviceclient.service.UserFeignClient;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author 落樱的悔恨
 * @description 针对表【question_solution_collect(题解收藏表)】的数据库操作Service实现
 * @createDate 2024-09-02 14:43:49
 */
@Service
public class QuestionSolutionCollectServiceImpl extends ServiceImpl<QuestionSolutionCollectMapper, QuestionSolutionCollect>
        implements QuestionSolutionCollectService {
    @Resource
    private UserFeignClient userFeignClient;


    /**
     * 收藏题解
     *
     * @param questionSolutionCollectAddRequest
     * @param request
     * @return
     */
    @Override
    public Boolean addQuestionSolutionCollect(QuestionSolutionCollectAddRequest questionSolutionCollectAddRequest, HttpServletRequest request) {
        // 获取登录用户
        User loginUser = userFeignClient.getLoginUser(request);
        // 拷贝
        QuestionSolutionCollect questionSolutionCollect = BeanUtil.copyProperties(questionSolutionCollectAddRequest, QuestionSolutionCollect.class);
        questionSolutionCollect.setUserId(loginUser.getId());
        // 写入
        return this.save(questionSolutionCollect);
    }

    /**
     * 取消收藏题解
     * @param id
     * @param request
     * @return
     */
    @Override
    public Boolean deleteQuestionSolutionCollect(Long id, HttpServletRequest request) {
        // 获取登录用户
        User loginUser = userFeignClient.getLoginUser(request);
        // 只有本人和管理员才能取消收藏
        QuestionSolutionCollect questionSolutionCollect = this.getById(id);
        if (!questionSolutionCollect.getUserId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有本人或管理员可以取消收藏");
        }
        // 删除
        return this.removeById(id);
    }

    @Override
    public QueryWrapper<QuestionSolutionCollect> getQueryWrapper(QuestionSolutionCollectQueryRequest questionSolutionCollectQueryRequest) {
        QueryWrapper<QuestionSolutionCollect> queryWrapper = new QueryWrapper<>();
        // 判空
        if (questionSolutionCollectQueryRequest == null) {
            return queryWrapper;
        }
        // 获取参数
        Long id = questionSolutionCollectQueryRequest.getId();
        Long solutionId = questionSolutionCollectQueryRequest.getSolutionId();
        Long userId = questionSolutionCollectQueryRequest.getUserId();
        String sortField = questionSolutionCollectQueryRequest.getSortField();
        String sortOrder = questionSolutionCollectQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(solutionId), "solutionId", solutionId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        // 返回
        return queryWrapper;
    }

    /**
     * 分页获取题解收藏（仅管理员）
     * @param questionSolutionCollectQueryRequest
     * @return
     */
    @Override
    public Page<QuestionSolutionCollect> listQuestionSolutionCollectByPage(QuestionSolutionCollectQueryRequest questionSolutionCollectQueryRequest) {
        // 获取分页参数
        long current = questionSolutionCollectQueryRequest.getCurrent();
        long size = questionSolutionCollectQueryRequest.getPageSize();
        // 查询
        return this.page(new Page<>(current, size),
                this.getQueryWrapper(questionSolutionCollectQueryRequest));
    }

    /**
     * 分页获取题解收藏
     * @param questionSolutionCollectQueryRequest
     * @return
     */
    @Override
    public Page<QuestionSolutionCollect> listQuestionSolutionCollectByPageUser(QuestionSolutionCollectQueryRequest questionSolutionCollectQueryRequest) {
        // 获取分页参数
        long current = questionSolutionCollectQueryRequest.getCurrent();
        long size = questionSolutionCollectQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 50, ErrorCode.PARAMS_ERROR);
        // 查询
        return this.page(new Page<>(current, size),
                this.getQueryWrapper(questionSolutionCollectQueryRequest));
    }
}




