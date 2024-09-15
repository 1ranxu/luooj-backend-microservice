package com.luoying.luoojbackendmodel.dto.question_list;

import com.luoying.luoojbackendcommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/15 16:25
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionListQueryRequest extends PageRequest implements Serializable {
    /**
     * 题单id
     */
    private Long id;

    /**
     * 题单标题
     */
    private String title;

    /**
     * 创建人id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}
