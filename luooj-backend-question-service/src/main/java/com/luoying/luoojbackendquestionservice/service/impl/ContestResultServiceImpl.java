package com.luoying.luoojbackendquestionservice.service.impl;

import cn.hutool.core.lang.Pair;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.luoying.luoojbackendcommon.common.ErrorCode;
import com.luoying.luoojbackendcommon.constant.CommonConstant;
import com.luoying.luoojbackendcommon.constant.RedisKey;
import com.luoying.luoojbackendcommon.exception.BusinessException;
import com.luoying.luoojbackendcommon.exception.ThrowUtils;
import com.luoying.luoojbackendcommon.utils.SqlUtils;
import com.luoying.luoojbackendmodel.codesanbox.ExecuteCodeRequest;
import com.luoying.luoojbackendmodel.codesanbox.ExecuteCodeResponse;
import com.luoying.luoojbackendmodel.codesanbox.RunCodeRequest;
import com.luoying.luoojbackendmodel.codesanbox.RunCodeResponse;
import com.luoying.luoojbackendmodel.dto.contest.ContestQuestion;
import com.luoying.luoojbackendmodel.dto.contest_result.ContestQuestionSubmit;
import com.luoying.luoojbackendmodel.dto.contest_result.ContestResultQueryRequest;
import com.luoying.luoojbackendmodel.dto.contest_result.ContestResultQuestionDetail;
import com.luoying.luoojbackendmodel.dto.question_submit.QuestionSubmitJudgeInfo;
import com.luoying.luoojbackendmodel.entity.Contest;
import com.luoying.luoojbackendmodel.entity.ContestResult;
import com.luoying.luoojbackendmodel.entity.User;
import com.luoying.luoojbackendmodel.enums.JudgeInfoMessagenum;
import com.luoying.luoojbackendmodel.vo.ContestRank;
import com.luoying.luoojbackendmodel.vo.ContestRankQuestionDetail;
import com.luoying.luoojbackendmodel.vo.UserVO;
import com.luoying.luoojbackendquestionservice.mapper.ContestResultMapper;
import com.luoying.luoojbackendquestionservice.service.ContestResultService;
import com.luoying.luoojbackendquestionservice.service.ContestService;
import com.luoying.luoojbackendserviceclient.service.JudgeFeignClient;
import com.luoying.luoojbackendserviceclient.service.UserFeignClient;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.luoying.luoojbackendcommon.constant.RedisKey.CONTEST_RANK_KEY;
import static com.luoying.luoojbackendcommon.constant.RedisKey.CONTEST_RANK_KEY_TTL;

/**
 * @author 落樱的悔恨
 * @description 针对表【contest_result(竞赛成绩表)】的数据库操作Service实现
 * @createDate 2024-09-02 14:50:13
 */
@Service
public class ContestResultServiceImpl extends ServiceImpl<ContestResultMapper, ContestResult> implements ContestResultService {
    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private JudgeFeignClient judgeFeignClient;

    @Resource
    private ContestService contestService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private static final Gson GSON = new Gson();

