package com.luoying.luoojbackendmodel.dto.contest;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;
import java.util.Map;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/16 15:02
 */
@Data
public class ContestAddRequest implements Serializable {
    /**
     * 竞赛标题
     */
    private String title;

    /**
     * 开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date startTime;

    /**
     * 结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    private Date endTime;

    /**
     * 比赛时长（秒）
     */
    private Integer duration;

    /**
     * 竞赛奖励
     */
    private String award;

    /**
     * 重要提示
     */
    private String tips;

    /**
     * 题目列表（json 数组）
     * key：题目顺序
     * value：竞赛题目
     */
    private Map<Integer, ContestQuestion> questions;

    /**
     * 状态（0-待开始、1-进行中、2-已结束）
     */
    private Integer status;

    private static final long serialVersionUID = 1L;
}
