package com.luoying.luoojbackendquestionservice.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luoying.luoojbackendcommon.constant.RedisKey;
import com.luoying.luoojbackendmodel.dto.question_submit.QuestionSubmitQueryRequest;
import com.luoying.luoojbackendmodel.entity.AcceptedQuestion;
import com.luoying.luoojbackendmodel.entity.Question;
import com.luoying.luoojbackendmodel.entity.QuestionSubmit;
import com.luoying.luoojbackendmodel.entity.User;
import com.luoying.luoojbackendmodel.vo.AcceptedQuestionDetailVO;
import com.luoying.luoojbackendmodel.vo.QuestionSubmitVO;
import com.luoying.luoojbackendquestionservice.mapper.AcceptedQuestionMapper;
import com.luoying.luoojbackendquestionservice.mapper.QuestionSubmitMapper;
import com.luoying.luoojbackendquestionservice.service.AcceptedQuestionService;
import com.luoying.luoojbackendquestionservice.service.QuestionService;
import com.luoying.luoojbackendquestionservice.service.QuestionSubmitService;
import com.luoying.luoojbackendserviceclient.service.UserFeignClient;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.luoying.luoojbackendcommon.constant.RedisKey.ACCEPTED_QUESTION_RANK_KEY;
import static com.luoying.luoojbackendcommon.constant.RedisKey.ACCEPTED_QUESTION_RANK_KEY_TTL;

/**
 * @author 落樱的悔恨
 * @description 针对表【accepted_question(题目通过表)】的数据库操作Service实现
 * @createDate 2024-09-02 14:54:22
 */
@Service
public class AcceptedQuestionServiceImpl extends ServiceImpl<AcceptedQuestionMapper, AcceptedQuestion> implements AcceptedQuestionService {

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private QuestionService questionService;

    @Resource
    private QuestionSubmitService questionSubmitService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private QuestionSubmitMapper questionSubmitMapper;

