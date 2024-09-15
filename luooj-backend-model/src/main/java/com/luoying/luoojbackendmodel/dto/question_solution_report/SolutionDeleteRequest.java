package com.luoying.luoojbackendmodel.dto.question_solution_report;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/15 17:43
 */
@Data
public class SolutionDeleteRequest implements Serializable {
    /**
     * 题解举报记录id
     */
    private Long id;

    /**
     * 被检举题解的id
     */
    private Long solutionId;

    private static final long serialVersionUID = 1L;
}
