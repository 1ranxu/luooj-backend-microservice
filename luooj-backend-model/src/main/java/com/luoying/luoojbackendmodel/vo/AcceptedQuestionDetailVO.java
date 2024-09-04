package com.luoying.luoojbackendmodel.vo;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/3 21:08
 */
@Data
public class AcceptedQuestionDetailVO implements Serializable {
    /**
     * 通过总数
     */
    private Integer passTotalNum;

    /**
     * 各难度的通过数
     */
    private HashMap<Integer,Integer> eachDifficultyPassNum;

    /**
     * 题目总数
     */
    private Integer questionTotalNum;

    /**
     * 各难度的题目数
     */
    private HashMap<Integer,Integer> eachDifficultyQuestionNum;

    /**
     * 总提交通过率
     */
    private Double submissionPassRate;

    /**
     * 各难度的提交通过率
     */
    private HashMap<Integer,Double> eachDifficultysubmissionPassRate;

    private static final long serialVersionUID = 1L;
}
