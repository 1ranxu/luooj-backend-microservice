package com.luoying.luoojbackendquestionservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luoying.luoojbackendcommon.common.ErrorCode;
import com.luoying.luoojbackendcommon.constant.CommonConstant;
import com.luoying.luoojbackendcommon.constant.UserConstant;
import com.luoying.luoojbackendcommon.exception.BusinessException;
import com.luoying.luoojbackendcommon.exception.ThrowUtils;
import com.luoying.luoojbackendcommon.utils.SqlUtils;
import com.luoying.luoojbackendmodel.dto.question_collect.QuestionCollectAddRequest;
import com.luoying.luoojbackendmodel.dto.question_collect.QuestionCollectDeleteRequest;
import com.luoying.luoojbackendmodel.dto.question_collect.QuestionCollectQueryRequest;
import com.luoying.luoojbackendmodel.dto.question_list.QuestionListQueryRequest;
import com.luoying.luoojbackendmodel.entity.QuestionCollect;
import com.luoying.luoojbackendmodel.entity.QuestionList;
import com.luoying.luoojbackendmodel.entity.User;
import com.luoying.luoojbackendmodel.vo.QuestionCollectByUserAllQuestionListDetail;
import com.luoying.luoojbackendmodel.vo.QuestionListVO;
import com.luoying.luoojbackendquestionservice.mapper.QuestionCollectMapper;
import com.luoying.luoojbackendquestionservice.service.QuestionCollectService;
import com.luoying.luoojbackendquestionservice.service.QuestionListService;
import com.luoying.luoojbackendserviceclient.service.UserFeignClient;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

/**
 * @author 落樱的悔恨
 * @description 针对表【question_collect(题单收藏题目表)】的数据库操作Service实现
 * @createDate 2024-09-02 14:33:59
 */
@Service
public class QuestionCollectServiceImpl extends ServiceImpl<QuestionCollectMapper, QuestionCollect> implements QuestionCollectService {
    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private QuestionListService questionListService;

    /**
     * 题单收藏题目
     *
     * @param questionCollectAddRequest
     * @param request
     * @return
     */
    @Override
    public Boolean addQuestionCollect(QuestionCollectAddRequest questionCollectAddRequest, HttpServletRequest request) {
        // 获取登录用户
        User loginUser = userFeignClient.getLoginUser(request);
        // 只有题单的创建人和管理员才能收藏题目
        QuestionList questionList = questionListService.getById(questionCollectAddRequest.getQuestionListId());
        if (!questionList.getUserId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有题单的创建人和管理员才能收藏题目");
        }
        // 拷贝
        QuestionCollect questionCollect = BeanUtil.copyProperties(questionCollectAddRequest, QuestionCollect.class);
        return this.save(questionCollect);
    }

