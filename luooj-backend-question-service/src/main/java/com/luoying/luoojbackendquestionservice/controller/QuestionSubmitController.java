package com.luoying.luoojbackendquestionservice.controller;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luoying.luoojbackendcommon.common.BaseResponse;
import com.luoying.luoojbackendcommon.common.ResultUtils;
import com.luoying.luoojbackendmodel.codesanbox.RunCodeRequest;
import com.luoying.luoojbackendmodel.codesanbox.RunCodeResponse;
import com.luoying.luoojbackendmodel.dto.question_submit.QuestionSubmitAddRequest;
import com.luoying.luoojbackendmodel.dto.question_submit.QuestionSubmitDetail;
import com.luoying.luoojbackendmodel.dto.question_submit.QuestionSubmitQueryRequest;
import com.luoying.luoojbackendmodel.entity.User;
import com.luoying.luoojbackendmodel.vo.QuestionSubmitVO;
import com.luoying.luoojbackendquestionservice.service.QuestionSubmitService;
import com.luoying.luoojbackendserviceclient.service.UserFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/5 19:07
 */
@RestController
@RequestMapping("/question_submit")
@Slf4j
public class QuestionSubmitController {
    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private UserFeignClient userFeignClient;

    static {
        initFlowRules();
    }

    /**
     * Sentinel限流策略
     */
    private static void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule = new FlowRule();
        rule.setResource("doQuestionSubmit");
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        // Set limit QPS to 114.
        rule.setCount(114);
        rules.add(rule);
        FlowRuleManager.loadRules(rules);
    }

    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest 提交题目请求
     * @param request                  {@link HttpServletRequest}
     */
    @PostMapping("/")
    // @SentinelResource(value = "doQuestionSubmit", blockHandler = "handleException")
    public BaseResponse<Long> doQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest, HttpServletRequest request) {
        return ResultUtils.success(questionSubmitService.doQuestionSubmit(questionSubmitAddRequest, request));
    }

    /**
     * Sentinel限流异常处理
     *
     * @param exception 异常信息
     */
    public BaseResponse<Long> handleException(BlockException exception) {
        log.info("限流异常信息" + exception);
        return ResultUtils.success(-1L);
    }

    /**
     * 分页获取题目提交列表
     *
     * @param questionSubmitQueryRequest 题目提交查询请求
     * @param request                    {@link HttpServletRequest}
     */
    @PostMapping("/list/page")
    public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest, HttpServletRequest request) {
        return ResultUtils.success(questionSubmitService.getQuestionSubmitVOPage(questionSubmitQueryRequest, request));
    }

    /**
     * 分页获取个人提交列表
     *
     * @param questionSubmitQueryRequest 题目提交查询请求
     * @param request                    {@link HttpServletRequest}
     */
    @PostMapping("/my/list/page")
    public BaseResponse<Page<QuestionSubmitVO>> listMyQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest, HttpServletRequest request) {
        questionSubmitQueryRequest.setUserId(userFeignClient.getLoginUser(request).getId());
        return ResultUtils.success(questionSubmitService.getQuestionSubmitVOPage(questionSubmitQueryRequest, request));
    }

    /**
     * 在线运行代码
     *
     * @param runCodeRequest 运行代码请求
     */
    @PostMapping("/run/online")
    public BaseResponse<RunCodeResponse> questionRunOnline(@RequestBody RunCodeRequest runCodeRequest) {
        return ResultUtils.success(questionSubmitService.questionRunOnline(runCodeRequest));
    }

    /**
     * 获取个人提交详情
     *
     * @param request
     * @return
     */
    @GetMapping("/get/user_submit/detail")
    public BaseResponse<QuestionSubmitDetail> getPersonSubmitDetail(HttpServletRequest request) {
        return ResultUtils.success(questionSubmitService.getPersonSubmitDetail(request));
    }
}
