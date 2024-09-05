package com.luoying.luoojbackendmodel.dto.questionsubmit;

import lombok.Data;
import org.redisson.misc.Hash;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;

/**
 * 个人提交详情
 * @Author 落樱的悔恨
 * @Date 2024/9/5 19:11
 */
@Data
public class QuestionSubmitDetail implements Serializable {
    /**
     * 某年提交总次数
     */
    private HashMap<String,Integer> years;
    /**
     * 某天提交总次数（yyyy-MM-dd）提交了多少次
     */
    private HashMap<String, Integer> submitDetail;

    /**
     * 某年提交总天数
     */
    private HashMap<String, Integer> dayNum;

    private static final long serialVersionUID = 1L;
}
