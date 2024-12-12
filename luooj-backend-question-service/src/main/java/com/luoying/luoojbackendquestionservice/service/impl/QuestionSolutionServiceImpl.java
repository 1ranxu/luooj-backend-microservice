package com.luoying.luoojbackendquestionservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
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
import com.luoying.luoojbackendcommon.constant.UserConstant;
import com.luoying.luoojbackendcommon.exception.BusinessException;
import com.luoying.luoojbackendcommon.exception.ThrowUtils;
import com.luoying.luoojbackendcommon.utils.SqlUtils;
import com.luoying.luoojbackendmodel.dto.question_solution.QuestionSolutionAddRequest;
import com.luoying.luoojbackendmodel.dto.question_solution.QuestionSolutionQueryRequest;
import com.luoying.luoojbackendmodel.dto.question_solution.QuestionSolutionUpdateRequest;
import com.luoying.luoojbackendmodel.entity.QuestionSolution;
import com.luoying.luoojbackendmodel.entity.QuestionSolutionCollect;
import com.luoying.luoojbackendmodel.entity.QuestionSolutionComment;
import com.luoying.luoojbackendmodel.entity.User;
import com.luoying.luoojbackendmodel.vo.UserVO;
import com.luoying.luoojbackendquestionservice.mapper.QuestionSolutionMapper;
import com.luoying.luoojbackendquestionservice.service.QuestionSolutionCollectService;
import com.luoying.luoojbackendquestionservice.service.QuestionSolutionCommentService;
import com.luoying.luoojbackendquestionservice.service.QuestionSolutionService;
import com.luoying.luoojbackendserviceclient.service.UserFeignClient;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.luoying.luoojbackendcommon.constant.RedisKey.LIKE_LIST_KEY;

/**
 * @author 落樱的悔恨
 * @description 针对表【question_solution(题解表)】的数据库操作Service实现
 * @createDate 2024-09-02 14:39:32
 */
