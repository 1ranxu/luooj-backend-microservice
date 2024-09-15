package com.luoying.luoojbackendquestionservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.BooleanUtil;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luoying.luoojbackendcommon.common.DeleteRequest;
import com.luoying.luoojbackendcommon.common.ErrorCode;
import com.luoying.luoojbackendcommon.constant.CommonConstant;
import com.luoying.luoojbackendcommon.constant.LikeConstant;
import com.luoying.luoojbackendcommon.constant.RedisKey;
import com.luoying.luoojbackendcommon.constant.UserConstant;
import com.luoying.luoojbackendcommon.exception.BusinessException;
import com.luoying.luoojbackendcommon.utils.SqlUtils;
import com.luoying.luoojbackendmodel.dto.question_comment.QuestionCommentAddRequest;
import com.luoying.luoojbackendmodel.dto.question_comment.QuestionCommentQueryRequest;
import com.luoying.luoojbackendmodel.entity.Question;
import com.luoying.luoojbackendmodel.entity.QuestionComment;
import com.luoying.luoojbackendmodel.entity.User;
import com.luoying.luoojbackendmodel.vo.QuestionCommentVO;
import com.luoying.luoojbackendquestionservice.mapper.QuestionCommentMapper;
import com.luoying.luoojbackendquestionservice.service.QuestionCommentService;
import com.luoying.luoojbackendquestionservice.service.QuestionService;
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
 * @description 针对表【question_comment(题目评论表)】的数据库操作Service实现
 * @createDate 2024-09-02 14:26:15
 */
@Service
public class QuestionCommentServiceImpl extends ServiceImpl<QuestionCommentMapper, QuestionComment> implements QuestionCommentService {

    @Resource
    private UserFeignClient userFeignClient;

    @Resource
    private QuestionService questionService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 发表评论
     * 回复评论
     *
     * @param questionCommentAddRequest
     * @param request
     * @return
     */
    @Override
    public Boolean publishQuestionComment(QuestionCommentAddRequest questionCommentAddRequest, HttpServletRequest request) {
        // 获取登录用户
        User loginUser = userFeignClient.getLoginUser(request);
        // 拷贝
        QuestionComment questionComment = BeanUtil.copyProperties(questionCommentAddRequest, QuestionComment.class);
        questionComment.setUserId(loginUser.getId());
        // 对应题目的评论总数 +1
        boolean isSuccess = questionService.update().setSql("comments = comments + 1").eq("id", questionCommentAddRequest.getQuestionId()).update();
        // 写入数据库
        return this.save(questionComment) && isSuccess;
    }

    /**
     * 删除评论
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @Override
    public Boolean deleteQuestionComment(DeleteRequest deleteRequest, HttpServletRequest request) {
        // 获取登录用户
        User loginUser = userFeignClient.getLoginUser(request);
        // 只有本人和管理员和评论所属题目的创建人才能删除评论（题目创始人删除题目）
        QuestionComment comment = this.getById(deleteRequest.getId());
        Question question = questionService.getById(comment.getQuestionId());
        if (!comment.getUserId().equals(loginUser.getId()) && !question.getUserId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有本人和管理员和评论所属题目的创建人才能删除评论");
        }
        // 删除该评论
        boolean isSuccess = this.removeById(deleteRequest.getId());
        // 对应题目的评论总数 -1
        questionService.update().setSql("comments = comments - 1").eq("id", comment.getQuestionId()).update();
        if (isSuccess) {// 删除缓存
            String key = RedisKey.getKey(LIKE_LIST_KEY, LikeConstant.QUESTION_COMMENT, deleteRequest.getId());
            stringRedisTemplate.delete(key);
        }
        if (comment.getParentId().longValue() == 0) {
            LambdaQueryWrapper<QuestionComment> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(QuestionComment::getParentId, deleteRequest.getId());
            List<QuestionComment> list = this.list(queryWrapper);
            // 删除子评论
            boolean isSuccess1 = this.remove(queryWrapper);
            // 对应题目的评论总数 -list.size()
            questionService.update().setSql("comments = comments - " + list.size()).eq("id", comment.getQuestionId()).update();
            if (isSuccess1) {
                for (QuestionComment c : list) {
                    // 删除缓存
                    String key = RedisKey.getKey(LIKE_LIST_KEY, LikeConstant.QUESTION_COMMENT, c.getId());
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
    public Boolean likeQuestionComment(Long id, HttpServletRequest request) {
        // 获取登录用户
        User loginUser = userFeignClient.getLoginUser(request);
        // 判断当前登录用户是否已经点赞该评论
        String key = RedisKey.getKey(LIKE_LIST_KEY, LikeConstant.QUESTION_COMMENT, id);
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

    @Override
    public Boolean isLiked(Long commentId, Long userId) {
        // 判断当前用户是否已经点赞该评论
        String key = RedisKey.getKey(LIKE_LIST_KEY, LikeConstant.QUESTION_COMMENT, commentId);
        return stringRedisTemplate.opsForSet().isMember(key, userId.toString());
    }

    /**
     * 根据id查询某条评论（仅管理员）
     *
     * @param id
     * @return
     */
    @Override
    public QuestionComment getQuestionCommentByid(Long id) {
        return this.getById(id);
    }

