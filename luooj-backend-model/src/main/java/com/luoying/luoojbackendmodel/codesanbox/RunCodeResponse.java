package com.luoying.luoojbackendmodel.codesanbox;

import com.luoying.luoojbackendmodel.dto.questionsubmit.QuestionSubmitJudgeInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author 落樱的悔恨
 * 在线运行代码响应
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RunCodeResponse {
    /**
     * 输出
     */
    private String output;

    /**
     * 执行信息
     */
    private String message;

    /**
     * 判题状态
     */
    private Integer status;

    /**
     * 判题信息
     */
    private QuestionSubmitJudgeInfo judgeInfo;
}
