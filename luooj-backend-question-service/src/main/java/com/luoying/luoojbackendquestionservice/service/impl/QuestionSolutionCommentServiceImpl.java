package com.luoying.luoojbackendquestionservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luoying.luoojbackendcommon.common.DeleteRequest;
import com.luoying.luoojbackendcommon.common.ErrorCode;
import com.luoying.luoojbackendcommon.constant.CommonConstant;
import com.luoying.luoojbackendcommon.constant.RedisKey;
import com.luoying.luoojbackendcommon.constant.UserConstant;
import com.luoying.luoojbackendcommon.exception.BusinessException;
import com.luoying.luoojbackendcommon.utils.SqlUtils;
import com.luoying.luoojbackendmodel.dto.question_solution_comment.QuestionSolutionCommentAddRequest;
import com.luoying.luoojbackendmodel.dto.question_solution_comment.QuestionSolutionCommentQueryRequest;
import com.luoying.luoojbackendmodel.entity.QuestionSolution;
import com.luoying.luoojbackendmodel.entity.QuestionSolutionComment;
import com.luoying.luoojbackendmodel.entity.User;
import com.luoying.luoojbackendmodel.vo.QuestionSolutionCommentVO;
import com.luoying.luoojbackendquestionservice.mapper.QuestionSolutionCommentMapper;
import com.luoying.luoojbackendquestionservice.service.QuestionSolutionCommentService;
import com.luoying.luoojbackendquestionservice.service.QuestionSolutionService;
import com.luoying.luoojbackendserviceclient.service.UserFeignClient;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;

import static com.luoying.luoojbackendcommon.constant.RedisKey.LIKE_LIST_KEY;

/**
 * @author 落樱的悔恨
 * @description 针对表【question_solution_comment(题解评论表)】的数据库操作Service实现
 * @createDate 2024-09-02 14:41:44
 */
