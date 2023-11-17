package com.luoying.luoojbackendmodel.dto.question;

import lombok.Data;

@Data
public class QuestionJudgeCase {
    /**
     * 输入用例
     */
    private String input;

    /**
     * 输出用例
     */
    private String output;
}
