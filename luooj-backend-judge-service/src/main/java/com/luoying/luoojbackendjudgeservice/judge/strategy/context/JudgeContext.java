package com.luoying.luoojbackendjudgeservice.judge.strategy.context;

import com.luoying.luoojbackendmodel.dto.question.QuestionJudgeCase;
import com.luoying.luoojbackendmodel.dto.question_submit.QuestionSubmitJudgeInfo;
import com.luoying.luoojbackendmodel.entity.Question;
import lombok.Data;

import java.util.List;

/**
 * @author 落樱的悔恨
 * 判题上下文
 */
@Data
public class JudgeContext {

    /**
     * 输入用例
     */
    private List<String> inputList;

    /**
     * 判题结果
     */
    private List<String> outputList;

    /**
     * 判题用例
     */
    private List<QuestionJudgeCase> judgeCaseList;

    /**
     * 题目
     */
    private Question question;

    /**
     * 判题信息
     */
    private QuestionSubmitJudgeInfo judgeInfo;

    /**
     * 编程语言
     */
    private String language;
}