@Service
public class QuestionSolutionCommentServiceImpl extends ServiceImpl<QuestionSolutionCommentMapper, QuestionSolutionComment>
        implements QuestionSolutionCommentService {

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private QuestionSolutionService questionSolutionService;

    /**
     * 发表评论
     * 回复评论
     *
     * @param questionSolutionCommentAddRequest
     * @param request
     * @return
     */
    @Override
    public Boolean publishQuestionSolutionComment(QuestionSolutionCommentAddRequest questionSolutionCommentAddRequest, HttpServletRequest request) {
        // 获取登录用户
        User loginUser = userFeignClient.getLoginUser(request);
        // 拷贝
        QuestionSolutionComment questionSolutionComment = BeanUtil.copyProperties(questionSolutionCommentAddRequest, QuestionSolutionComment.class);
        questionSolutionComment.setUserId(loginUser.getId());
        // 对应题目的评论总数 +1
        boolean isSuccess = questionSolutionService.update().setSql("comments = comments + 1").eq("id", questionSolutionCommentAddRequest.getSolutionId()).update();
        // 写入数据库
        return this.save(questionSolutionComment) && isSuccess;
    }

    /**
     * 删除评论
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @Override
    public Boolean deleteQuestionSolutionComment(DeleteRequest deleteRequest, HttpServletRequest request) {
        // 获取登录用户
        User loginUser = userFeignClient.getLoginUser(request);
        // 只有本人和管理员和评论所属题解的创建人才能删除评论（题解创始人删除题解）
        QuestionSolutionComment comment = this.getById(deleteRequest.getId());
        QuestionSolution questionSolution = questionSolutionService.getById(comment.getSolutionId());
        if (!comment.getUserId().equals(loginUser.getId()) && !questionSolution.getUserId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有本人和管理员和评论所属题解的创建人才能删除评论");
        }
        // 删除该评论
        boolean isSuccess = this.removeById(deleteRequest.getId());
        // 对应题解的评论总数 -1
        questionSolutionService.update().setSql("comments = comments - 1").eq("id", comment.getSolutionId()).update();
        if (isSuccess) {// 删除缓存
            String key = RedisKey.getKey(LIKE_LIST_KEY, "question_solution_comment", deleteRequest.getId());
            stringRedisTemplate.delete(key);
        }
        if (comment.getParentId().longValue() == 0) {
            LambdaQueryWrapper<QuestionSolutionComment> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(QuestionSolutionComment::getParentId, deleteRequest.getId());
            List<QuestionSolutionComment> list = this.list(queryWrapper);
            // 删除子评论
            boolean isSuccess1 = this.remove(queryWrapper);
            // 对应题解的评论总数 -list.size()
            questionSolutionService.update().setSql("comments = comments - " + list.size()).eq("id", comment.getSolutionId()).update();
            if (isSuccess1) {
                for (QuestionSolutionComment c : list) {
                    // 删除缓存
                    String key = RedisKey.getKey(LIKE_LIST_KEY, "question_solution_comment", c.getId());
                    stringRedisTemplate.delete(key);
                }
            }
        }
        return isSuccess;
    }

    /**
     * 点赞某评论
     *
     * @param id
     * @param request
     * @return
     */
    @Override
    public Boolean likeQuestionSolutionComment(Long id, HttpServletRequest request) {
        // 获取登录用户
        User loginUser = userFeignClient.getLoginUser(request);
        // 判断当前登录用户是否已经点赞该评论
        String key = RedisKey.getKey(LIKE_LIST_KEY, "question_solution_comment", id);
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
     * @param commentId
     * @param userId
     * @return
     */
    @Override
    public Boolean isLiked(Long commentId, Long userId) {
        // 判断当前用户是否已经点赞该评论
        String key = RedisKey.getKey(LIKE_LIST_KEY, "question_solution_comment", commentId);
        return stringRedisTemplate.opsForSet().isMember(key, userId.toString());
    }

    /**
     * 根据id查询某条评论（仅管理员）
     *
     * @param id
     * @return
     */
    @Override
    public QuestionSolutionComment getQuestionSolutionCommentByid(Long id) {
        return this.getById(id);
    }

    @Override
    public QueryWrapper<QuestionSolutionComment> getQueryWrapper(QuestionSolutionCommentQueryRequest questionSolutionCommentQueryRequest) {
        QueryWrapper<QuestionSolutionComment> queryWrapper = new QueryWrapper<>();
        // 判空
        if (questionSolutionCommentQueryRequest == null) {
            return queryWrapper;
        }
        // 获取参数
        Long id = questionSolutionCommentQueryRequest.getId();
        Long userId = questionSolutionCommentQueryRequest.getUserId();
        Long solutionId = questionSolutionCommentQueryRequest.getSolutionId();
        Long parentId = questionSolutionCommentQueryRequest.getParentId();
        Long respondUserId = questionSolutionCommentQueryRequest.getRespondUserId();
        String content = questionSolutionCommentQueryRequest.getContent();
        String sortField = questionSolutionCommentQueryRequest.getSortField();
        String sortOrder = questionSolutionCommentQueryRequest.getSortOrder();
        // 拼接查询条件
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(solutionId), "solutionId", solutionId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(parentId), "parentId", parentId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(respondUserId), "respondUserId", respondUserId);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        // 返回
        return queryWrapper;
    }

    /**
     * 分页查询评论列表（仅管理员）
     *
     * @param questionSolutionCommentQueryRequest
     * @return
     */
    @Override
    public Page<QuestionSolutionComment> listQuestionSolutionCommentByPage(QuestionSolutionCommentQueryRequest questionSolutionCommentQueryRequest) {
        // 获取分页参数
        long current = questionSolutionCommentQueryRequest.getCurrent();
        long size = questionSolutionCommentQueryRequest.getPageSize();
        // 查询
        return this.page(new Page<>(current, size),
                this.getQueryWrapper(questionSolutionCommentQueryRequest));
    }

    /**
     * 根据题解id查询评论列表
     *
     * @param questionSolutionCommentQueryRequest
     * @param request
     * @return
     */
    @Override
    public QuestionSolutionCommentVO listQuestionSolutionComment(QuestionSolutionCommentQueryRequest questionSolutionCommentQueryRequest, HttpServletRequest request) {
        QuestionSolutionCommentVO questionSolutionCommentVO = new QuestionSolutionCommentVO();
        long current = questionSolutionCommentQueryRequest.getCurrent();
        long pageSize = questionSolutionCommentQueryRequest.getPageSize();
        // 获取登录用户id
        Long userId = userFeignClient.getLoginUser(request).getId();
        // 根据题解id查询评论列表
        List<QuestionSolutionComment> allList = this.list(this.getQueryWrapper(questionSolutionCommentQueryRequest));
        // 存储所有一级评论
        Map<Long, QuestionSolutionComment> map = new HashMap<>();
        List<QuestionSolutionComment> result = new ArrayList<>();
        for (QuestionSolutionComment c : allList) {
            if (c.getParentId() == 0) { // 这是一个一级评论
                // 判断是否点赞过
                c.setIsLike(isLiked(c.getId(), userId));
                result.add(c);
                map.put(c.getId(), c);
                c.setChildList(new ArrayList<>());
            }
        }
        // 存储所有二级评论
        for (QuestionSolutionComment c : allList) {
            if (c.getParentId() != 0) { // 这是一个二级评论
                // 获取该二级评论的父评论
                QuestionSolutionComment parent = map.get(c.getParentId());
                if (parent == null) { // 二级评论的父评论有可能被删除，该二级评论也就无需返回
                    continue;
                }
                // 判断是否点赞过
                c.setIsLike(isLiked(c.getId(), userId));
                // 把该评论添加到父级评论的子集评论列表中
                parent.getChildList().add(c);
            }
        }
        // 根据点赞数从大到小排序，点赞数相等，则根据评论数从大到小排序排序
        result.sort((o1, o2) -> {
            if (!Objects.equals(o1.getLikes(), o2.getLikes())) {
                return (int) (o2.getLikes() - o1.getLikes());
            } else {
                return o2.getChildList().size() - o1.getChildList().size();
            }
        });
        // 设置分页参数
        int startIndex = Math.min((int) ((current - 1) * pageSize), result.size());
        int endIndex = Math.min((int) (startIndex + pageSize), result.size());
        // 设置结果
        questionSolutionCommentVO.setResult(result.subList(startIndex, endIndex));
        questionSolutionCommentVO.setCommentNum((long) allList.size());
        questionSolutionCommentVO.setTotal((long) result.size());
        // 返回
        return questionSolutionCommentVO;
    }
}




