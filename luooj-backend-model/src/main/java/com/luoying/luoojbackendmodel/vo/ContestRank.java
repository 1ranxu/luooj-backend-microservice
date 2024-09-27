package com.luoying.luoojbackendmodel.vo;

import cn.hutool.core.lang.Pair;
import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/16 14:23
 */
@Data
public class ContestRank implements Serializable {
    /**
     * 参赛用户信息
     */
    private UserVO userVO;

    /**
     * 总得分
     */
    private Integer score;

    /**
     * 完成时间（通过时间和处罚时间的综合）
     */
    private Pair<String, Integer> time;

    /**
     * 参赛详细信息
     * key：题目顺序
     * value：题目详细信息
     */
    private Map<Integer, ContestRankQuestionDetail> submitDetail;

    private static final long serialVersionUID = 1L;
}