    /**
     * 获取条件构造器
     *
     * @param questionCommentQueryRequest
     * @return
     */
    @Override
    public Wrapper<QuestionComment> getQueryWrapper(QuestionCommentQueryRequest questionCommentQueryRequest) {
        QueryWrapper<QuestionComment> queryWrapper = new QueryWrapper<>();
        // 判空
        if (questionCommentQueryRequest == null) {
            return queryWrapper;
        }
        // 获取参数
        Long id = questionCommentQueryRequest.getId();
        Long userId = questionCommentQueryRequest.getUserId();
        Long questionId = questionCommentQueryRequest.getQuestionId();
        Long parentId = questionCommentQueryRequest.getParentId();
        Long respondUserId = questionCommentQueryRequest.getRespondUserId();
        String content = questionCommentQueryRequest.getContent();
        String sortField = questionCommentQueryRequest.getSortField();
        String sortOrder = questionCommentQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(questionId), "questionId", questionId);
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
     * @param questionCommentQueryRequest
     * @return
     */
    @Override
    public Page<QuestionComment> listQuestionCommentByPage(QuestionCommentQueryRequest questionCommentQueryRequest) {
        // 获取分页参数
        long current = questionCommentQueryRequest.getCurrent();
        long size = questionCommentQueryRequest.getPageSize();
        // 查询
        return this.page(new Page<>(current, size), this.getQueryWrapper(questionCommentQueryRequest));
    }

    /**
     * 根据题目id查询评论列表
     *
     * @param questionCommentQueryRequest
     * @param request
     * @return
     */
    @Override
    public QuestionCommentVO listQuestionComment(QuestionCommentQueryRequest questionCommentQueryRequest, HttpServletRequest request) {
        QuestionCommentVO questionCommentVO = new QuestionCommentVO();
        long current = questionCommentQueryRequest.getCurrent();
        long pageSize = questionCommentQueryRequest.getPageSize();
        // 获取登录用户id
        Long userId = userFeignClient.getLoginUser(request).getId();
        // 根据题目id查询评论列表
        List<QuestionComment> allList = this.list(this.getQueryWrapper(questionCommentQueryRequest));
        // 存储所有一级评论
        Map<Long, QuestionComment> map = new HashMap<>();
        List<QuestionComment> result = new ArrayList<>();
        for (QuestionComment c : allList) {
            if (c.getParentId() == 0) { // 这是一个一级评论
                // 判断是否点赞过
                c.setIsLike(isLiked(c.getId(), userId));
                result.add(c);
                map.put(c.getId(), c);
                c.setChildList(new ArrayList<>());
            }
        }
        // 存储所有二级评论
        for (QuestionComment c : allList) {
            if (c.getParentId() != 0) { // 这是一个二级评论
                // 获取该二级评论的父评论
                QuestionComment parent = map.get(c.getParentId());
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
        questionCommentVO.setResult(result.subList(startIndex, endIndex));
        questionCommentVO.setCommentNum((long) allList.size());
        questionCommentVO.setTotal((long) result.size());
        // 返回
        return questionCommentVO;
    }
}




