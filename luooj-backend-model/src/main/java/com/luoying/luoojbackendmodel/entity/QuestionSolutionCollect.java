package com.luoying.luoojbackendmodel.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 题解收藏表
 * @TableName question_solution_collect
 */
@TableName(value ="question_solution_collect")
@Data
public class QuestionSolutionCollect implements Serializable {
    /**
     * 题解收藏记录id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 题解id
     */
    private Long questionListId;

    /**
     * 收藏人id
     */
    private Long userId;

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