    /**
     * 竞赛题目提交
     *
     * @param contestQuestionSubmit
     * @param request
     * @return
     */
    @Override
    public QuestionSubmitJudgeInfo contestQuestionSubmit(ContestQuestionSubmit contestQuestionSubmit, HttpServletRequest request) {
        // 提交
        QuestionSubmitJudgeInfo questionSubmitJudgeInfo = judgeFeignClient.contestJudge(contestQuestionSubmit);
        // 获取登录用户
        User loginUser = userFeignClient.getLoginUser(request);
        // 获取题目信息
        Contest contest = contestService.getContestById(contestQuestionSubmit.getContestId());
        Map<Integer, ContestQuestion> contestQuestionDetail = GSON.fromJson(contest.getQuestions(), new TypeToken<Map<Integer, ContestQuestion>>() {
        }.getType());
        // 获取提交信息
        Long contestId = contestQuestionSubmit.getContestId();
        Integer order = contestQuestionSubmit.getOrder();
        Long questionId = contestQuestionSubmit.getQuestionId();
        String language = contestQuestionSubmit.getLanguage();
        String code = contestQuestionSubmit.getCode();
        // 获取判题信息
        JudgeInfoMessagenum judgeInfoMessagenum = JudgeInfoMessagenum.getEnumByValue(questionSubmitJudgeInfo.getMessage());
        if (JudgeInfoMessagenum.ACCEPTED.equals(judgeInfoMessagenum)) {// 通过
            // 查看之前是否有提交
            LambdaQueryWrapper<ContestResult> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ContestResult::getContestId, contestId);
            queryWrapper.eq(ContestResult::getApplicantId, loginUser.getId());
            ContestResult contestResult = this.getOne(queryWrapper);
            if (contestResult == null) {// 首次提交
                contestResult = new ContestResult();
                // 创建 resultQuestionDetailMap
                HashMap<Integer, ContestResultQuestionDetail> resultQuestionDetailMap = new HashMap<>();
                // 创建竞赛结果题目详情（resultQuestionDetail）并填充数据
                ContestResultQuestionDetail resultQuestionDetail = new ContestResultQuestionDetail();
                resultQuestionDetail.setQuestionId(questionId);
                resultQuestionDetail.setLanguage(language);
                resultQuestionDetail.setCode(code);
                resultQuestionDetail.setAcceptTime(new Date());
                resultQuestionDetail.setErrorTry(0);
                // 将 resultQuestionDetail 存入 resultQuestionDetailMap
                resultQuestionDetailMap.put(order, resultQuestionDetail);
                // 填充 contestResult
                contestResult.setContestId(contestQuestionSubmit.getContestId());
                contestResult.setApplicantId(loginUser.getId());
                contestResult.setUserName(loginUser.getUserName());
                contestResult.setTotalScore(contestQuestionDetail.get(order).getScore());
                contestResult.setContestDetail(GSON.toJson(resultQuestionDetailMap));
                // 写入数据库
                this.save(contestResult);
            } else { // 非首次提交
                // 获取竞赛结果详情（resultDetail）
                Map<Integer, ContestResultQuestionDetail> resultQuestionDetailMap = GSON.fromJson(contestResult.getContestDetail(), new TypeToken<Map<Integer, ContestResultQuestionDetail>>() {
                }.getType());
                // 获取竞赛结果题目详情（resultQuestionDetail）
                ContestResultQuestionDetail resultQuestionDetail = resultQuestionDetailMap.get(order);
                if (resultQuestionDetail == null) { // 第一次提交该题目
                    // 创建竞赛结果题目详情（resultQuestionDetail）并填充数据
                    resultQuestionDetail = new ContestResultQuestionDetail();
                    resultQuestionDetail.setQuestionId(questionId);
                    resultQuestionDetail.setLanguage(language);
                    resultQuestionDetail.setCode(code);
                    resultQuestionDetail.setAcceptTime(new Date());
                    resultQuestionDetail.setErrorTry(0);
                    // 存入 resultQuestionDetailMap
                    resultQuestionDetailMap.put(order, resultQuestionDetail);
                    // 增加分数
                    contestResult.setTotalScore(contestResult.getTotalScore() + contestQuestionDetail.get(order).getScore());
                    contestResult.setContestDetail(GSON.toJson(resultQuestionDetailMap));
                    // 更新
                    this.updateById(contestResult);
                } else if (resultQuestionDetail.getAcceptTime() == null) {// 之前提交过该题目，但未通过
                    resultQuestionDetail.setLanguage(language);
                    resultQuestionDetail.setCode(code);
                    resultQuestionDetail.setAcceptTime(new Date());
                    // 存入 resultQuestionDetailMap
                    resultQuestionDetailMap.put(order, resultQuestionDetail);
                    // 增加分数
                    contestResult.setTotalScore(contestResult.getTotalScore() + contestQuestionDetail.get(order).getScore());
                    contestResult.setContestDetail(GSON.toJson(resultQuestionDetailMap));
                    // 更新
                    this.updateById(contestResult);
                }
            }
        } else if (JudgeInfoMessagenum.WRONG_ANSWER.equals(judgeInfoMessagenum)) { // 未通过
            // 查看之前是否有提交
            LambdaQueryWrapper<ContestResult> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ContestResult::getContestId, contestId);
            queryWrapper.eq(ContestResult::getApplicantId, loginUser.getId());
            ContestResult contestResult = this.getOne(queryWrapper);
            if (contestResult == null) { // 首次提交
                contestResult = new ContestResult();
                // 创建 resultQuestionDetailMap
                HashMap<Integer, ContestResultQuestionDetail> resultQuestionDetailMap = new HashMap<>();
                // 创建竞赛结果题目详情（resultQuestionDetail）并填充数据
                ContestResultQuestionDetail resultQuestionDetail = new ContestResultQuestionDetail();
                resultQuestionDetail.setQuestionId(questionId);
                resultQuestionDetail.setErrorTry(1);
                // 将 resultQuestionDetail 存入 resultQuestionDetailMap
                resultQuestionDetailMap.put(order, resultQuestionDetail);
                // 填充 contestResult
                contestResult.setContestId(contestQuestionSubmit.getContestId());
                contestResult.setApplicantId(loginUser.getId());
                contestResult.setUserName(loginUser.getUserName());
                contestResult.setTotalScore(0);
                contestResult.setContestDetail(GSON.toJson(resultQuestionDetailMap));
                // 写入数据库
                this.save(contestResult);
            } else {// 非首次提交
                // 获取竞赛结果详情（resultDetail）
                Map<Integer, ContestResultQuestionDetail> resultQuestionDetailMap = GSON.fromJson(contestResult.getContestDetail(), new TypeToken<Map<Integer, ContestResultQuestionDetail>>() {
                }.getType());
                // 获取竞赛结果题目详情（resultQuestionDetail）
                ContestResultQuestionDetail resultQuestionDetail = resultQuestionDetailMap.get(order);
                if (resultQuestionDetail == null) { // 第一次提交该题目
                    // 创建竞赛结果题目详情（resultQuestionDetail）并填充数据
                    resultQuestionDetail = new ContestResultQuestionDetail();
                    resultQuestionDetail.setQuestionId(questionId);
                    resultQuestionDetail.setErrorTry(1);
                    // 存入 resultQuestionDetailMap
                    resultQuestionDetailMap.put(order, resultQuestionDetail);
                    // 存入 contestResult
                    contestResult.setContestDetail(GSON.toJson(resultQuestionDetailMap));
                    // 更新
                    this.updateById(contestResult);
                } else if (resultQuestionDetail.getAcceptTime() == null) {// 之前提交过该题目，但未通过
                    // 创建竞赛结果题目详情（resultQuestionDetail）并填充数据
                    resultQuestionDetail.setErrorTry(resultQuestionDetail.getErrorTry() + 1);
                    // 存入 contestResult
                    contestResult.setContestDetail(GSON.toJson(resultQuestionDetailMap));
                    // 更新
                    this.updateById(contestResult);
                }
            }
        }
        return questionSubmitJudgeInfo;
    }

    /**
     * 竞赛题目运行
     *
     * @param runCodeRequest
     * @return
     */
    @Override
    public RunCodeResponse contestQuestionRun(RunCodeRequest runCodeRequest) {
        // 在线运行代码只需要提供一个输入用例，但执行代码请求需要输入用例以集合的方式传入，所以需要转换为集合，但集合只有一个元素
        List<String> inputList = Arrays.asList(runCodeRequest.getInput());
        // 构造执行代码请求
        ExecuteCodeRequest executeCodeRequest = ExecuteCodeRequest.builder().code(runCodeRequest.getCode()).language(runCodeRequest.getLanguage()).inputList(inputList).build();
        // 运行
        ExecuteCodeResponse executeCodeResponse = judgeFeignClient.runOnline(executeCodeRequest);
        // 获取输出
        List<String> outputList = Optional.ofNullable(executeCodeResponse.getOutputList()).orElse(Arrays.asList(""));
        // 构造运行代码响应
        return RunCodeResponse.builder().output(outputList.get(0)).message(executeCodeResponse.getMessage()).status(executeCodeResponse.getStatus()).judgeInfo(executeCodeResponse.getJudgeInfo()).build();
    }

    /**
     * 获取查询条件
     *
     * @param contestResultQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<ContestResult> getQueryWrapper(ContestResultQueryRequest contestResultQueryRequest) {
        QueryWrapper<ContestResult> queryWrapper = new QueryWrapper<>();
        // 判空
        if (contestResultQueryRequest == null) {
            return queryWrapper;
        }
        // 获取参数
        Long contestId = contestResultQueryRequest.getContestId();
        String sortField = contestResultQueryRequest.getSortField();
        String sortOrder = contestResultQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.eq(ObjectUtils.isNotEmpty(contestId), "contestId", contestId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        // 返回
        return queryWrapper;
    }

    /**
     * 竞赛排名统计
     *
     * @param contestResultQueryRequest
     * @return
     */
    @Override
    public Page<ContestRank> contestRankStatistics(ContestResultQueryRequest contestResultQueryRequest) {
        // 获取分页参数
        long current = contestResultQueryRequest.getCurrent();
        long size = contestResultQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 50, ErrorCode.PARAMS_ERROR);
        // 查询缓存
        String key = RedisKey.getKey(CONTEST_RANK_KEY, contestResultQueryRequest.getContestId());
        String contestRankPageStr = stringRedisTemplate.opsForValue().get(key);
        if (StrUtil.isNotBlank(contestRankPageStr)) {
            Page<ContestRank> page = GSON.fromJson(contestRankPageStr, new TypeToken<Page<ContestRank>>() {
            }.getType());
            if (page != null) {
                return page;
            }
        }
        // 判断竞赛是否存在
        Contest contest = contestService.getById(contestResultQueryRequest.getContestId());
        if (contest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请不要攻击本站");
        }
        // 查询
        Page<ContestResult> contestResultPage = this.page(new Page<>(current, size), this.getQueryWrapper(contestResultQueryRequest));
        // 处理
        List<ContestRank> contestRankList = contestResultPage.getRecords().stream().map(contestResult -> {
            ContestRank contestRank = new ContestRank();
            // 填充用户信息
            UserVO userVO = userFeignClient.getUserVO(userFeignClient.getById(contestResult.getApplicantId()));
            contestRank.setUserVO(userVO);
            // 填充得分
            contestRank.setScore(contestResult.getTotalScore());
            // 获取竞赛结果题目详情
            Map<Integer, ContestResultQuestionDetail> resultQuestionDetailMap = GSON.fromJson(contestResult.getContestDetail(), new TypeToken<Map<Integer, ContestResultQuestionDetail>>() {
            }.getType());
            // 创建提交详情
            Map<Integer, ContestRankQuestionDetail> submitDetail = new HashMap<>();
            // 最大通过时间
            Date maxTime = null;
            // 总错误尝试次数
            int errorTry = 0;
            // 获取竞赛开始时间
            Date startTime = contest.getStartTime();
            // 遍历竞赛结果题目详情
            for (Integer order : resultQuestionDetailMap.keySet()) {
                // 获取竞赛结果题目详情
                ContestResultQuestionDetail resultQuestionDetail = resultQuestionDetailMap.get(order);
                // 获取该题目通过时间
                Date acceptTime = resultQuestionDetail.getAcceptTime();
                ContestRankQuestionDetail rankQuestionDetail = null;
                if (acceptTime != null) {// 通过该题目
                    // 创建竞赛排名题目详情
                    rankQuestionDetail = new ContestRankQuestionDetail();
                    // 设置题目id
                    rankQuestionDetail.setId(resultQuestionDetail.getQuestionId());
                    // 设置该题目的通过时间
                    rankQuestionDetail.setAcceptedTime(calcHourMinuteSecond(acceptTime, startTime).getKey());
                    // 统计最大通过时间
                    if (maxTime == null) {
                        maxTime = acceptTime;
                    } else if (acceptTime.after(maxTime)) {
                        maxTime = acceptTime;
                    }
                    // 设置代码
                    rankQuestionDetail.setCode(resultQuestionDetail.getCode());
                    // 放入提交详情
                    submitDetail.put(order, rankQuestionDetail);
                }
                // 累加错误尝试次数
                errorTry += resultQuestionDetail.getErrorTry();
            }
            // 填充提交详情
            contestRank.setSubmitDetail(submitDetail);
            // 填充总耗时
            if (maxTime != null) {// 有通过题目
                // 添加惩罚加时
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(maxTime);
                calendar.add(Calendar.MINUTE, errorTry * 5);
                contestRank.setTime(calcHourMinuteSecond(calendar.getTime(), contest.getStartTime()));
            } else {// 没有通过题目
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(new Date());
                calendar.set(Calendar.HOUR, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                contestRank.setTime(calcHourMinuteSecond(calendar.getTime(), calendar.getTime()));
            }
            return contestRank;
        }).sorted((o1, o2) -> !o1.getScore().equals(o2.getScore()) ? o2.getScore() - o1.getScore() : o2.getTime().getValue() - o1.getTime().getValue()).collect(Collectors.toList());
        // 组装成新的 Page
        Page<ContestRank> contestRankPage = new Page<>();
        contestRankPage.setRecords(contestRankList);
        contestRankPage.setCurrent(contestResultPage.getCurrent());
        contestRankPage.setSize(contestResultPage.getSize());
        contestRankPage.setTotal(contestResultPage.getTotal());
        // 存入缓存
        stringRedisTemplate.opsForValue().set(key, GSON.toJson(contestRankPage));
        stringRedisTemplate.expire(key, CONTEST_RANK_KEY_TTL, TimeUnit.SECONDS);
        // 返回
        return contestRankPage;
    }

    /**
     * 删除
     *
     * @param id
     * @return
     */
    @Override
    public Boolean deleteContestResult(Long id) {
        return this.removeById(id);
    }

    /**
     * 获取竞赛结果
     *
     * @param id
     * @return
     */
    @Override
    public ContestResult getContestResultById(Long id) {
        return this.getById(id);
    }

    /**
     * 分页获取竞赛结果
     *
     * @param contestResultQueryRequest
     * @return
     */
    @Override
    public Page<ContestResult> listContestResultByPage(ContestResultQueryRequest contestResultQueryRequest) {
        // 获取分页参数
        long current = contestResultQueryRequest.getCurrent();
        long size = contestResultQueryRequest.getPageSize();
        // 查询
        return this.page(new Page<>(current, size), this.getQueryWrapper(contestResultQueryRequest));
    }

    /**
     * 计算时间差,返回时分秒 HH:mm:ss
     *
     * @param acceptTime
     * @param startTime
     * @return
     */
    Pair<String, Integer> calcHourMinuteSecond(Date acceptTime, Date startTime) {
        DecimalFormat decimalFormat = new DecimalFormat("00");
        long diff = acceptTime.getTime() - startTime.getTime();//得到的差值
        long hours = diff / (1000 * 60 * 60); //获取时
        long minutes = (diff - hours * (1000 * 60 * 60)) / (1000 * 60);  //获取分钟
        long s = (diff / 1000 - hours * 60 * 60 - minutes * 60);//获取秒
        String countTime = decimalFormat.format(hours) + ":" + decimalFormat.format(minutes) + ":" + decimalFormat.format(s);
        return new Pair<>(countTime, (int) diff / 1000);
    }
}