    /**
     * 题单取消收藏题目
     *
     * @param questionCollectDeleteRequest
     * @param request
     * @return
     */
    @Override
    public Boolean deleteQuestionCollect(QuestionCollectDeleteRequest questionCollectDeleteRequest, HttpServletRequest request) {
        // 获取登录用户
        User loginUser = userFeignClient.getLoginUser(request);
        // 只有题单的创建人和管理员才能取消收藏
        QuestionList questionList = questionListService.getById(questionCollectDeleteRequest.getQuestionListId());
        if (!questionList.getUserId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有题单的创建人和管理员才能取消收藏");
        }
        // 删除
        LambdaQueryWrapper<QuestionCollect> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QuestionCollect::getQuestionListId, questionCollectDeleteRequest.getQuestionListId());
        queryWrapper.eq(QuestionCollect::getQuestionId, questionCollectDeleteRequest.getQuestionId());
        return this.remove(queryWrapper);
    }

    /**
     * 获取用户所有题单对某道题目的收藏情况
     * @param questionListQueryRequest
     * @param questionId
     * @return
     */
    @Override
    public QuestionCollectByUserAllQuestionListDetail isQuestionCollectedByUserAllQuestionList(QuestionListQueryRequest questionListQueryRequest, Long questionId) {
        // 根据userId查询用户创建的题单
        QueryWrapper<QuestionList> questionListQueryWrapper = questionListService.getQueryWrapper(questionListQueryRequest);
        long current = questionListQueryRequest.getCurrent();
        long pageSize = questionListQueryRequest.getPageSize();
        Page<QuestionList> questionListPage = new Page<>(current, pageSize);
        questionListService.page(questionListPage, questionListQueryWrapper);
        // 用于存储用户是否收藏某道题目
        AtomicReference<Boolean> isQuestionCollect = new AtomicReference<>(false);
        // 依次判断每个题单是否收藏某道题目
        List<QuestionListVO> questionListVOList = questionListPage.getRecords().stream().map(questionList -> {
            // 将questionList对象拷贝给VO对象
            QuestionListVO questionListVO = new QuestionListVO();
            BeanUtil.copyProperties(questionList, questionListVO);
            // 查询该题单是否收藏某道题目
            LambdaQueryWrapper<QuestionCollect> questionCollectQueryWrapper = new LambdaQueryWrapper<>();
            questionCollectQueryWrapper.eq(QuestionCollect::getQuestionId, questionId);
            questionCollectQueryWrapper.eq(QuestionCollect::getQuestionListId, questionList.getId());
            QuestionCollect questionCollect = this.getOne(questionCollectQueryWrapper);
            // 设置该题单是否收藏某道题目
            if (questionCollect != null) {
                questionListVO.setIsCollect(true);
                isQuestionCollect.set(true);
            }else{
                questionListVO.setIsCollect(false);
            }
            return questionListVO;
        }).collect(Collectors.toList());
        // 封装分页对象
        Page<QuestionListVO> questionListVOPage = new Page<>();
        questionListVOPage.setRecords(questionListVOList);
        questionListVOPage.setTotal(questionListPage.getTotal());
        questionListVOPage.setCurrent(current);
        questionListVOPage.setSize(pageSize);
        // 封装详情对象
        QuestionCollectByUserAllQuestionListDetail questionCollectByUserAllQuestionListDetail = new QuestionCollectByUserAllQuestionListDetail();
        questionCollectByUserAllQuestionListDetail.setIsCollect(isQuestionCollect.get());
        questionCollectByUserAllQuestionListDetail.setQuestionListVOPage(questionListVOPage);
        // 返回
        return questionCollectByUserAllQuestionListDetail;
    }

    /**
     * 获取题目收藏记录
     *
     * @param id
     * @return
     */
    @Override
    public QuestionCollect getQuestionCollectById(Long id) {
        return this.getById(id);
    }

    /**
     * 获取查询条件
     *
     * @param questionCollectQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionCollect> getQueryWrapper(QuestionCollectQueryRequest questionCollectQueryRequest) {
        QueryWrapper<QuestionCollect> queryWrapper = new QueryWrapper<>();
        // 判空
        if (questionCollectQueryRequest == null) {
            return queryWrapper;
        }
        // 获取参数
        Long id = questionCollectQueryRequest.getId();
        Long questionListId = questionCollectQueryRequest.getQuestionListId();
        Long questionId = questionCollectQueryRequest.getQuestionId();
        String sortField = questionCollectQueryRequest.getSortField();
        String sortOrder = questionCollectQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionListId), "questionListId", questionListId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        // 返回
        return queryWrapper;
    }

    /**
     * 分页获取题目收藏（仅管理员）
     *
     * @param questionCollectQueryRequest
     * @return
     */
    @Override
    public Page<QuestionCollect> listQuestionCollectByPage(QuestionCollectQueryRequest questionCollectQueryRequest) {
        // 获取分页参数
        long current = questionCollectQueryRequest.getCurrent();
        long size = questionCollectQueryRequest.getPageSize();
        // 查询
        return this.page(new Page<>(current, size), this.getQueryWrapper(questionCollectQueryRequest));
    }

    /**
     * 分页获取题目收藏
     *
     * @param questionCollectQueryRequest
     * @return
     */
    @Override
    public Page<QuestionCollect> listQuestionCollectByPageUser(QuestionCollectQueryRequest questionCollectQueryRequest) {
        // 获取分页参数
        long current = questionCollectQueryRequest.getCurrent();
        long size = questionCollectQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 50, ErrorCode.PARAMS_ERROR);
        // 查询
        return this.page(new Page<>(current, size), this.getQueryWrapper(questionCollectQueryRequest));
    }
}




