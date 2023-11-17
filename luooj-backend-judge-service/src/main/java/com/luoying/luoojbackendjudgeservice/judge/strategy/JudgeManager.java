package com.luoying.luoojbackendjudgeservice.judge.strategy;

import com.luoying.luoojbackendjudgeservice.judge.strategy.impl.DefaultJudgeStrategy;
import com.luoying.luoojbackendjudgeservice.judge.strategy.impl.JavaJudgeStrategy;
import com.luoying.luoojbackendmodel.dto.questionsubmit.QuestionSubmitJudgeInfo;
import com.luoying.luoojbackendmodel.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

/**
 * 判题管理（简化代码）
 */
@Service
public class JudgeManager {

    public QuestionSubmitJudgeInfo doJudge(JudgeContext context) {
        QuestionSubmit questionSubmit = context.getQuestionSubmit();
        String language = questionSubmit.getLanguage();
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        if ("java".equals(language)) {
            judgeStrategy = new JavaJudgeStrategy();
        }
        return judgeStrategy.doJudge(context);
    }
}
