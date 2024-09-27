package com.luoying.luoojbackendmodel.dto.contest_result;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/17 20:10
 */
@Data
public class ContestResultQuestionDetail implements Serializable {
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

    /**
     * 通过时间
     */
    private Date acceptTime;

    /**
     * 错误尝试次数（默认为0）
     */
    private Integer errorTry;

    private static final long serialVersionUID = 1L;
}
