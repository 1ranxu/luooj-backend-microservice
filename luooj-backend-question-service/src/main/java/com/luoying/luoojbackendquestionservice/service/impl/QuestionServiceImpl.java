package com.luoying.luoojbackendquestionservice.service.impl;

import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.luoying.luoojbackendcommon.common.ErrorCode;
import com.luoying.luoojbackendcommon.constant.CommonConstant;
import com.luoying.luoojbackendcommon.constant.RedisKey;
import com.luoying.luoojbackendcommon.exception.BusinessException;
import com.luoying.luoojbackendcommon.exception.ThrowUtils;
import com.luoying.luoojbackendcommon.utils.RedisData;
import com.luoying.luoojbackendcommon.utils.SqlUtils;
import com.luoying.luoojbackendmodel.dto.question.QuestionJudgeCase;
import com.luoying.luoojbackendmodel.dto.question.QuestionJudgeCconfig;
import com.luoying.luoojbackendmodel.dto.question.QuestionQueryRequest;
import com.luoying.luoojbackendmodel.dto.question.QuestionUpdateRequest;
import com.luoying.luoojbackendmodel.entity.AcceptedQuestion;
import com.luoying.luoojbackendmodel.entity.Question;
import com.luoying.luoojbackendmodel.entity.User;
import com.luoying.luoojbackendmodel.vo.QuestionVO;
import com.luoying.luoojbackendmodel.vo.UserVO;
import com.luoying.luoojbackendquestionservice.mapper.QuestionMapper;
import com.luoying.luoojbackendquestionservice.service.AcceptedQuestionService;
import com.luoying.luoojbackendquestionservice.service.QuestionService;
import com.luoying.luoojbackendserviceclient.service.UserFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.luoying.luoojbackendcommon.constant.RedisKey.*;

/**
 * @author 落樱的悔恨
 * @description 针对表【question(题目)】的数据库操作Service实现
 * @createDate 2023-11-09 16:32:34
 */
