package com.luoying.luoojbackendmodel.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class QuestionListVO implements Serializable {
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

    /**
     * 该题单是否收藏了某道题目
     */
    private Boolean isCollect;

    /**
     * 创建时间
     */
    private Date createTime;
}