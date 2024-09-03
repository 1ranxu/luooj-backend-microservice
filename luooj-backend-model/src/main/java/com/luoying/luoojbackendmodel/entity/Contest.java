package com.luoying.luoojbackendmodel.entity;

import com.baomidou.mybatisplus.annotation.*;

import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 竞赛表
 * @TableName contest
 */
@TableName(value ="contest")
@Data
public class Contest implements Serializable {
    /**
     * 竞赛id
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 竞赛标题
     */
    private String title;

    /**
     * 开始时间
     */
    private Date startTime;

    /**
     * 结束时间
     */
    private Date endTime;

    /**
     * 比赛时长（秒）
     */
    private Integer duration;

    /**
     * 竞赛奖励
     */
    private String award;

    /**
     * 重要提示
     */
    private String tips;

    /**
     * 题目列表（json 数组）
     */
    private String questions;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 状态（0-待开始、1-进行中、2-已结束）
     */
    private Integer status;

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