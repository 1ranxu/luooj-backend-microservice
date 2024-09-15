package com.luoying.luoojbackendmodel.dto.question_list_collect;

import com.luoying.luoojbackendcommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/15 16:40
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionListCollectQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 题单id
     */
    private Long questionListId;

    /**
     * 收藏人id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}
