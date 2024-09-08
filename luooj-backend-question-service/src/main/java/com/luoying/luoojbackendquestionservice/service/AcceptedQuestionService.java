package com.luoying.luoojbackendquestionservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.luoying.luoojbackendmodel.entity.AcceptedQuestion;

import javax.servlet.http.HttpServletRequest;

/**
* @author 落樱的悔恨
* @description 针对表【accepted_question(题目通过表)】的数据库操作Service
* @createDate 2024-09-02 14:54:22
*/
public interface AcceptedQuestionService extends IService<AcceptedQuestion> {

    /**
     * 判断当前用户是否通过了某道题目
     * @param questionId
     * @param request
     * @return
     */
    Boolean isisAccepted(Long questionId, HttpServletRequest request);

}
