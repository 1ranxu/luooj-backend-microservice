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
import com.luoying.luoojbackendcommon.common.DeleteRequest;
import com.luoying.luoojbackendcommon.common.ErrorCode;
import com.luoying.luoojbackendcommon.constant.CommonConstant;
import com.luoying.luoojbackendcommon.constant.LikeConstant;
import com.luoying.luoojbackendcommon.constant.RedisKey;
import com.luoying.luoojbackendcommon.exception.BusinessException;
import com.luoying.luoojbackendcommon.exception.ThrowUtils;
import com.luoying.luoojbackendcommon.utils.RedisData;
import com.luoying.luoojbackendcommon.utils.SqlUtils;
import com.luoying.luoojbackendmodel.dto.question.*;
import com.luoying.luoojbackendmodel.entity.*;
import com.luoying.luoojbackendmodel.vo.QuestionVO;
import com.luoying.luoojbackendmodel.vo.UserVO;
import com.luoying.luoojbackendquestionservice.mapper.QuestionMapper;
import com.luoying.luoojbackendquestionservice.service.AcceptedQuestionService;
import com.luoying.luoojbackendquestionservice.service.QuestionCollectService;
import com.luoying.luoojbackendquestionservice.service.QuestionCommentService;
import com.luoying.luoojbackendquestionservice.service.QuestionService;
import com.luoying.luoojbackendserviceclient.service.UserFeignClient;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
    @Lazy
    private AcceptedQuestionService acceptedQuestionService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private QuestionMapper questionMapper;

    @Resource
    @Lazy
    private QuestionCommentService questionCommentService;

    @Resource
    private QuestionCollectService questionCollectService;

    private static final Gson GSON = new Gson();

    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    /**
     * 创建题目（仅管理员）
     *
     * @param questionAddRequest
     * @param request
     * @return
     */
    @Override
    public Long addQuestion(QuestionAddRequest questionAddRequest, HttpServletRequest request) {
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
        this.validQuestion(question, true);
        // 获取登录用户
        User loginUser = userFeignClient.getLoginUser(request);
        // 设置创建人
        question.setUserId(loginUser.getId());
        // 保存
        boolean result = this.save(question);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return question.getId();
    }


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
     * 删除题目（仅管理员）
     *
     * @param deleteRequest
     * @return
     */
    @Override
    public Boolean deleteQuestion(DeleteRequest deleteRequest, HttpServletRequest request) {
        // 校验
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 判断题目是否存在
        long id = deleteRequest.getId();
        Question oldQuestion = this.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 删除该题目下的所有一级评论
        LambdaQueryWrapper<QuestionComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QuestionComment::getQuestionId, deleteRequest.getId());
        queryWrapper.eq(QuestionComment::getParentId, 0L);
        List<QuestionComment> list = questionCommentService.list(queryWrapper);
        for (QuestionComment c : list) {
            DeleteRequest delete = new DeleteRequest();
            delete.setId(c.getId());
            questionCommentService.deleteQuestionComment(delete, request);
        }
        // 删除收藏该题目的记录
        LambdaQueryWrapper<QuestionCollect> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(QuestionCollect::getQuestionId, deleteRequest.getId());
        questionCollectService.remove(queryWrapper1);
        // 删除题目
        boolean isSuccess = this.removeById(id);
        // 删除缓存
        if (isSuccess) {
            String key = RedisKey.getKey(LIKE_LIST_KEY, LikeConstant.QUESTION_SOLUTION_COMMENT, deleteRequest.getId());
            stringRedisTemplate.delete(key);
        }
        return isSuccess;
    }

    /**
     * 点赞题目
     *
     * @param id
     * @param request
     * @return
     */
    @Override
    public Boolean likeQuestion(Long id, HttpServletRequest request) {
        // 获取登录用户
        User loginUser = userFeignClient.getLoginUser(request);
        // 判断当前登录用户是否已经点赞该题目
        String key = RedisKey.getKey(LIKE_LIST_KEY, LikeConstant.QUESTION, id);
        Boolean isMember = stringRedisTemplate.opsForSet().isMember(key, loginUser.getId().toString());
        if (BooleanUtil.isFalse(isMember)) { // 未点赞
            // 数据库点赞数 +1
            boolean isSuccess = update().setSql("likes = likes + 1").eq("id", id).update();
            if (isSuccess) {
                // 保存用户到Redis的set集合
                stringRedisTemplate.opsForSet().add(key, loginUser.getId().toString());
            }
            return isSuccess;
        } else { // 已点赞
            // 数据库点赞数 -1
            boolean isSuccess = update().setSql("likes = likes - 1").eq("id", id).update();
            if (isSuccess) {
                // 把用户从Redis的set集合移除
                stringRedisTemplate.opsForSet().remove(key, loginUser.getId().toString());
            }
            return isSuccess;
        }
    }

    /**
     * 更新题目（仅管理员）
     *
     * @param questionUpdateRequest
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Boolean updateQuestion(QuestionUpdateRequest questionUpdateRequest) {
        // 校验
        if (questionUpdateRequest == null || questionUpdateRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
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

    /**
     * 根据 id 获取题目（仅管理员）
     *
     * @param id
     * @return
     */
    @Override
    public Question getQuestionById(long id) {
        // 校验
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 查询
        Question question = this.getById(id);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return question;
    }

    /**
     * 根据 id 获取封装后的题目
     *
     * @param id
     * @param request
     * @return
     */
    @Override
    public QuestionVO getQuestionVOById(long id, HttpServletRequest request) {
        // 校验
        if (id <= 0 || (id + "").length() != 19) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 查询
        Question question = this.queryById(id);
        if (question == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        return this.getQuestionVO(question, request);
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
     * 分页获取题目列表（仅管理员）
     *
     * @param questionQueryRequest
     * @return
     */
    @Override
    public Page<Question> listQuestionByPage(QuestionQueryRequest questionQueryRequest) {
        // 获取分页参数
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 查询
        return this.page(new Page<>(current, size), this.getQueryWrapper(questionQueryRequest));
    }

    /**
     * 分页获取封装后的题目
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    @Override
    public Page<QuestionVO> getQuestionVOPage(QuestionQueryRequest questionQueryRequest, HttpServletRequest request) {
        if (questionQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取分页参数
        long current = questionQueryRequest.getCurrent();
        long size = questionQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 50, ErrorCode.PARAMS_ERROR);
        // 查询
        Page<Question> questionPage = this.page(new Page<>(current, size), this.getQueryWrapper(questionQueryRequest));
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
                queryWrapper.eq(AcceptedQuestion::getUserId, id);
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
     * 更新题目（仅管理员）
     *
     * @param questionEditRequest
     * @param request
     * @return
     */
    @Override
    public Boolean editQuestion(QuestionEditRequest questionEditRequest, HttpServletRequest request) {
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
        this.validQuestion(question, false);
        // 判断题目是否存在
        long id = questionEditRequest.getId();
        Question oldQuestion = this.getById(id);
        ThrowUtils.throwIf(oldQuestion == null, ErrorCode.NOT_FOUND_ERROR);
        // 更新
        return this.updateById(question);
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

        if (json != null) { // 返回空值缓存
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
     * 获取上一道题目
     *
     * @param questionId
     * @return
     */
    @Override
    public Long getPrevQuestion(long questionId) {
        String tableName = "question";
        return questionMapper.getPrevQuestion(tableName, questionId);
    }

    /**
     * 获取下一道题目
     *
     * @param questionId
     * @return
     */
    @Override
    public Long getNextQuestion(long questionId) {
        String tableName = "question";
        return questionMapper.getNextQuestion(tableName, questionId);
    }

    /**
     * 随机获取一道题目
     *
     * @return
     */
    @Override
    public Long getRandomQuestion() {
        List<Question> questionList = questionMapper.selectList(null);
        Random random = new Random();
        int index = random.nextInt(questionList.size());
        Question question = questionList.get(index);
        return question.getId();
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
}




