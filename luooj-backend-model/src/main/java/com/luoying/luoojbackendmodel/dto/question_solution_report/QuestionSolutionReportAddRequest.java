package com.luoying.luoojbackendmodel.dto.question_solution_report;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/15 17:38
 */
@Data
public class QuestionSolutionReportAddRequest implements Serializable {
    /**
     * 被检举题解的id
     */
    private Long solutionId;

    /**
     * 被检举人id
     */
    private Long reportedUserId;

    private static final long serialVersionUID = 1L;
}
