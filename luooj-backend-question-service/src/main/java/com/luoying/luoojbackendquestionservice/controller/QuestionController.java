package com.luoying.luoojbackendquestionservice.controller;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.Gson;
import com.luoying.luoojbackendcommon.annotation.AuthCheck;
import com.luoying.luoojbackendcommon.common.BaseResponse;
import com.luoying.luoojbackendcommon.common.DeleteRequest;
import com.luoying.luoojbackendcommon.common.ErrorCode;
import com.luoying.luoojbackendcommon.common.ResultUtils;
import com.luoying.luoojbackendcommon.constant.RedisKey;
import com.luoying.luoojbackendcommon.constant.UserConstant;
import com.luoying.luoojbackendcommon.exception.BusinessException;
import com.luoying.luoojbackendcommon.exception.ThrowUtils;
import com.luoying.luoojbackendmodel.codesanbox.ExecuteCodeRequest;
import com.luoying.luoojbackendmodel.codesanbox.ExecuteCodeResponse;
import com.luoying.luoojbackendmodel.codesanbox.RunCodeRequest;
import com.luoying.luoojbackendmodel.codesanbox.RunCodeResponse;
import com.luoying.luoojbackendmodel.dto.question.*;
import com.luoying.luoojbackendmodel.dto.questionsubmit.QuestionSubmitAddRequest;
import com.luoying.luoojbackendmodel.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.luoying.luoojbackendmodel.entity.Question;
import com.luoying.luoojbackendmodel.entity.QuestionSubmit;
import com.luoying.luoojbackendmodel.entity.User;
import com.luoying.luoojbackendmodel.enums.QuestionSubmitLanguageEnum;
import com.luoying.luoojbackendmodel.vo.QuestionSubmitVO;
import com.luoying.luoojbackendmodel.vo.QuestionVO;
import com.luoying.luoojbackendquestionservice.mapper.QuestionMapper;
import com.luoying.luoojbackendquestionservice.mapper.QuestionSubmitMapper;
import com.luoying.luoojbackendquestionservice.service.QuestionService;
import com.luoying.luoojbackendquestionservice.service.QuestionSubmitService;
import com.luoying.luoojbackendserviceclient.service.JudgeFeignClient;
import com.luoying.luoojbackendserviceclient.service.UserFeighClient;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RRateLimiter;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author 落樱的悔恨
 * 题目接口
 */
@RestController
@RequestMapping("/")
@Slf4j
public class QuestionController {
    @Resource
    private QuestionSubmitService questionSubmitService;
    @Resource
    private QuestionService questionService;

    @Resource
    private UserFeighClient userFeignClient;

    @Resource
    private RRateLimiter rateLimiter;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private QuestionSubmitMapper questionSubmitMapper;

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    private JudgeFeignClient judgeFeignClient;

    private final static Gson GSON = new Gson();

    static {
        initFlowRules();
    }

    // region 增删改查

