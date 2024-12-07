package com.luoying.luoojbackendmodel.vo;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.Data;

/**
 * @Author 落樱的悔恨
 * @Date 2024/12/6 23:52
 */
@Data
public class QuestionCollectByUserAllQuestionListDetail {
    private static final long serialVersionUID = 1L;
    /**
     * 用户是否收藏了某道题目
     */
    private Boolean isCollect;

    /**
     * 用户所有题单对某道题目的收藏情况
     */
    private Page<QuestionListVO> questionListVOPage;
}
