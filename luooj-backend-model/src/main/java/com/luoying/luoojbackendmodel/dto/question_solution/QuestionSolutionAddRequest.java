package com.luoying.luoojbackendmodel.dto.question_solution;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 题解创建请求
 * @Author 落樱的悔恨
 * @Date 2024/9/7 20:21
 */
@Data
public class QuestionSolutionAddRequest implements Serializable {
    /**
     * 题目id
     */
    private Long questionId;

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
