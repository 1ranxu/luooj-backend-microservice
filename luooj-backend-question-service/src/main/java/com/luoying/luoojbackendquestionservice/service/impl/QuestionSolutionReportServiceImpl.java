package com.luoying.luoojbackendquestionservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luoying.luoojbackendcommon.common.DeleteRequest;
import com.luoying.luoojbackendcommon.constant.CommonConstant;
import com.luoying.luoojbackendcommon.utils.SqlUtils;
import com.luoying.luoojbackendmodel.dto.question_solution_report.QuestionSolutionReportAddRequest;
import com.luoying.luoojbackendmodel.dto.question_solution_report.QuestionSolutionReportQueryRequest;
import com.luoying.luoojbackendmodel.dto.question_solution_report.SolutionDeleteRequest;
import com.luoying.luoojbackendmodel.entity.QuestionSolutionReport;
import com.luoying.luoojbackendmodel.entity.User;
import com.luoying.luoojbackendquestionservice.mapper.QuestionSolutionReportMapper;
import com.luoying.luoojbackendquestionservice.service.QuestionSolutionReportService;
import com.luoying.luoojbackendquestionservice.service.QuestionSolutionService;
import com.luoying.luoojbackendserviceclient.service.UserFeignClient;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author 落樱的悔恨
 * @description 针对表【question_solution_report(题解举报表)】的数据库操作Service实现
 * @createDate 2024-09-09 20:44:51
 */
@Service
public class QuestionSolutionReportServiceImpl extends ServiceImpl<QuestionSolutionReportMapper, QuestionSolutionReport> implements QuestionSolutionReportService {
    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private QuestionSolutionService questionSolutionService;

    /**
     * 新增题解举报
     *
     * @param questionSolutionReportAddRequest
     * @param request
     * @return
     */
    @Override
    public Boolean addQuestionSolutionReport(QuestionSolutionReportAddRequest questionSolutionReportAddRequest, HttpServletRequest request) {
        // 获取登录用户
        User loginUser = userFeignClient.getLoginUser(request);
        // 拷贝
        QuestionSolutionReport questionSolutionReport = BeanUtil.copyProperties(questionSolutionReportAddRequest, QuestionSolutionReport.class);
        questionSolutionReport.setUserId(loginUser.getId());
        // 写入
        return this.save(questionSolutionReport);
    }

    /**
     * 删除题解举报
     *
     * @param id
     * @return
     */
    @Override
    public Boolean deleteQuestionSolutionReport(Long id) {
        // 删除
        return this.removeById(id);
    }

    /**
     * 确定违规，删除对应题解
     *
     * @param solutionDeleteRequest
     * @param request
     * @return
     */
    @Override
    public Boolean deleteQuestionSolutionAfterConfirm(SolutionDeleteRequest solutionDeleteRequest, HttpServletRequest request) {
        // 删除题解
        DeleteRequest deleteRequest = new DeleteRequest();
        deleteRequest.setId(solutionDeleteRequest.getSolutionId());
        Boolean isSuccess = questionSolutionService.deleteQuestionSolution(deleteRequest, request);
        // 删除本条题解举报记录
        return this.removeById(solutionDeleteRequest.getId()) && isSuccess;
    }

    /**
     * 获取题解举报
     *
     * @param id
     * @return
     */
    @Override
    public QuestionSolutionReport getQuestionSolutionReportById(Long id) {
        return this.getById(id);
    }

    /**
     * 获取查询条件
     *
     * @param questionSolutionReportQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<QuestionSolutionReport> getQueryWrapper(QuestionSolutionReportQueryRequest questionSolutionReportQueryRequest) {
        QueryWrapper<QuestionSolutionReport> queryWrapper = new QueryWrapper<>();
        // 判空
        if (questionSolutionReportQueryRequest == null) {
            return queryWrapper;
        }
        // 获取参数
        Long id = questionSolutionReportQueryRequest.getId();
        Long userId = questionSolutionReportQueryRequest.getUserId();
        Long solutionId = questionSolutionReportQueryRequest.getSolutionId();
        Long reportedUserId = questionSolutionReportQueryRequest.getReportedUserId();
        String sortField = questionSolutionReportQueryRequest.getSortField();
        String sortOrder = questionSolutionReportQueryRequest.getSortOrder();


        // 拼接查询条件
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(solutionId), "solutionId", solutionId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(reportedUserId), "reportedUserId", reportedUserId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        // 返回
        return queryWrapper;
    }

    /**
     * 分页获取题解举报
     *
     * @param questionSolutionReportQueryRequest
     * @return
     */
    @Override
    public Page<QuestionSolutionReport> listQuestionSolutionReportByPage(QuestionSolutionReportQueryRequest questionSolutionReportQueryRequest) {
        // 获取分页参数
        long current = questionSolutionReportQueryRequest.getCurrent();
        long size = questionSolutionReportQueryRequest.getPageSize();
        // 查询
        return this.page(new Page<>(current, size), this.getQueryWrapper(questionSolutionReportQueryRequest));
    }
}