    /**
     * 创建题目（仅管理员）
     *
     * @param questionAddRequest 创建题目请求
     * @param request            {@link HttpServletRequest}
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addQuestion(@RequestBody QuestionAddRequest questionAddRequest, HttpServletRequest request) {
        // 判空
        if (questionAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 拷贝
        Question question = new Question();
        BeanUtils.copyProperties(questionAddRequest, question);
        // 获取题目标签
        List<String> tags = questionAddRequest.getTags();
        if (tags != null) {
            // 转Json字符串
            question.setTags(GSON.toJson(tags));
        }
        // 获取判题用例
        List<QuestionJudgeCase> judgeCaseList = questionAddRequest.getJudgeCaseList();
        if (judgeCaseList != null) {
            // 转Json字符串
            question.setJudgeCase(GSON.toJson(judgeCaseList));
        }
        // 获取判题配置
        QuestionJudgeCconfig judgeConfig = questionAddRequest.getJudgeConfig();
        if (judgeConfig != null) {
            // 转Json字符串
            question.setJudgeConfig(GSON.toJson(judgeConfig));
        }
        // 校验参数
        questionService.validQuestion(question, true);
        // 获取登录用户
        User loginUser = userFeignClient.getLoginUser(request);
        // 设置参数
        question.setUserId(loginUser.getId());
        question.setFavourNum(0);
        question.setThumbNum(0);
        // 保存
        boolean result = questionService.save(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        long newQuestionId = question.getId();
        // 返回
        return ResultUtils.success(newQuestionId);
    }

    /**
     * 删除题目（仅管理员）
     *
     * @param deleteRequest 删除请求
     * @param request       {@link HttpServletRequest}
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteQuestion(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        // 校验
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断题目是否存在
        long id = deleteRequest.getId();
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 删除
        boolean b = questionService.removeById(id);
        return ResultUtils.success(b);
    }

    /**
     * 更新题目（仅管理员）
     *
     * @param questionUpdateRequest 题目更新请求
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateQuestion(@RequestBody QuestionUpdateRequest questionUpdateRequest) {
        // 校验
        if (questionUpdateRequest == null || questionUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return ResultUtils.success(questionService.update(questionUpdateRequest));
    }

    /**
     * 根据 id 获取题目（仅管理员）
     *
     * @param id 题目id
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Question> getQuestionById(long id, HttpServletRequest request) {
        // 校验
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 查询
        Question question = questionService.getById(id);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 返回
        return ResultUtils.success(question);
    }

    /**
     * 根据 id 获取封装后的题目
     *
     * @param id 题目id
     */
    @GetMapping("/get/vo")
    public BaseResponse<QuestionVO> getQuestionVOById(@RequestParam("id") long id, HttpServletRequest request) {
        // 校验
        if (id <= 0 || (id + "").length() != 19) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 查询
        Question question = questionService.queryById(id);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 封装返回
        return ResultUtils.success(questionService.getQuestionVO(question, request));
    }

