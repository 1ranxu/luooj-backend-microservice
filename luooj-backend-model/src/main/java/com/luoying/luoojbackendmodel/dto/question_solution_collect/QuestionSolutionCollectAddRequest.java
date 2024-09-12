package com.luoying.luoojbackendmodel.dto.question_solution_collect;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/10 19:56
 */
@Data
public class QuestionSolutionCollectAddRequest implements Serializable {
    /**
     * 题解id
     */
    private Long solutionId;

    private static final long serialVersionUID = 1L;
}
