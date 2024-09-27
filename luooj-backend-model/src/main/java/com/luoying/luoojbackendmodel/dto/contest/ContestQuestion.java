package com.luoying.luoojbackendmodel.dto.contest;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/17 20:14
 */
@Data
public class ContestQuestion implements Serializable {
    /**
     * 题目id
     */
    private Long questionId;

    /**
     * 分数
     */
    private Integer score;

    private static final long serialVersionUID = 1L;
}
