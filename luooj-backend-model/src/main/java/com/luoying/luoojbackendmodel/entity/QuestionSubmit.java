package com.luoying.luoojbackendmodel.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * @author 落樱的悔恨
 * 题目提交记录
 * @TableName question_submit
 */
@TableName(value = "question_submit")
@Data
public class QuestionSubmit implements Serializable {
    /**
     * 提交记录id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 编程语言
     */
    private String language;

    /**
     * 题目id
     */
    private Long questionId;

    /**
     * 提交用户id
     */
    private Long userId;

    /**
     * 用户提交的代码
     */
    private String code;

    /**
     * 判题信息
     */
    private String judgeInfo;

    /**
     * 判题状态（0-待判题、1-判题中、2-通过、3-失败）
     */
    private Integer status;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 是否删除
     */
    @TableLogic
    private Integer isDelete;

    @TableField(value = "COUNT(*)", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER, whereStrategy = FieldStrategy.NEVER)
    private Integer count;

    @TableField(value = "COUNT(DISTINCT DATE(createTime))", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER, whereStrategy = FieldStrategy.NEVER)
    private Integer countDay;

    @TableField(value = "DATE_FORMAT(createTime, '%Y')", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER, whereStrategy = FieldStrategy.NEVER)
    private Integer year;

    @TableField(value = "DATE_FORMAT(createTime, '%Y%m%d')", insertStrategy = FieldStrategy.NEVER, updateStrategy = FieldStrategy.NEVER, whereStrategy = FieldStrategy.NEVER)
    private Integer day;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}