package com.luoying.luoojbackendmodel.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 竞赛报名表
 * @TableName contest_apply
 */
@TableName(value ="contest_apply")
@Data
public class ContestApply implements Serializable {
    /**
     * 竞赛报名记录id
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