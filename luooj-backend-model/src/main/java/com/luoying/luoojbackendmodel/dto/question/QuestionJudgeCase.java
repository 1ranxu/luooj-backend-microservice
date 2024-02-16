package com.luoying.luoojbackendmodel.dto.question;

import lombok.Data;

/**
 * @author 落樱的悔恨
 * 判题用例
 */
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
