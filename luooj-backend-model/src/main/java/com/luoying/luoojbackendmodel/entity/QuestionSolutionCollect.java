package com.luoying.luoojbackendmodel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

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
    private Long solutionId;

    /**
     * 收藏人id
     */
    private Long userId;

    /**
     * 创建时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}