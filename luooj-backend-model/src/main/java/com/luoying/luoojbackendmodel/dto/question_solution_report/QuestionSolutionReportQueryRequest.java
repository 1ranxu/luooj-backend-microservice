package com.luoying.luoojbackendmodel.dto.question_solution_report;

import com.luoying.luoojbackendcommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/15 17:38
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionSolutionReportQueryRequest extends PageRequest implements Serializable {
    /**
     * 题解举报记录id
     */
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

    private static final long serialVersionUID = 1L;
}
