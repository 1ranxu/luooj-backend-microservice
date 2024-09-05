package com.luoying.luoojbackendquestionservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luoying.luoojbackendcommon.common.ErrorCode;
import com.luoying.luoojbackendcommon.constant.CommonConstant;
import com.luoying.luoojbackendcommon.exception.BusinessException;
import com.luoying.luoojbackendcommon.utils.SqlUtils;
import com.luoying.luoojbackendmodel.dto.questionsubmit.QuestionSubmitAddRequest;
import com.luoying.luoojbackendmodel.dto.questionsubmit.QuestionSubmitDetail;
import com.luoying.luoojbackendmodel.dto.questionsubmit.QuestionSubmitQueryRequest;
import com.luoying.luoojbackendmodel.entity.Question;
import com.luoying.luoojbackendmodel.entity.QuestionSubmit;
import com.luoying.luoojbackendmodel.entity.User;
import com.luoying.luoojbackendmodel.enums.QuestionSubmitLanguageEnum;
import com.luoying.luoojbackendmodel.enums.QuestionSubmitStatusEnum;
import com.luoying.luoojbackendmodel.vo.QuestionSubmitVO;
import com.luoying.luoojbackendmodel.vo.QuestionVO;
import com.luoying.luoojbackendquestionservice.mapper.QuestionSubmitMapper;
import com.luoying.luoojbackendquestionservice.rabbitmq.MessageProducer;
import com.luoying.luoojbackendquestionservice.service.QuestionService;
import com.luoying.luoojbackendquestionservice.service.QuestionSubmitService;
import com.luoying.luoojbackendserviceclient.service.JudgeFeignClient;
import com.luoying.luoojbackendserviceclient.service.UserFeighClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 落樱的悔恨
 * @description 针对表【question_submit(题目提交记录)】的数据库操作Service实现
 * @createDate 2023-11-09 16:32:34
 */
