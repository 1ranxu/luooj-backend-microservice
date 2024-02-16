package com.luoying.luoojbackendmodel.dto.questionsubmit;

import lombok.Data;

/**
 * @author 落樱的悔恨
 * 题目提交判题信息
 */
@Data
public class QuestionSubmitJudgeInfo {
    /**
     * 程序执行信息
     */
    private String message;

    /**
     * 内存消耗 KB
     */
    private Long memory;

    /**
     * 时间消耗 ms
     */
    private Long time;
}
