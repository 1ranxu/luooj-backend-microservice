package com.luoying.luoojbackendmodel.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.util.Date;
import lombok.Data;

/**
 * 题解举报表
 * @TableName question_solution_report
 */
@TableName(value ="question_solution_report")
@Data
public class QuestionSolutionReport implements Serializable {
    /**
     * 题解举报记录id
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 检举人id
     */
    private Long userId;

    /**
     * 被检举题解的id
     */
    private Long solutionId;

    /**
     * 被检举人id
     */
    private Long reportedUserId;

    /**
     * 创建时间
     */
    private Date createTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}