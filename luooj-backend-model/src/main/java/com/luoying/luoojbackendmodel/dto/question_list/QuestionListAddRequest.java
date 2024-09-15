package com.luoying.luoojbackendmodel.dto.question_list;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/15 16:05
 */
@Data
public class QuestionListAddRequest implements Serializable {
    /**
     * 题单标题
     */
    private String title;

    private static final long serialVersionUID = 1L;
}
