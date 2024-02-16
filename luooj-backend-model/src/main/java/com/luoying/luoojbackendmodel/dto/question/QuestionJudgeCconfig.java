package com.luoying.luoojbackendmodel.dto.question;

import lombok.Data;

/**
 * @author 落樱的悔恨
 * 判题配置
 */
@Data
public class QuestionJudgeCconfig {
    /**
     * 时间限制 ms
     */
    private Long timeLimit;

    /**
     * 内存限制 KB
     */
    private Long memoryLimit;

    /**
     * 堆栈限制 KB
     */
    private Long stackLimit;
}
