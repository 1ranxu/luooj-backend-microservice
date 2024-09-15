package com.luoying.luoojbackendmodel.dto.question_collect;

import com.luoying.luoojbackendcommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/15 16:41
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionCollectQueryRequest extends PageRequest implements Serializable {
    /**
     * 题目收藏记录id
     */
    private Long id;

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