@Service
public class QuestionSubmitServiceImpl extends ServiceImpl<QuestionSubmitMapper, QuestionSubmit>
        implements QuestionSubmitService {
    private static final String EXCHANGE_NAME = "oj_exchange";
    private static final String ROUTING_KEY = "oj_routingKey";
    @Resource
    private QuestionService questionService;

    @Resource
    private UserFeighClient userFeighClient;

    @Resource
    @Lazy
    private JudgeFeignClient judgeFeignClient;

    @Resource
    private MessageProducer messageProducer;


    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest 题目提交创建请求
     * @param loginUser                登录用户
     * @return 提交记录id
     */
    @Override
    public long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser) {
        // 校验编程语言是否合法
        String language = questionSubmitAddRequest.getLanguage();
        QuestionSubmitLanguageEnum languageEnum = QuestionSubmitLanguageEnum.getEnumByValue(language);
        if (languageEnum == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编程语言错误");
        }
        // 判断题目是否存在
        long questionId = questionSubmitAddRequest.getQuestionId();
        Question question = questionService.getById(questionId);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 设置提交数
        Question updateQuestion = new Question();
        synchronized (this) {// 串行设置
            Integer submitNum = question.getSubmitNum();
            submitNum = submitNum + 1;
            updateQuestion.setId(questionId);
            updateQuestion.setSubmitNum(submitNum);
            boolean save = questionService.updateById(updateQuestion);
            if (!save) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "数据保存失败");
            }
        }
        // 获取用户id
        long userId = loginUser.getId();
        // 锁必须要包裹住事务方法
        // 构造题目提交
        QuestionSubmit questionSubmit = new QuestionSubmit();
        questionSubmit.setUserId(userId);
        questionSubmit.setQuestionId(questionId);
        questionSubmit.setLanguage(language);
        questionSubmit.setCode(questionSubmitAddRequest.getCode());
        // 设置初始状态
        questionSubmit.setStatus(QuestionSubmitStatusEnum.WAITING.getValue());
        questionSubmit.setJudgeInfo("{}");
        // 保存到提交记录表
        boolean result = this.save(questionSubmit);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "题目提交失败");
        }

        // 发送提交记录id到消息队列
        if (result) {
            messageProducer.sendMessage(EXCHANGE_NAME, ROUTING_KEY, String.valueOf(questionSubmit.getId()));
        }

        // 执行判题服务
        /*CompletableFuture.runAsync(() -> {

            judgeService.doJudge(questionSubmit.getId());
        });*/

        return questionSubmit.getQuestionId();
    }

    /**
     * 获取查询条件
     *
     * @param questionSubmitQueryRequest 题目提交查询请求
     */
    @Override
    public QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest) {
        QueryWrapper<QuestionSubmit> queryWrapper = new QueryWrapper<>();
        // 判空
        if (questionSubmitQueryRequest == null) {
            return queryWrapper;
        }
        // 获取参数
        String language = questionSubmitQueryRequest.getLanguage();
        Long questionId = questionSubmitQueryRequest.getQuestionId();
        Long userId = questionSubmitQueryRequest.getUserId();
        Integer status = questionSubmitQueryRequest.getStatus();
        String sortField = questionSubmitQueryRequest.getSortField();
        String sortOrder = questionSubmitQueryRequest.getSortOrder();

        // 拼接查询条件
        // 根据用户id，题目id、编程语言，题目状态去查询提交记录
        queryWrapper.like(QuestionSubmitLanguageEnum.getEnumByValue(language) != null, "language", language);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(QuestionSubmitStatusEnum.getEnumByValue(status) != null, "status", status);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }

    /**
     * 获取封装后的题目提交
     *
     * @param questionSubmit 题目提交
     * @param loginUser      登录用户
     */
    @Override
    public QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser) {
        QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
        // 脱敏
        // 仅本人能和管理员能看见提交记录的代码
        if (loginUser.getId() != questionSubmit.getUserId() && !userFeighClient.isAdmin(loginUser)) {
            questionSubmitVO.setCode(null);
        }
        return questionSubmitVO;
    }

    /**
     * 分页获取封装后的题目提交
     *
     * @param questionSubmitPage {@link Page<QuestionSubmit>}
     * @param loginUser          登录用户
     */
    @Override
    public Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage,
                                                          User loginUser) {
        // 获取题目提交集合
        List<QuestionSubmit> questionSubmitList = questionSubmitPage.getRecords();
        Page<QuestionSubmitVO> questionSubmitVOPage = new Page<>(questionSubmitPage.getCurrent(), questionSubmitPage.getSize(), questionSubmitPage.getTotal());
        // 判空
        if (CollectionUtils.isEmpty(questionSubmitList)) {
            return questionSubmitVOPage;
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = questionSubmitList.stream().map(QuestionSubmit::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userFeighClient.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 关联查询题目信息
        Set<Long> questionIdSet = questionSubmitList.stream().map(QuestionSubmit::getQuestionId).collect(Collectors.toSet());
        Map<Long, List<Question>> questionIdUserListMap = questionService.listByIds(questionIdSet).stream()
                .collect(Collectors.groupingBy(Question::getId));
        // 3. 填充信息
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitList.stream().map(questionSubmit -> {
            QuestionSubmitVO questionSubmitVO = QuestionSubmitVO.objToVo(questionSubmit);
            Long userId = questionSubmit.getUserId();
            Long questionId = questionSubmit.getQuestionId();
            User user = null;
            Question question = null;
            // 填充用户信息
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            // 填充题目信息
            if (questionIdUserListMap.containsKey(questionId)) {
                question = questionIdUserListMap.get(questionId).get(0);
            }
            questionSubmitVO.setUserVO(userFeighClient.getUserVO(user));
            questionSubmitVO.setQuestionVO(QuestionVO.objToVo(question));
            return questionSubmitVO;
        }).collect(Collectors.toList());
        questionSubmitVOPage.setRecords(questionSubmitVOList);
        return questionSubmitVOPage;
    }

    /**
     * 获取个人提交详情
     * @param request
     * @return
     */
    @Override
    public QuestionSubmitDetail getPersonSubmitDetail(HttpServletRequest request) {
        QuestionSubmitDetail questionSubmitDetail = new QuestionSubmitDetail();
        // 1.获取登录用户id
        User loginUser = userFeighClient.getLoginUser(request);
        // 2.根据年份分组
        LambdaQueryWrapper<QuestionSubmit> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionSubmit::getUserId, loginUser.getId());
        wrapper.select(QuestionSubmit::getYear, QuestionSubmit::getCount);
        wrapper.groupBy(QuestionSubmit::getYear);
        List<QuestionSubmit> questionSubmitList = this.list(wrapper);
        HashMap<String, Integer> years = new HashMap<>();
        for (QuestionSubmit questionSubmit : questionSubmitList) {
            years.put(questionSubmit.getYear().toString(), questionSubmit.getCount());
        }
        questionSubmitDetail.setYears(years);
        // 3.根据日分组
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionSubmit::getUserId, loginUser.getId());
        wrapper.select(QuestionSubmit::getDay, QuestionSubmit::getCount);
        wrapper.groupBy(QuestionSubmit::getDay);
        questionSubmitList = this.list(wrapper);
        HashMap<String, Integer> submitDetail = new HashMap<>();
        SimpleDateFormat originalFormat = new SimpleDateFormat("yyyyMMdd");
        SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd");
        for (QuestionSubmit questionSubmit : questionSubmitList) {
            String formattedDateStr = null;
            try {
                Date date = originalFormat.parse(questionSubmit.getDay().toString());
                formattedDateStr = targetFormat.format(date);
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
            submitDetail.put(formattedDateStr, questionSubmit.getCount());
        }
        questionSubmitDetail.setSubmitDetail(submitDetail);
        // 4.查询一年提交了多少天
        wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(QuestionSubmit::getUserId, loginUser.getId());
        wrapper.select(QuestionSubmit::getYear, QuestionSubmit::getCountDay);
        wrapper.groupBy(QuestionSubmit::getYear);
        questionSubmitList = this.list(wrapper);
        HashMap<String, Integer> dayNum = new HashMap<>();
        for (QuestionSubmit questionSubmit : questionSubmitList) {
            dayNum.put(questionSubmit.getYear().toString(), questionSubmit.getCountDay());
        }
        questionSubmitDetail.setDayNum(dayNum);
        return questionSubmitDetail;
    }
}