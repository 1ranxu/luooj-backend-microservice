package com.luoying.luoojbackendquestionservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luoying.luoojbackendcommon.common.ErrorCode;
import com.luoying.luoojbackendcommon.constant.CommonConstant;
import com.luoying.luoojbackendcommon.constant.UserConstant;
import com.luoying.luoojbackendcommon.exception.BusinessException;
import com.luoying.luoojbackendcommon.exception.ThrowUtils;
import com.luoying.luoojbackendcommon.utils.SqlUtils;
import com.luoying.luoojbackendmodel.dto.question_list.QuestionListAddRequest;
import com.luoying.luoojbackendmodel.dto.question_list.QuestionListQueryRequest;
import com.luoying.luoojbackendmodel.dto.question_list.QuestionListUpdateRequest;
import com.luoying.luoojbackendmodel.entity.QuestionList;
import com.luoying.luoojbackendmodel.entity.User;
import com.luoying.luoojbackendquestionservice.mapper.QuestionListMapper;
import com.luoying.luoojbackendquestionservice.service.QuestionListService;
import com.luoying.luoojbackendserviceclient.service.UserFeignClient;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author 落樱的悔恨
 * @description 针对表【question_list(题单)】的数据库操作Service实现
 * @createDate 2024-09-02 14:30:38
 */
@Service
public class QuestionListServiceImpl extends ServiceImpl<QuestionListMapper, QuestionList>
        implements QuestionListService {
    @Resource
    private UserFeignClient userFeignClient;

    /**
     * 创建题单
     *
     * @param questionListAddRequest
     * @param request
     * @return
     */
    @Override
    public Boolean addQuestionList(QuestionListAddRequest questionListAddRequest, HttpServletRequest request) {
        // 获取登录用户
        User loginUser = userFeignClient.getLoginUser(request);
        // 拷贝
        QuestionList questionList = BeanUtil.copyProperties(questionListAddRequest, QuestionList.class);
        questionList.setUserId(loginUser.getId());
        // 写入
        return this.save(questionList);
    }

    /**
     * 删除题单
     *
     * @param id
     * @param request
     * @return
     */
    @Override
    public Boolean deleteQuestionList(Long id, HttpServletRequest request) {
        // 获取登录用户
        User loginUser = userFeignClient.getLoginUser(request);
        // 只有本人和管理员才能删除题单
        QuestionList questionList = this.getById(id);
        if (!questionList.getUserId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有本人或管理员可以删除题单");
        }
        // 删除
        return this.removeById(id);
    }

    /**
     * 更新题单
     *
     * @param questionListUpdateRequest
     * @param request
     * @return
     */
    @Override
    public Boolean updateQuestionList(QuestionListUpdateRequest questionListUpdateRequest, HttpServletRequest request) {
        // 获取登录用户
        User loginUser = userFeignClient.getLoginUser(request);
        // 只有本人和管理员才能更新题单
        QuestionList questionList = this.getById(questionListUpdateRequest);
        if (!questionList.getUserId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有本人或管理员可以更新题单");
        }
        // 拷贝
        questionList = BeanUtil.copyProperties(questionListUpdateRequest, QuestionList.class);
        // 更新
        return this.updateById(questionList);
    }

    /**
     * 获取题单
     *
     * @param id
     * @return
     */
    @Override
    public QuestionList getQuestionListById(Long id) {
        return this.getById(id);
    }

    /**
     * 获取查询条件
     *
     * @param questionListQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionList> getQueryWrapper(QuestionListQueryRequest questionListQueryRequest) {
        QueryWrapper<QuestionList> queryWrapper = new QueryWrapper<>();
        // 判空
        if (questionListQueryRequest == null) {
            return queryWrapper;
        }
        // 获取参数
        Long id = questionListQueryRequest.getId();
        String title = questionListQueryRequest.getTitle();
        Long userId = questionListQueryRequest.getUserId();
        String sortField = questionListQueryRequest.getSortField();
        String sortOrder = questionListQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.like(StrUtil.isNotBlank(title), "title", title);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        // 返回
        return queryWrapper;
    }

    /**
     * 分页获取题单（仅管理员）
     *
     * @param questionListQueryRequest
     * @return
     */
    @Override
    public Page<QuestionList> listQuestionListByPage(QuestionListQueryRequest questionListQueryRequest) {
        // 获取分页参数
        long current = questionListQueryRequest.getCurrent();
        long size = questionListQueryRequest.getPageSize();
        // 查询
        return this.page(new Page<>(current, size),
                this.getQueryWrapper(questionListQueryRequest));
    }

    /**
     * 分页获取题单
     *
     * @param questionListQueryRequest
     * @return
     */
    @Override
    public Page<QuestionList> listQuestionListByPageUser(QuestionListQueryRequest questionListQueryRequest) {
        // 获取分页参数
        long current = questionListQueryRequest.getCurrent();
        long size = questionListQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 50, ErrorCode.PARAMS_ERROR);
        // 查询
        return this.page(new Page<>(current, size),
                this.getQueryWrapper(questionListQueryRequest));
    }
}