    /**
     * 分页获取题目列表（仅管理员）
     *
     * @param questionQueryRequest 题目查询请求
     * @param request              {@link HttpServletRequest}
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Question>> listQuestionByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                           HttpServletRequest request) {
        // 获取分页参数
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 查询
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        // 返回
        return ResultUtils.success(questionPage);
    }

    /**
     * 分页获取题目列表（封装类）
     *
     * @param questionQueryRequest 题目查询请求
     * @param request              {@link HttpServletRequest}
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                               HttpServletRequest request) {
        // 获取分页参数
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 50, ErrorCode.PARAMS_ERROR);
        // 查询
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        // 返回
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }

    /**
     * 分页获取当前用户创建的题目列表
     *
     * @param questionQueryRequest 题目查询请求
     * @param request              {@link HttpServletRequest}
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<QuestionVO>> listMyQuestionVOByPage(@RequestBody QuestionQueryRequest questionQueryRequest,
                                                                 HttpServletRequest request) {
        // 判空
        if (questionQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取当前登录用户
        User loginUser = userFeignClient.getLoginUser(request);
        // 设置参数
        questionQueryRequest.setUserId(loginUser.getId());
        // 获取分页参数
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 50, ErrorCode.PARAMS_ERROR);
        // 查询
        Page<Question> questionPage = questionService.page(new Page<>(current, size),
                questionService.getQueryWrapper(questionQueryRequest));
        return ResultUtils.success(questionService.getQuestionVOPage(questionPage, request));
    }

    // endregion

    /**
     * 更新题目（仅管理员）
     *
     * @param questionEditRequest 题目更新请求
     * @param request             {@link HttpServletRequest}
     */
    @PostMapping("/edit")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> editQuestion(@RequestBody QuestionEditRequest questionEditRequest, HttpServletRequest request) {
        // 判空
        if (questionEditRequest == null || questionEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 拷贝
        Question question = new Question();
        BeanUtils.copyProperties(questionEditRequest, question);
        // 获取题目标签
        List<String> tags = questionEditRequest.getTags();
        if (tags != null) {
            // 转Json字符串
            question.setTags(GSON.toJson(tags));
        }
        // 获取判题用例
        List<QuestionJudgeCase> judgeCaseList = questionEditRequest.getJudgeCaseList();
        if (judgeCaseList != null) {
            // 转Json字符串
            question.setJudgeCase(GSON.toJson(judgeCaseList));
        }
        // 获取判题配置
        QuestionJudgeCconfig judgeConfig = questionEditRequest.getJudgeConfig();
        if (judgeConfig != null) {
            // 转Json字符串
            question.setJudgeConfig(GSON.toJson(judgeConfig));
        }
        // 参数校验
        questionService.validQuestion(question, false);
        // 判断题目是否存在
        long id = questionEditRequest.getId();
        Question oldQuestion = questionService.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 更新
        boolean result = questionService.updateById(question);
        return ResultUtils.success(result);
    }


    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest 提交题目请求
     * @param request                  {@link HttpServletRequest}
     */
    @PostMapping("/question_submit")
    // @SentinelResource(value = "doQuestionSubmit", blockHandler = "handleException")
    public BaseResponse<Long> doQuestionSubmit(@RequestBody QuestionSubmitAddRequest questionSubmitAddRequest,
                                               HttpServletRequest request) {
        // 获取当前登录用户
        final User loginUser = userFeignClient.getLoginUser(request);
        // 获取单个用户题目提交的key
        String key = RedisKey.getKey(RedisKey.SINGLE_USER_QUESTION_SUBMIT_KEY, loginUser.getId());
        // 判断该用户5秒内是否提交过
        Boolean isExists = stringRedisTemplate.hasKey(key);
        if (isExists) {// 5秒内提交过就不允许获取令牌
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "您的提交次数过于频繁，请稍后再试");
        }
        // 获取令牌
        if (!rateLimiter.tryAcquire()) {
            throw new BusinessException(ErrorCode.API_REQUEST_ERROR, "系统繁忙，请稍后再试");
        }
        // 获取令牌成功，添加单个用户题目提交的key，标记该用户
        stringRedisTemplate.opsForValue().set(key, "");
        // 设置单个用户题目提交的key的过期时间，限制每个用户五秒内只能提交一次
        stringRedisTemplate.expire(key, RedisKey.SINGLE_USER_QUESTION_SUBMIT_KEY_TTL, TimeUnit.SECONDS);
        // 判空
        if (questionSubmitAddRequest == null || questionSubmitAddRequest.getQuestionId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 提交
        long questionSubmitId = questionSubmitService.doQuestionSubmit(questionSubmitAddRequest, loginUser);
        return ResultUtils.success(questionSubmitId);
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
     * Sentinel限流策略
     */
    private static void initFlowRules() {
        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule = new FlowRule();
        rule.setResource("doQuestionSubmit");
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        // Set limit QPS to 1.
        rule.setCount(1);
        rules.add(rule);
        FlowRuleManager.loadRules(rules);
    }


    /**
     * 分页获取题目提交列表
     *
     * @param questionSubmitQueryRequest 题目提交查询请求
     * @param request                    {@link HttpServletRequest}
     */
    @PostMapping("/question_submit/list/page")
    public BaseResponse<Page<QuestionSubmitVO>> listQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
                                                                         HttpServletRequest request) {
        // 获取分页参数
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();
        // 获取当前登录用户
        User loginUser = userFeignClient.getLoginUser(request);
        // 限制爬虫
        ThrowUtils.throwIf(size > 50, ErrorCode.PARAMS_ERROR);
        // 查询
        Page<QuestionSubmit> questionPage = questionSubmitService.page(new Page<>(current, size),
                questionSubmitService.getQueryWrapper(questionSubmitQueryRequest));
        // 返回
        return ResultUtils.success(questionSubmitService.getQuestionSubmitVOPage(questionPage, loginUser));
    }

    /**
     * 分页获取个人提交列表
     *
     * @param questionSubmitQueryRequest 题目提交查询请求
     * @param request                    {@link HttpServletRequest}
     */
    @PostMapping("/question_submit/my/list/page")
    public BaseResponse<Page<QuestionSubmitVO>> listMyQuestionSubmitByPage(@RequestBody QuestionSubmitQueryRequest questionSubmitQueryRequest,
                                                                           HttpServletRequest request) {
        // 获取分页参数
        long current = questionSubmitQueryRequest.getCurrent();
        long size = questionSubmitQueryRequest.getPageSize();
        String language = questionSubmitQueryRequest.getLanguage();
        Long questionId = questionSubmitQueryRequest.getQuestionId();
        // 获取当前登录用户
        User loginUser = userFeignClient.getLoginUser(request);
        // 限制爬虫
        ThrowUtils.throwIf(size > 50, ErrorCode.PARAMS_ERROR);
        // 查询用户在该题目下的所有提交记录
        Long userId = loginUser.getId();
        String tableName = "question_submit_" + userId;
        List<QuestionSubmit> questionSubmitList = questionSubmitMapper.queryQuestionSubmitList(tableName, size, (current - 1) * size, language, questionId);
        // 获取用户在该题目下的提交次数
        long total = questionSubmitMapper.countQuestionSubmitAll(tableName, questionId);
        // 组装
        Page<QuestionSubmit> questionPage = new Page<>();
        questionPage.setRecords(questionSubmitList);
        questionPage.setCurrent(current);
        questionPage.setSize(size);
        questionPage.setTotal(total);
        // 返回
        return ResultUtils.success(questionSubmitService.getQuestionSubmitVOPage(questionPage, loginUser));
    }

    /**
     * 获取支持的编程语言
     */
    @GetMapping("/get/language")
    public BaseResponse<List<String>> getCodeLanguage() {
        return ResultUtils.success(QuestionSubmitLanguageEnum.getValues());
    }

    /**
     * 在线运行代码
     *
     * @param runCodeRequest 运行代码请求
     */
    @PostMapping("/run/online")
    public BaseResponse<RunCodeResponse> questionRunOnline(@RequestBody RunCodeRequest runCodeRequest) {
        // 在线运行代码只需要提供一个输入用例，但执行代码请求需要输入用例以集合的方式传入，所以需要转换为集合，但集合只有一个元素
        List<String> inputList = Arrays.asList(runCodeRequest.getInput());
        // 构造执行代码请求
        ExecuteCodeRequest executeCodeRequest =
                ExecuteCodeRequest.builder()
                        .code(runCodeRequest.getCode())
                        .language(runCodeRequest.getLanguage())
                        .inputList(inputList)
                        .build();
        // 运行
        ExecuteCodeResponse executeCodeResponse = judgeFeignClient.runOnline(executeCodeRequest);
        // 获取输出
        List<String> outputList = Optional.ofNullable(executeCodeResponse.getOutputList()).orElse(Arrays.asList(""));
        // 构造运行代码响应
        RunCodeResponse runCodeResponse = RunCodeResponse.builder()
                .output(outputList.get(0))
                .message(executeCodeResponse.getMessage())
                .status(executeCodeResponse.getStatus())
                .judgeInfo(executeCodeResponse.getJudgeInfo())
                .build();
        // 返回
        return ResultUtils.success(runCodeResponse);
    }

    /**
     * 获取上一道题目
     *
     * @param questionId 当前题目id
     */
    @GetMapping("/get/questionId/previous")
    public BaseResponse<Long> getPrevQuestion(@RequestParam("questionId") long questionId) {
        String tableName = "question";
        return ResultUtils.success(questionMapper.getPrevQuestion(tableName, questionId));
    }

    /**
     * 获取下一道题目
     *
     * @param questionId 当前题目id
     */
    @GetMapping("/get/questionId/next")
    public BaseResponse<Long> getNextQuestion(@RequestParam("questionId") long questionId) {
        String tableName = "question";
        return ResultUtils.success(questionMapper.getNextQuestion(tableName, questionId));
    }

    /**
     * 随机获取一道题目
     */
    @GetMapping("/get/questionId/random")
    public BaseResponse<Long> getRandomQuestion() {
        List<Question> questionList = questionMapper.selectList(null);
        Random random = new Random();
        int index = random.nextInt(questionList.size());
        Question question = questionList.get(index);
        return ResultUtils.success(question.getId());
    }


}
