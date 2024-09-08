package com.luoying.luoojbackendquestionservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.luoying.luoojbackendcommon.common.DeleteRequest;
import com.luoying.luoojbackendcommon.common.ErrorCode;
import com.luoying.luoojbackendcommon.constant.CommonConstant;
import com.luoying.luoojbackendcommon.constant.UserConstant;
import com.luoying.luoojbackendcommon.exception.BusinessException;
import com.luoying.luoojbackendcommon.exception.ThrowUtils;
import com.luoying.luoojbackendcommon.utils.SqlUtils;
import com.luoying.luoojbackendmodel.dto.question_solution.QuestionSolutionAddRequest;
import com.luoying.luoojbackendmodel.dto.question_solution.QuestionSolutionQueryRequest;
import com.luoying.luoojbackendmodel.dto.question_solution.QuestionSolutionUpdateRequest;
import com.luoying.luoojbackendmodel.entity.QuestionSolution;
import com.luoying.luoojbackendmodel.entity.User;
import com.luoying.luoojbackendquestionservice.mapper.QuestionSolutionMapper;
import com.luoying.luoojbackendquestionservice.service.QuestionSolutionService;
import com.luoying.luoojbackendserviceclient.service.UserFeignClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 落樱的悔恨
 * @description 针对表【question_solution(题解表)】的数据库操作Service实现
 * @createDate 2024-09-02 14:39:32
 */
@Service
public class QuestionSolutionServiceImpl extends ServiceImpl<QuestionSolutionMapper, QuestionSolution>
        implements QuestionSolutionService {

    @Resource
    private UserFeignClient userFeignClient;

    private final static Gson GSON = new Gson();

    /**
     * 创建题解
     *
     * @param questionSolutionAddRequest
     * @param request
     * @return
     */
    @Override
    public Boolean addQuestionSolution(QuestionSolutionAddRequest questionSolutionAddRequest, HttpServletRequest request) {
        // 获取登录用户id
        Long userId = userFeignClient.getLoginUser(request).getId();
        // 拷贝
        QuestionSolution questionSolution = BeanUtil.copyProperties(questionSolutionAddRequest, QuestionSolution.class);
        // 获取题目标签
        List<String> tags = questionSolutionAddRequest.getTags();
        if (tags != null) {
            // 转Json字符串
            questionSolution.setTags(GSON.toJson(tags));
        }
        // 设置创建人id
        questionSolution.setUserId(userId);
        // 写入数据库
        return this.save(questionSolution);
    }

    /**
     * 删除题解
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @Override
    public Boolean deleteQuestionSolution(DeleteRequest deleteRequest, HttpServletRequest request) {
        // 获取登录用户id
        User loginUser = userFeignClient.getLoginUser(request);
        Long userId = loginUser.getId();
        // 只有管理员和本人才能删除
        QuestionSolution questionSolution = this.getById(deleteRequest.getId());
        if (!questionSolution.getUserId().equals(userId) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有本人或管理员可以删除");
        }
        // 删除
        return this.removeById(deleteRequest.getId());
    }

    /**
     * 更新题解
     *
     * @param questionSolutionUpdateRequest
     * @param request
     * @return
     */
    @Override
    public Boolean updateQuestionSolution(QuestionSolutionUpdateRequest questionSolutionUpdateRequest, HttpServletRequest request) {
        // 获取登录用户id
        User loginUser = userFeignClient.getLoginUser(request);
        Long userId = loginUser.getId();
        // 只有管理员和本人才能更新
        QuestionSolution questionSolution = this.getById(questionSolutionUpdateRequest.getId());
        if (!questionSolution.getUserId().equals(userId) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有本人或管理员可以修改");
        }
        // 拷贝
        questionSolution = BeanUtil.copyProperties(questionSolutionUpdateRequest, QuestionSolution.class);
        // 获取题目标签
        List<String> tags = questionSolutionUpdateRequest.getTags();
        if (tags != null) {
            // 转Json字符串
            questionSolution.setTags(GSON.toJson(tags));
        }
        // 更新
        return this.updateById(questionSolution);
    }

    /**
     * 查询题解
     *
     * @param id
     * @return
     */
    @Override
    public QuestionSolution getQuestionSolutionById(Long id) {
        return this.getById(id);
    }

    /**
     * 获取条件构造器
     *
     * @param questionSolutionQueryRequest
     * @return
     */
    @Override
    public Wrapper<QuestionSolution> getQueryWrapper(QuestionSolutionQueryRequest questionSolutionQueryRequest) {
        QueryWrapper<QuestionSolution> queryWrapper = new QueryWrapper<>();
        // 判空
        if (questionSolutionQueryRequest == null) {
            return queryWrapper;
        }
        // 获取参数
        Long id = questionSolutionQueryRequest.getId();
        Long questionId = questionSolutionQueryRequest.getQuestionId();
        Long userId = questionSolutionQueryRequest.getUserId();
        String title = questionSolutionQueryRequest.getTitle();
        String content = questionSolutionQueryRequest.getContent();
        List<String> tags = questionSolutionQueryRequest.getTags();
        String sortField = questionSolutionQueryRequest.getSortField();
        String sortOrder = questionSolutionQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        if (CollectionUtils.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        // 返回
        return queryWrapper;
    }

    /**
     * 分页获取题解列表（仅管理员）
     *
     * @param questionSolutionQueryRequest
     * @return
     */
    @Override
    public Page<QuestionSolution> listQuestionSolutionByPage(QuestionSolutionQueryRequest questionSolutionQueryRequest) {
        // 获取分页参数
        long current = questionSolutionQueryRequest.getCurrent();
        long size = questionSolutionQueryRequest.getPageSize();
        // 查询
        return this.page(new Page<>(current, size),
                this.getQueryWrapper(questionSolutionQueryRequest));
    }

    /**
     * 分页获取题解列表（普通用户）
     *
     * @param questionSolutionQueryRequest
     * @return
     */
    @Override
    public Page<QuestionSolution> listQuestionSolutionByPageUser(QuestionSolutionQueryRequest questionSolutionQueryRequest) {
        // 获取分页参数
        long current = questionSolutionQueryRequest.getCurrent();
        long size = questionSolutionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 50, ErrorCode.PARAMS_ERROR);
        // 查询
        return this.page(new Page<>(current, size),
                this.getQueryWrapper(questionSolutionQueryRequest));
    }
}




