package com.luoying.luoojbackendmodel.dto.question_comment;

import com.luoying.luoojbackendcommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/13 19:41
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionCommentQueryRequest extends PageRequest implements Serializable {
    /**
     * 评论id
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 题目id
     */
    private Long questionId;

    /**
     * 本评论关联的1级评论id，如果本评论是一级评论，则值为0
     */
    private Long parentId;

    /**
     * 本评论回复的评论的发布人，如果本评论是一级评论，则值为0
     */
    private Long respondUserId;

    /**
     * 回复内容
     */
    private String content;

    private static final long serialVersionUID = 1L;
}
