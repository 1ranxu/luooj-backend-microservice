package com.luoying.luoojbackendmodel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 竞赛成绩表
 *
 * @TableName contest_result
 */
@TableName(value = "contest_result")
@Data
public class ContestResult implements Serializable {
    /**
     * 竞赛成绩记录id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 竞赛id
     */
    private Long contestId;

    /**
     * 参赛者id
     */
    private Long applicantId;

    /**
     * 参赛者名称
     */
    private String userName;

    /**
     * 总得分
     */
    private Long totalScore;

    /**
     * 所有题目的总耗时(s)
     */
    private Long totalTime;

    /**
     * 所有题目的总内存(KB)
     */
    private Long totalMemory;

    /**
     * 竞赛详情（json）
     */
    private String contestDetail;

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