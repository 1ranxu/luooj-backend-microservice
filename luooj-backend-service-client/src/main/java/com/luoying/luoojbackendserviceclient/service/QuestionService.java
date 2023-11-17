package com.luoying.luoojbackendserviceclient.service;



import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luoying.luoojbackendmodel.entity.Question;
import com.luoying.luoojbackendmodel.vo.QuestionVO;

import javax.servlet.http.HttpServletRequest;

/**
* @author 落樱的悔恨
* @description 针对表【question(题目)】的数据库操作Service
* @createDate 2023-11-09 16:32:34
*/
public interface QuestionService  {
    /**
     * 校验
     *
     * @param question
     * @param add
     */
    void validQuestion(Question question, boolean add);

    

    /**
     * 获取题目封装
     *
     * @param question
     * @param request
     * @return
     */
    QuestionVO getQuestionVO(Question question, HttpServletRequest request);

    /**
     * 分页获取题目封装
     *
     * @param questionPage
     * @param request
     * @return
     */
    Page<QuestionVO> getQuestionVOPage(Page<Question> questionPage, HttpServletRequest request);
}
