package com.luoying.luoojbackendmodel.dto.question_list_collect;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/15 16:40
 */
@Data
public class QuestionListCollectAddRequest implements Serializable {
    /**
     * 题单id
     */
    private Long questionListId;

    private static final long serialVersionUID = 1L;
}
