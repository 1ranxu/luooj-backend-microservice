package com.luoying.luoojbackendmodel.dto.question_solution_collect;

import com.luoying.luoojbackendcommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/10 20:53
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionSolutionCollectQueryRequest extends PageRequest implements Serializable {
    /**
     * 题解收藏记录id
     */
    private Long id;

    /**
     * 题解id
     */
    private Long solutionId;

    /**
     * 收藏人id
     */
    private Long userId;

    private static final long serialVersionUID = 1L;
}
