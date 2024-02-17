package com.luoying.luoojbackendquestionservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luoying.luoojbackendcommon.common.ErrorCode;
import com.luoying.luoojbackendcommon.constant.CommonConstant;
import com.luoying.luoojbackendcommon.exception.BusinessException;
import com.luoying.luoojbackendcommon.exception.ThrowUtils;
import com.luoying.luoojbackendcommon.utils.SqlUtils;
import com.luoying.luoojbackendmodel.dto.question.QuestionQueryRequest;
import com.luoying.luoojbackendmodel.entity.AcceptedQuestion;
import com.luoying.luoojbackendmodel.entity.Question;
import com.luoying.luoojbackendmodel.entity.User;
import com.luoying.luoojbackendmodel.vo.QuestionVO;
import com.luoying.luoojbackendmodel.vo.UserVO;
import com.luoying.luoojbackendquestionservice.mapper.AcceptedQuestionMapper;
import com.luoying.luoojbackendquestionservice.mapper.QuestionMapper;
import com.luoying.luoojbackendquestionservice.service.QuestionService;
import com.luoying.luoojbackendserviceclient.service.UserFeighClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author 落樱的悔恨
 * @description 针对表【question(题目)】的数据库操作Service实现
 * @createDate 2023-11-09 16:32:34
 */
@Service
public class QuestionServiceImpl extends ServiceImpl<QuestionMapper, Question>
        implements QuestionService {
    @Resource
    private UserFeighClient userFeighClient;

    @Resource
    private AcceptedQuestionMapper acceptedQuestionMapper;

    /**
     * 校验参数
     * @param question 题目
     * @param add 是否为新增
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
        String answer = questionQueryRequest.getAnswer();
        Long userId = questionQueryRequest.getUserId();
        String sortField = questionQueryRequest.getSortField();
        String sortOrder = questionQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        queryWrapper.like(StringUtils.isNotBlank(answer), "answer", answer);
        if (CollectionUtils.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        // 返回
        return queryWrapper;
    }

    /**
     * 获取封装后的题目
     * @param question 题目
     * @param request {@link HttpServletRequest}
     */
    @Override
    public QuestionVO getQuestionVO(Question question, HttpServletRequest request) {
        QuestionVO questionVO = QuestionVO.objToVo(question);
        // 1. 关联查询创建人信息
        Long userId = question.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userFeighClient.getById(userId);
        }
        UserVO userVO = userFeighClient.getUserVO(user);
        // 设置创建人消息
        questionVO.setUserVO(userVO);
        // 返回
        return questionVO;
    }

    /**
     * 分页获取封装后的题目
     * @param questionPage {@link Page<Question>}
     * @param request {@link HttpServletRequest}
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
        Map<Long, List<User>> userIdUserListMap = userFeighClient.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 填充信息
        List<QuestionVO> questionVOList = questionList.stream().map(question -> {
            QuestionVO questionVO = QuestionVO.objToVo(question);
            // 填充创建用户信息
            Long userId = question.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            questionVO.setUserVO(userFeighClient.getUserVO(user));
            return questionVO;
        }).collect(Collectors.toList());

        // 4. 获取当前登录用户id
        try {
            User loginUser = userFeighClient.getLoginUser(request);
            if (loginUser != null) {
                Long id = loginUser.getId();
                // 5. 查询该用户的题目通过表，获取所有通过题目的id集合
                String tableName = "accepted_question_" + id;
                List<AcceptedQuestion> acceptedQuestionList = acceptedQuestionMapper.queryAcceptedQuestionList(tableName);
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
}




