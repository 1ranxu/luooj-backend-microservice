package com.luoying.luoojbackendmodel.dto.question;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * 创建请求
 *
 */
@Data
public class QuestionAddRequest implements Serializable {
    /**
     * 题目标题
     */
    private String title;

    /**
     * 题目内容
     */
    private String content;

    /**
     * 标签列表（json 数组）
     */
    private List<String> tags;

    /**
     * 题目答案
     */
    private String answer;

    /**
     * 判题配置（json对象）
     */
    private QuestionJudgeCconfig judgeConfig;

    /**
     * 判题用例（json数组）
     */
    private List<QuestionJudgeCase> judgeCaseList;

    private static final long serialVersionUID = 1L;
}