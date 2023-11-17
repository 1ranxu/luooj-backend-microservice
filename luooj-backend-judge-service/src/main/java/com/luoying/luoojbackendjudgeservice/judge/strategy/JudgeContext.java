package com.luoying.luoojbackendjudgeservice.judge.strategy;

import com.luoying.luoojbackendmodel.dto.question.QuestionJudgeCase;
import com.luoying.luoojbackendmodel.dto.questionsubmit.QuestionSubmitJudgeInfo;
import com.luoying.luoojbackendmodel.entity.Question;
import com.luoying.luoojbackendmodel.entity.QuestionSubmit;
import lombok.Data;

import java.util.List;
@Data
public class JudgeContext {

    private List<String> inputList;

    private List<String> outputList;

    private List<QuestionJudgeCase> judgeCaseList;

    private Question question;

    private QuestionSubmitJudgeInfo judgeInfo;

    private QuestionSubmit questionSubmit;
}
