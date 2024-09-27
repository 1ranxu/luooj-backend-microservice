package com.luoying.luoojbackendmodel.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/16 14:26
 */
@Data
public class ContestRankQuestionDetail implements Serializable {
    /**
     * 题目id
     */
    private Long id;

    /**
     * 通过时间
     */
    private String acceptedTime;

    /**
     * 代码
     */
    private String code;


    private static final long serialVersionUID = 1L;
}
