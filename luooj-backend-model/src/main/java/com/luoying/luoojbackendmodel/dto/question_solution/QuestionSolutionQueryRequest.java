package com.luoying.luoojbackendmodel.dto.question_solution;

import com.luoying.luoojbackendcommon.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * @author 落樱的悔恨
 * 题目查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QuestionSolutionQueryRequest extends PageRequest implements Serializable {
    /**
     * 题解id
     */
    private Long id;

    /**
     * 题目id
     */
    private Long questionId;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 题解标题
     */
    private String title;

    /**
     * 题解内容
     */
    private String content;

    /**
     * 标签列表（json 数组）
     */
    private List<String> tags;

    private static final long serialVersionUID = 1L;
}