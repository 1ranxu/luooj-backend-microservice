package com.luoying.luoojbackendmodel.dto.questionsubmit;

import lombok.Data;
@Data
public class QuestionSubmitAddRequest {
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