@Service
@Slf4j
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question> implements QuestionService {
    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private AcceptedQuestionService acceptedQuestionService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private final static Gson GSON = new Gson();

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * 校验参数
     *
     * @param question 题目
     * @param add      是否为新增
     */
    @Override
    public void validQuestion(Question question, boolean add) {
        // 判空
        if (question == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取参数
        String title = question.getTitle();
        String content = question.getContent();
        String tags = question.getTags();
        String answer = question.getAnswer();
        String judgeConfig = question.getJudgeConfig();
        String judgeCase = question.getJudgeCase();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(title, content, tags), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(title) && title.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        if (StringUtils.isNotBlank(content) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
        }
        if (StringUtils.isNotBlank(answer) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "答案过长");
        }
        if (StringUtils.isNotBlank(judgeCase) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "判题用例过长");
        }
        if (StringUtils.isNotBlank(judgeConfig) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "判题配置过长");
        }
    }

    /**
     * 获取查询条件
     *
     * @param questionQueryRequest 题目查询请求
     */
    @Override
    public QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest) {
        QueryWrapper<Question> queryWrapper = new QueryWrapper<>();
        // 判空
        if (questionQueryRequest == null) {
            return queryWrapper;
        }
        // 获取参数
        Long id = questionQueryRequest.getId();
        String title = questionQueryRequest.getTitle();
        String content = questionQueryRequest.getContent();
        List<String> tags = questionQueryRequest.getTags();
        Long userId = questionQueryRequest.getUserId();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        if (CollectionUtils.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        // 返回
        return queryWrapper;
    }

    /**
     * 获取封装后的题目
     *
     * @param question 题目
     * @param request  {@link HttpServletRequest}
     */
    @Override
    public QuestionVO getQuestionVO(Question question, HttpServletRequest request) {
        QuestionVO questionVO = QuestionVO.objToVo(question);
        // 1. 关联查询创建人信息
        Long userId = question.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userFeignClient.getById(userId);
        }
        UserVO userVO = userFeignClient.getUserVO(user);
        // 设置创建人消息
        questionVO.setUserVO(userVO);
        // 返回
        return questionVO;
    }

    /**
     * 分页获取封装后的题目
     *
     * @param questionPage {@link Page<Question>}
     * @param request      {@link HttpServletRequest}
     */
    @Override
    public Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request) {
        // 获取题目集合
        List<Question> questionList = questionPage.getRecords();
        Page<QuestionVO> questionVOPage = new Page<>(questionPage.getCurrent(), questionPage.getSize(), questionPage.getTotal());
        // 判空
        if (CollectionUtils.isEmpty(questionList)) {
            return questionVOPage;
        }
        // 1. 题目关联查询用户信息
        Set<Long> userIdSet = questionList.stream().map(Question::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userFeignClient.listByIds(userIdSet).stream().collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        List<QuestionVO> questionVOList = questionList.stream().map(question -> {
            QuestionVO questionVO = QuestionVO.objToVo(question);
            // 填充创建用户信息
            Long userId = question.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionVO.setUserVO(userFeignClient.getUserVO(user));
            return questionVO;
        }).collect(Collectors.toList());

        // 4. 获取当前登录用户id
        try {
            User loginUser = userFeignClient.getLoginUser(request);
            if (loginUser != null) {
                Long id = loginUser.getId();
                // 5. 查询该用户通过的题目，获取所有通过题目的id集合
                LambdaQueryWrapper<AcceptedQuestion> queryWrapper = new LambdaQueryWrapper<>();
                queryWrapper.eq(AcceptedQuestion::getUserId,id);
                List<AcceptedQuestion> acceptedQuestionList = acceptedQuestionService.list(queryWrapper);
                Set<Long> acceptedQuestionIdSet = acceptedQuestionList.stream().map(AcceptedQuestion::getQuestionId).collect(Collectors.toSet());
                questionVOList = questionVOList.stream().map(questionVO -> {
                    // 填充是否通过信息
                    if (acceptedQuestionIdSet.contains(questionVO.getId())) {
                        // 0 代表通过
                        questionVO.setIsAccepted(0);
                    } else {
                        // 1 代表未通过
                        questionVO.setIsAccepted(1);
                    }
                    return questionVO;
                }).collect(Collectors.toList());
            }
        } catch (Exception e) {
            questionVOList = questionVOList.stream().map(questionVO -> {
                // 1 代表未通过
                questionVO.setIsAccepted(1);
                return questionVO;
            }).collect(Collectors.toList());
        }
        questionVOPage.setRecords(questionVOList);
        return questionVOPage;
    }

    /**
     * 根据题目id查询题目
     *
     * @param id 题目id
     */
    @Override
    public Question queryById(long id) {
        // 1. 从redis查询缓存
        String json = stringRedisTemplate.opsForValue().get(RedisKey.getKey(RedisKey.SINGLE_QUESTION_KEY, id));
        // 2. 判断缓存是否存在
        if (StringUtils.isNotBlank(json)) { // 3. 缓存存在
            // 判断缓存是否过期
            RedisData redisData = JSONUtil.toBean(json, RedisData.class);
            Question question = JSONUtil.toBean((JSONObject) redisData.getData(), Question.class);
            LocalDateTime expireTime = redisData.getExpireTime();
            if (LocalDateTime.now().isBefore(expireTime)) { // 未过期，直接返回
                return question;
            } else { // 过期，缓存重建
                // 获取互斥锁
                boolean isLock = tryLock(RedisKey.getKey(LOCK_QUESTION_KEY, id), LOCK_QUESTION_KEY_TTL);
                if (!isLock) { // 获取锁失败，直接返回旧缓存
                    return question;
                }
                // 获取锁成功，开辟新线程，实现缓存重建
                CompletableFuture.runAsync(() -> {
                    log.info("缓存重建任务执行中" + ",执行人：" + Thread.currentThread().getName());
                    try {
                        Question tempQuestion = getById(id);
                        RedisData tempRedisData = new RedisData();
                        tempRedisData.setData(tempQuestion);
                        // 逻辑过期
                        tempRedisData.setExpireTime(LocalDateTime.now().plusMinutes(SINGLE_QUESTION_KEY_TTL + RandomUtil.randomLong(0, 5)));
                        // 数据库中存在，写入redis缓存
                        stringRedisTemplate.opsForValue().set(RedisKey.getKey(RedisKey.SINGLE_QUESTION_KEY, id), JSONUtil.toJsonStr(tempRedisData));
                    } finally {
                        // 释放锁
                        unLock(RedisKey.getKey(LOCK_QUESTION_KEY, id));
                    }
                }, threadPoolExecutor);
                // 本线程返回旧缓存
                return question;
            }
        }

        if(json != null){ // 返回空值缓存
            return null;
        }

        // 4. 缓存不存在，根据id查询数据库
        Question question = getById(id);
        if (question == null) { // 5. 数据库中不存在，返回null
            // 缓存空值 一种缓存穿透解决方案
            stringRedisTemplate.opsForValue().set(RedisKey.getKey(RedisKey.SINGLE_QUESTION_KEY, id), "");
            stringRedisTemplate.expire(RedisKey.getKey(RedisKey.SINGLE_QUESTION_KEY, id), CACHE_NULL_TTL, TimeUnit.MINUTES);
            return null;
        }
        // 5. 数据库中存在，写入redis缓存
        RedisData redisData = new RedisData();
        redisData.setData(question);
        // 逻辑过期
        redisData.setExpireTime(LocalDateTime.now().plusMinutes(SINGLE_QUESTION_KEY_TTL + RandomUtil.randomLong(0, 5)));
        stringRedisTemplate.opsForValue().set(RedisKey.getKey(RedisKey.SINGLE_QUESTION_KEY, id), JSONUtil.toJsonStr(redisData));
        // 6. 返回
        return question;
    }

    /**
     * 获取锁
     */
    private boolean tryLock(String key, long ttl) {
        Boolean result = stringRedisTemplate.opsForValue().setIfAbsent(key, "1", ttl, TimeUnit.SECONDS);
        return BooleanUtil.isTrue(result);
    }

    /**
     * 释放锁
     */
    private void unLock(String key) {
        stringRedisTemplate.delete(key);
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean update(QuestionUpdateRequest questionUpdateRequest) {
        // 拷贝
        Question question = new Question();
        BeanUtils.copyProperties(questionUpdateRequest, question);
        // 获取题目标签
        List<String> tags = questionUpdateRequest.getTags();
        if (tags != null) {
            // 转Json字符串
            question.setTags(GSON.toJson(tags));
        }
        // 获取判题用例
        List<QuestionJudgeCase> judgeCaseList = questionUpdateRequest.getJudgeCaseList();
        if (judgeCaseList != null) {
            // 转Json字符串
            question.setJudgeCase(GSON.toJson(judgeCaseList));
        }
        // 获取判题配置
        QuestionJudgeCconfig judgeConfig = questionUpdateRequest.getJudgeConfig();
        if (judgeConfig != null) {
            // 转Json字符串
            question.setJudgeConfig(GSON.toJson(judgeConfig));
        }
        // 参数校验
        this.validQuestion(question, false);
        // 判断题目是否存在
        long id = questionUpdateRequest.getId();
        Question oldQuestion = this.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 更新
        boolean result = this.updateById(question);
        // 删除缓存
        stringRedisTemplate.delete(RedisKey.getKey(SINGLE_QUESTION_KEY, question.getId()));
        // 返回
        return result;
    }
}




