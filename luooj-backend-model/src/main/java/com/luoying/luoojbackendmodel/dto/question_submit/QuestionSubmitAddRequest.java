package com.luoying.luoojbackendmodel.dto.question_submit;

import lombok.Data;

import java.io.Serializable;

/**
 * @author 落樱的悔恨
 * 题目提交创建请求
 */
@Data
public class QuestionSubmitAddRequest implements Serializable {
    /**
     * 编程语言
     */
    private String language;

    /**
     * 题目id
     */
    private Long questionId;

    /**
     * 用户提交的代码
     */
    private String code;

    private static final long serialVersionUID = 1L;
}
