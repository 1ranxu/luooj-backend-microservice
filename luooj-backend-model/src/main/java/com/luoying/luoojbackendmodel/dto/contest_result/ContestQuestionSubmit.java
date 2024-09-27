package com.luoying.luoojbackendmodel.dto.contest_result;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/16 14:43
 */
@Data
public class ContestQuestionSubmit implements Serializable {
    /**
     * 竞赛id
     */
    private Long contestId;

    /**
     * 第几道题目
     */
    private Integer order;

    /**
     * 题目id
     */
    private Long questionId;

    /**
     * 编程语言
     */
    private String language;

    /**
     * 代码
     */
    private String code;

    private static final long serialVersionUID = 1L;
}
