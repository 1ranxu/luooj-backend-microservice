package com.luoying.luoojbackendmodel.vo;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.luoying.luoojbackendmodel.dto.question.QuestionJudgeCconfig;
import com.luoying.luoojbackendmodel.entity.Question;
import lombok.Data;
import org.springframework.beans.BeanUtils;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * @author 落樱的悔恨
 * 题目VO
 */
@Data
public class QuestionVO implements Serializable {
    private final static Gson GSON = new Gson();

    /**
     * 题目id
     */
    private Long id;

    /**
     * 题目标题
     */
    private String title;

    /**
     * 题目内容
     */
    private String content;

    /**
     * 题目难度（0-简单，1-中等，2-困难）
     */
    private Integer difficulty;

    /**
     * 标签列表（json 数组）
     */
    private List<String> tags;

    /**
     * 题目答案
     */
    private String answer;

    /**
     * 题目提交数
     */
    private Long submitNum;

    /**
     * 题目通过数
     */
    private Long acceptedNum;

    /**
     * 判题配置（json对象）
     */
    private QuestionJudgeCconfig judgeConfig;

    /**
     * 评论数
     */
    private Long comments;

    /**
     * 点赞数
     */
    private Long likes;

    /**
     * 创建用户 id
     */
    private Long userId;

    /**
     * 是否通过
     */
    private Integer isAccepted;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新时间
     */
    private Date updateTime;

    /**
     *
     */
    private UserVO userVO;

    /**
     * 包装类转对象
     *
     * @param questionVO {@link QuestionVO}
     */
    public static Question voToObj(QuestionVO questionVO) {
        if (questionVO == null) {
            return null;
        }
        Question question = new Question();
        BeanUtils.copyProperties(questionVO, question);
        List<String> tagList = questionVO.getTags();
        if (tagList != null) {
            question.setTags(GSON.toJson(tagList));
            // question.setTags(JSONUtil.toJsonStr(tagList));
        }
        QuestionJudgeCconfig judgeConfig = questionVO.getJudgeConfig();
        if (judgeConfig != null) {
            question.setJudgeConfig(GSON.toJson(judgeConfig));
            // question.setJudgeConfig(JSONUtil.toJsonStr(judgeConfig));
        }
        return question;
    }

    /**
     * 对象转包装类
     *
     * @param question {@link Question}
     */
    public static QuestionVO objToVo(Question question) {
        if (question == null) {
            return null;
        }
        QuestionVO questionVO = new QuestionVO();
        BeanUtils.copyProperties(question, questionVO);
        questionVO.setTags(GSON.fromJson(question.getTags(), new TypeToken<List<String>>() {
        }.getType()));
        // questionVO.setTags(JSONUtil.toList(question.getTags(), String.class));
        questionVO.setJudgeConfig(GSON.fromJson(question.getJudgeConfig(), new TypeToken<QuestionJudgeCconfig>() {
        }.getType()));
        // questionVO.setJudgeConfig(JSONUtil.toBean(question.getJudgeConfig(), QuestionJudgeCconfig.class));
        return questionVO;
    }
}