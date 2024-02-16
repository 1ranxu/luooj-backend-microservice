package com.luoying.luoojbackendquestionservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luoying.luoojbackendmodel.dto.question.QuestionQueryRequest;
import com.luoying.luoojbackendmodel.entity.Question;
import com.luoying.luoojbackendmodel.vo.QuestionVO;


import javax.servlet.http.HttpServletRequest;

/**
* @author 落樱的悔恨
* @description 针对表【question(题目)】的数据库操作Service
* @createDate 2023-11-09 16:32:34
*/
public interface QuestionService extends IService<Question> {
    /**
     * 校验参数
     *
     * @param question 题目
     * @param add 是否为新增
     */
    void validQuestion(Question question, boolean add);

    /**
     * 获取查询条件
     *
     * @param questionQueryRequest 题目查询请求
     */
    QueryWrapper<Question> getQueryWrapper(QuestionQueryRequest questionQueryRequest);
    

    /**
     * 获取封装后的题目
     *
     * @param question 题目
     * @param request {@link HttpServletRequest}
     */
    QuestionVO getQuestionVO(Question question, HttpServletRequest request);

    /**
     * 分页获取封装后的题目
     *
     * @param questionPage {@link Page<Question>}
     * @param request {@link HttpServletRequest}
     */
    Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request);
}
