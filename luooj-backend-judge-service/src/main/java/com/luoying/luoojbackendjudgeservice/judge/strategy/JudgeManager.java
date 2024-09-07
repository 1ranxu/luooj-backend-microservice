package com.luoying.luoojbackendjudgeservice.judge.strategy;

import com.luoying.luoojbackendjudgeservice.judge.strategy.impl.DefaultJudgeStrategy;
import com.luoying.luoojbackendjudgeservice.judge.strategy.impl.JavaJudgeStrategy;
import com.luoying.luoojbackendmodel.dto.question_submit.QuestionSubmitJudgeInfo;
import com.luoying.luoojbackendmodel.entity.QuestionSubmit;
import org.springframework.stereotype.Service;

/**
 * @author 落樱的悔恨
 * 判题管理（简化代码）
 */
@Service
public class JudgeManager {

    public QuestionSubmitJudgeInfo doJudge(JudgeContext context) {
        // 根据判题上下文获取编程语言
        QuestionSubmit questionSubmit = context.getQuestionSubmit();
        String language = questionSubmit.getLanguage();
        // 根据编程语言获取判题策略
        JudgeStrategy judgeStrategy = new DefaultJudgeStrategy();
        if ("java".equals(language)) {
            judgeStrategy = new JavaJudgeStrategy();
        }
        // 判题
        return judgeStrategy.doJudge(context);
    }
}
