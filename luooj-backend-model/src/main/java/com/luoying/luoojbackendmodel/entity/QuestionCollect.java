package com.luoying.luoojbackendmodel.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 题单收藏题目表
 * @TableName question_collect
 */
@TableName(value ="question_collect")
@Data
public class QuestionCollect implements Serializable {
    /**
     * 题目收藏记录id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 题单id
     */
    private String questionListId;

    /**
     * 收藏的题目的id
     */
    private Long questionId;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}