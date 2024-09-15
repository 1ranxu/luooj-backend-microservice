package com.luoying.luoojbackendmodel.dto.question_collect;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/15 16:40
 */
@Data
public class QuestionCollectAddRequest implements Serializable {
    /**
     * 题单id
     */
    private Long questionListId;

    /**
     * 收藏的题目的id
     */
    private Long questionId;

    private static final long serialVersionUID = 1L;
}