    /**
     * 获取用户通过题目的详情
     *
     * @param request
     * @return
     */
    @Override
    public AcceptedQuestionDetailVO getAcceptedQuestionDetail(Long userId, HttpServletRequest request) {
        AcceptedQuestionDetailVO acceptedQuestionDetailVO = new AcceptedQuestionDetailVO();
        // 2.填充通过题目总数
        LambdaQueryWrapper<AcceptedQuestion> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(Objects.nonNull(userId), AcceptedQuestion::getUserId, userId);
        List<AcceptedQuestion> acceptedQuestionList = this.list(queryWrapper1);
        acceptedQuestionDetailVO.setPassTotalNum(acceptedQuestionList.size());
        // 3.填充各难度的通过数
        List<Long> questionIds = acceptedQuestionList.stream().map(acceptedQuestion -> acceptedQuestion.getQuestionId()).collect(Collectors.toList());
        HashMap<Integer, Integer> eachDifficultyPassNum = new HashMap<>();
        eachDifficultyPassNum.put(0, 0);
        eachDifficultyPassNum.put(1, 0);
        eachDifficultyPassNum.put(2, 0);
        if (CollectionUtil.isNotEmpty(questionIds)) {// listByIds不接受空集合
            List<Question> questionList1 = questionService.listByIds(questionIds);
            Map<Integer, List<Question>> difficultyMap1 = questionList1.stream().collect(Collectors.groupingBy(Question::getDifficulty));
            for (Integer difficulty : difficultyMap1.keySet()) {
                eachDifficultyPassNum.put(difficulty, difficultyMap1.get(difficulty).size());
            }
            acceptedQuestionDetailVO.setEachDifficultyPassNum(eachDifficultyPassNum);
        } else {
            acceptedQuestionDetailVO.setEachDifficultyPassNum(eachDifficultyPassNum);
        }
        // 4.填充题目总数
        List<Question> questionList2 = questionService.list();
        acceptedQuestionDetailVO.setQuestionTotalNum(questionList2.size());
        // 5.填充各难度的题目数
        Map<Integer, List<Question>> difficultyMap2 = questionList2.stream().collect(Collectors.groupingBy(Question::getDifficulty));
        HashMap<Integer, Integer> eachDifficultyQuestionNum = new HashMap<>();
        eachDifficultyQuestionNum.put(0, 0);
        eachDifficultyQuestionNum.put(1, 0);
        eachDifficultyQuestionNum.put(2, 0);
        for (Integer difficulty : difficultyMap2.keySet()) {
            eachDifficultyQuestionNum.put(difficulty, difficultyMap2.get(difficulty).size());
        }
        acceptedQuestionDetailVO.setEachDifficultyQuestionNum(eachDifficultyQuestionNum);
        // 6.填充总提交通过率
        QuestionSubmitQueryRequest questionSubmitQueryRequest = new QuestionSubmitQueryRequest();
        questionSubmitQueryRequest.setUserId(userId);
        Page<QuestionSubmit> questionSubmitPage = questionSubmitMapper.selectPage(new Page<>(1, Long.MAX_VALUE), questionSubmitQueryRequest);
        // 6.1 总提交数
        List<QuestionSubmitVO> questionSubmitVOList = questionSubmitPage.getRecords().stream().map(questionSubmit -> QuestionSubmitVO.objToVo(questionSubmit)).collect(Collectors.toList());
        // 6.2 总提交通过数
        List<QuestionSubmitVO> acceptedQuestionSubmitVOList = questionSubmitVOList.stream().filter(questionSubmit -> "Accepted".equals(questionSubmit.getJudgeInfo().getMessage())).collect(Collectors.toList());
        if (questionSubmitVOList.size() != 0) {// 防止除数为0
            acceptedQuestionDetailVO.setSubmissionPassRate(acceptedQuestionSubmitVOList.size() * 1.0 / questionSubmitVOList.size());
        } else {
            acceptedQuestionDetailVO.setSubmissionPassRate(0.0);
        }
        // 7.填充各难度的提交通过率
        HashMap<Integer, Double> eachDifficultySubmitPassRate = new HashMap<>();
        eachDifficultySubmitPassRate.put(0, 0.0);
        eachDifficultySubmitPassRate.put(1, 0.0);
        eachDifficultySubmitPassRate.put(2, 0.0);
        // 统计各难度题目的提交总数
        HashMap<Integer, Integer> eachDifficultySubmitNum = new HashMap<>();
        eachDifficultySubmitNum.put(0, 0);
        eachDifficultySubmitNum.put(1, 0);
        eachDifficultySubmitNum.put(2, 0);
        for (QuestionSubmitVO questionSubmitVO : questionSubmitVOList) {
            Question question = questionService.getById(questionSubmitVO.getQuestionId());
            Integer difficulty = question.getDifficulty();
            eachDifficultySubmitNum.put(difficulty, eachDifficultySubmitNum.getOrDefault(difficulty, 0) + 1);
        }
        // 统计各难度题目的提交通过数
        HashMap<Integer, Integer> eachDifficultySubmitAcceptedNum = new HashMap<>();
        eachDifficultySubmitAcceptedNum.put(0, 0);
        eachDifficultySubmitAcceptedNum.put(1, 0);
        eachDifficultySubmitAcceptedNum.put(2, 0);
        for (QuestionSubmitVO questionSubmitVO : acceptedQuestionSubmitVOList) {
            Question question = questionService.getById(questionSubmitVO.getQuestionId());
            Integer difficulty = question.getDifficulty();
            eachDifficultySubmitAcceptedNum.put(difficulty, eachDifficultySubmitAcceptedNum.getOrDefault(difficulty, 0) + 1);
        }
        for (Integer difficulty : eachDifficultySubmitAcceptedNum.keySet()) {
            if (eachDifficultySubmitNum.get(difficulty) != 0) { // 防止除数为0
                eachDifficultySubmitPassRate.put(difficulty, eachDifficultySubmitAcceptedNum.get(difficulty) * 1.0 / eachDifficultySubmitNum.get(difficulty));
            } else {
                eachDifficultySubmitPassRate.put(difficulty, 0.0);
            }
        }
        // 统计的难度题目的通过率
        acceptedQuestionDetailVO.setEachDifficultysubmissionPassRate(eachDifficultySubmitPassRate);
        return acceptedQuestionDetailVO;
    }

    /**
     * 获取用户的排名（通过题目数量）
     *
     * @param request
     * @return
     */
    @Override
    public Long getAcceptedQuestionRanking(HttpServletRequest request) {
        User loginUser = userFeignClient.getLoginUser(request);
        // 查询缓存
        Long userId = loginUser.getId();
        String key = RedisKey.getKey(ACCEPTED_QUESTION_RANK_KEY);
        Long rank = stringRedisTemplate.opsForZSet().rank(key, userId.toString());
        if (rank != null) return rank + 1;
        // 未查到，构建缓存
        LambdaQueryWrapper<AcceptedQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AcceptedQuestion::getUserId, userId);
        List<AcceptedQuestion> acceptedQuestionList = this.list(queryWrapper);
        stringRedisTemplate.opsForZSet().add(key, userId.toString(), acceptedQuestionList.size() * 1.0);
        stringRedisTemplate.expire(key, ACCEPTED_QUESTION_RANK_KEY_TTL, TimeUnit.MINUTES);
        rank = stringRedisTemplate.opsForZSet().rank(key, userId.toString());
        return rank + 1;
    }

    /**
     * 判断当前用户是否通过了某道题目
     *
     * @param questionId
     * @param request
     * @return
     */
    @Override
    public Boolean isAccepted(Long questionId, HttpServletRequest request) {
        // 获取登录用户
        User loginUser = userFeignClient.getLoginUser(request);
        // 构造条件
        LambdaQueryWrapper<AcceptedQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AcceptedQuestion::getQuestionId, questionId);
        queryWrapper.eq(AcceptedQuestion::getUserId, loginUser.getId());
        // 查询
        return this.getOne(queryWrapper) != null;
    }
}




