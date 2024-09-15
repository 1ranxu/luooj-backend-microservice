package com.luoying.luoojbackendquestionservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luoying.luoojbackendcommon.common.ErrorCode;
import com.luoying.luoojbackendcommon.constant.CommonConstant;
import com.luoying.luoojbackendcommon.exception.ThrowUtils;
import com.luoying.luoojbackendcommon.utils.SqlUtils;
import com.luoying.luoojbackendmodel.dto.question_list_collect.QuestionListCollectAddRequest;
import com.luoying.luoojbackendmodel.dto.question_list_collect.QuestionListCollectDeleteRequest;
import com.luoying.luoojbackendmodel.dto.question_list_collect.QuestionListCollectQueryRequest;
import com.luoying.luoojbackendmodel.entity.QuestionListCollect;
import com.luoying.luoojbackendmodel.entity.User;
import com.luoying.luoojbackendquestionservice.mapper.QuestionListCollectMapper;
import com.luoying.luoojbackendquestionservice.service.QuestionListCollectService;
import com.luoying.luoojbackendserviceclient.service.UserFeignClient;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author 落樱的悔恨
 * @description 针对表【question_list_collect(题单收藏表)】的数据库操作Service实现
 * @createDate 2024-09-02 14:36:48
 */
@Service
public class QuestionListCollectServiceImpl extends ServiceImpl<QuestionListCollectMapper, QuestionListCollect> implements QuestionListCollectService {
    @Resource
    private UserFeignClient userFeignClient;

    /**
     * 收藏题单
     *
     * @param questionListCollectAddRequest
     * @param request
     * @return
     */
    @Override
    public Boolean addQuestionListCollect(QuestionListCollectAddRequest questionListCollectAddRequest, HttpServletRequest request) {
        // 获取登录用户
        User loginUser = userFeignClient.getLoginUser(request);
        // 拷贝
        QuestionListCollect questionListCollect = BeanUtil.copyProperties(questionListCollectAddRequest, QuestionListCollect.class);
        questionListCollect.setUserId(loginUser.getId());
        // 写入
        return this.save(questionListCollect);
    }

    /**
     * 取消收藏题单
     *
     * @param questionListCollectDeleteRequest
     * @param request
     * @return
     */
    @Override
    public Boolean deleteQuestionListCollect(QuestionListCollectDeleteRequest questionListCollectDeleteRequest, HttpServletRequest request) {
        // 获取登录用户
        User loginUser = userFeignClient.getLoginUser(request);
        // 删除
        LambdaQueryWrapper<QuestionListCollect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QuestionListCollect::getQuestionListId, questionListCollectDeleteRequest.getQuestionListId());
        queryWrapper.eq(QuestionListCollect::getUserId, loginUser.getId());
        return this.remove(queryWrapper);
    }

    /**
     * 获取题单收藏
     *
     * @param id
     * @return
     */
    @Override
    public QuestionListCollect getQuestionListCollectById(Long id) {
        return this.getById(id);
    }

    /**
     * 获取查询条件
     *
     * @param questionListCollectQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionListCollect> getQueryWrapper(QuestionListCollectQueryRequest questionListCollectQueryRequest) {
        QueryWrapper<QuestionListCollect> queryWrapper = new QueryWrapper<>();
        // 判空
        if (questionListCollectQueryRequest == null) {
            return queryWrapper;
        }
        // 获取参数
        Long id = questionListCollectQueryRequest.getId();
        Long questionListId = questionListCollectQueryRequest.getQuestionListId();
        Long userId = questionListCollectQueryRequest.getUserId();
        String sortField = questionListCollectQueryRequest.getSortField();
        String sortOrder = questionListCollectQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionListId), "questionListId", questionListId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        // 返回
        return queryWrapper;
    }

    /**
     * 分页获取题单收藏（仅管理员）
     *
     * @param questionListCollectQueryRequest
     * @return
     */
    @Override
    public Page<QuestionListCollect> listQuestionListCollectByPage(QuestionListCollectQueryRequest questionListCollectQueryRequest) {
        // 获取分页参数
        long current = questionListCollectQueryRequest.getCurrent();
        long size = questionListCollectQueryRequest.getPageSize();
        // 查询
        return this.page(new Page<>(current, size), this.getQueryWrapper(questionListCollectQueryRequest));
    }

    /**
     * 分页获取题单收藏
     *
     * @param questionListCollectQueryRequest
     * @return
     */
    @Override
    public Page<QuestionListCollect> listQuestionListCollectByPageUser(QuestionListCollectQueryRequest questionListCollectQueryRequest) {
        // 获取分页参数
        long current = questionListCollectQueryRequest.getCurrent();
        long size = questionListCollectQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 50, ErrorCode.PARAMS_ERROR);
        // 查询
        return this.page(new Page<>(current, size), this.getQueryWrapper(questionListCollectQueryRequest));
    }
}




