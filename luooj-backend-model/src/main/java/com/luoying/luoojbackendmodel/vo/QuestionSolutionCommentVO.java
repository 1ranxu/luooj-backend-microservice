package com.luoying.luoojbackendmodel.vo;

import com.luoying.luoojbackendmodel.entity.QuestionSolutionComment;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/8 19:43
 */
@Data
public class QuestionSolutionCommentVO implements Serializable {
    /**
     * 包含了所有一级评论
     */
    private List<QuestionSolutionComment> result;

    /**
     * 评论总数（用于预览）
     */
    private Long commentNum;

    /**
     * 一级评论总数（用于分页）
     */
    private Long total;

    private static final long serialVersionUID = 1L;
}