@Service
public class QuestionSolutionServiceImpl extends ServiceImpl<QuestionSolutionMapper, QuestionSolution>
        implements QuestionSolutionService {

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    @Lazy
    private QuestionSolutionCommentService questionSolutionCommentService;

    @Resource
    private QuestionSolutionCollectService questionSolutionCollectService;

    private final static Gson GSON = new Gson();

    /**
     * 创建题解
     *
     * @param questionSolutionAddRequest
     * @param request
     * @return
     */
    @Override
    public Boolean addQuestionSolution(QuestionSolutionAddRequest questionSolutionAddRequest, HttpServletRequest request) {
        // 获取登录用户id
        Long userId = userFeignClient.getLoginUser(request).getId();
        // 拷贝
        QuestionSolution questionSolution = BeanUtil.copyProperties(questionSolutionAddRequest, QuestionSolution.class);
        // 获取题目标签
        List<String> tags = questionSolutionAddRequest.getTags();
        if (tags != null) {
            // 转Json字符串
            questionSolution.setTags(GSON.toJson(tags));
        }
        // 设置创建人id
        questionSolution.setUserId(userId);
        // 写入数据库
        return this.save(questionSolution);
    }

    /**
     * 删除题解
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @Override
    public Boolean deleteQuestionSolution(DeleteRequest deleteRequest, HttpServletRequest request) {
        // 获取登录用户id
        User loginUser = userFeignClient.getLoginUser(request);
        Long userId = loginUser.getId();
        // 只有管理员和本人才能删除
        QuestionSolution questionSolution = this.getById(deleteRequest.getId());
        if (!questionSolution.getUserId().equals(userId) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有本人或管理员可以删除");
        }
        // 删除该题解下的所有一级评论
        LambdaQueryWrapper<QuestionSolutionComment> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(QuestionSolutionComment::getSolutionId, deleteRequest.getId());
        queryWrapper.eq(QuestionSolutionComment::getParentId, 0);
        List<QuestionSolutionComment> list = questionSolutionCommentService.list(queryWrapper);
        for (QuestionSolutionComment c : list) {
            DeleteRequest delete = new DeleteRequest();
            delete.setId(c.getId());
            questionSolutionCommentService.deleteQuestionSolutionComment(delete, request);
        }
        // 删除该题解的收藏记录
        LambdaQueryWrapper<QuestionSolutionCollect> queryWrapper1 = new LambdaQueryWrapper<>();
        queryWrapper1.eq(QuestionSolutionCollect::getSolutionId, deleteRequest.getId());
        questionSolutionCollectService.remove(queryWrapper1);
        // 删除题解
        boolean isSuccess = this.removeById(deleteRequest.getId());
        if (isSuccess) { // 删除缓存
            String key = RedisKey.getKey(LIKE_LIST_KEY, LikeConstant.QUESTION_SOLUTION, deleteRequest.getId());
            stringRedisTemplate.delete(key);
        }
        return isSuccess;
    }

    /**
     * 点赞题解
     *
     * @param id
     * @param request
     * @return
     */
    @Override
    public Boolean likeQuestionSolution(Long id, HttpServletRequest request) {
        // 获取登录用户
        User loginUser = userFeignClient.getLoginUser(request);
        // 判断当前登录用户是否已经点赞该题解
        String key = RedisKey.getKey(LIKE_LIST_KEY, LikeConstant.QUESTION_SOLUTION, id);
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
     * 判断题解是否被点赞过
     *
     * @param solutionId
     * @param userId
     * @return
     */
    @Override
    public Boolean isLiked(Long solutionId, Long userId) {
        // 判断当前用户是否已经点赞该题解
        String key = RedisKey.getKey(LIKE_LIST_KEY, "question_solution", solutionId);
        return stringRedisTemplate.opsForSet().isMember(key, userId.toString());
    }

    /**
     * 更新题解
     *
     * @param questionSolutionUpdateRequest
     * @param request
     * @return
     */
    @Override
    public Boolean updateQuestionSolution(QuestionSolutionUpdateRequest questionSolutionUpdateRequest, HttpServletRequest request) {
        // 获取登录用户id
        User loginUser = userFeignClient.getLoginUser(request);
        Long userId = loginUser.getId();
        // 只有管理员和本人才能更新
        QuestionSolution questionSolution = this.getById(questionSolutionUpdateRequest.getId());
        if (!questionSolution.getUserId().equals(userId) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有本人或管理员可以修改");
        }
        // 拷贝
        questionSolution = BeanUtil.copyProperties(questionSolutionUpdateRequest, QuestionSolution.class);
        // 获取题目标签
        List<String> tags = questionSolutionUpdateRequest.getTags();
        if (tags != null) {
            // 转Json字符串
            questionSolution.setTags(GSON.toJson(tags));
        }
        // 更新
        return this.updateById(questionSolution);
    }

    /**
     * 查询题解
     *
     * @param id
     * @return
     */
    @Override
    public QuestionSolution getQuestionSolutionById(Long id,HttpServletRequest request) {
        // 获取登录用户
        User loginUser = userFeignClient.getLoginUser(request);
        // 查询题解
        QuestionSolution questionSolution = this.getById(id);
        // 判断当前登录用户是否点赞
        questionSolution.setIsLike(isLiked(id,loginUser.getId()));
        return questionSolution;
    }

    /**
     * 获取条件构造器
     *
     * @param questionSolutionQueryRequest
     * @return
     */
    @Override
    public Wrapper<QuestionSolution> getQueryWrapper(QuestionSolutionQueryRequest questionSolutionQueryRequest) {
        QueryWrapper<QuestionSolution> queryWrapper = new QueryWrapper<>();
        // 判空
        if (questionSolutionQueryRequest == null) {
            return queryWrapper;
        }
        // 获取参数
        Long id = questionSolutionQueryRequest.getId();
        Long questionId = questionSolutionQueryRequest.getQuestionId();
        Long userId = questionSolutionQueryRequest.getUserId();
        String title = questionSolutionQueryRequest.getTitle();
        String content = questionSolutionQueryRequest.getContent();
        List<String> tags = questionSolutionQueryRequest.getTags();
        String sortField = questionSolutionQueryRequest.getSortField();
        String sortOrder = questionSolutionQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        if (CollectionUtils.isNotEmpty(tags)) {
            for (String tag : tags) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        // 返回
        return queryWrapper;
    }

    /**
     * 分页获取题解列表（仅管理员）
     *
     * @param questionSolutionQueryRequest
     * @return
     */
    @Override
    public Page<QuestionSolution> listQuestionSolutionByPage(QuestionSolutionQueryRequest questionSolutionQueryRequest) {
        // 获取分页参数
        long current = questionSolutionQueryRequest.getCurrent();
        long size = questionSolutionQueryRequest.getPageSize();
        // 查询
        return this.page(new Page<>(current, size),
                this.getQueryWrapper(questionSolutionQueryRequest));
    }

    /**
     * 分页获取题解列表（普通用户）
     *
     * @param questionSolutionQueryRequest
     * @return
     */
    @Override
    public Page<QuestionSolution> listQuestionSolutionByPageUser(QuestionSolutionQueryRequest questionSolutionQueryRequest, HttpServletRequest request) {
        // 获取分页参数
        long current = questionSolutionQueryRequest.getCurrent();
        long size = questionSolutionQueryRequest.getPageSize();
        // 获取登录用户id
        Long userId = userFeignClient.getLoginUser(request).getId();
        // 限制爬虫
        ThrowUtils.throwIf(size > 50, ErrorCode.PARAMS_ERROR);
        // 查询
        Page<QuestionSolution> page = this.page(new Page<>(current, size),
                this.getQueryWrapper(questionSolutionQueryRequest));
        // 判断是否点赞过题解
        for (QuestionSolution questionSolution : page.getRecords()) {
            questionSolution.setIsLike(isLiked(questionSolution.getId(), userId));
        }
        // 填充题解作者信息
        for (QuestionSolution questionSolution : page.getRecords()){
            UserVO userVO = userFeignClient.getUserVO(userFeignClient.getById(questionSolution.getUserId()));
            questionSolution.setUserName(userVO.getUserName());
            questionSolution.setUserAvatar(userVO.getUserAvatar());
        }
        return page;
    }
}




