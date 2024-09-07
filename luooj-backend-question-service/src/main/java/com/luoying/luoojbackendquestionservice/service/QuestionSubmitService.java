package com.luoying.luoojbackendquestionservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luoying.luoojbackendmodel.dto.question_submit.QuestionSubmitAddRequest;
import com.luoying.luoojbackendmodel.dto.question_submit.QuestionSubmitDetail;
import com.luoying.luoojbackendmodel.dto.question_submit.QuestionSubmitQueryRequest;
import com.luoying.luoojbackendmodel.entity.QuestionSubmit;
import com.luoying.luoojbackendmodel.entity.User;
import com.luoying.luoojbackendmodel.vo.QuestionSubmitVO;

import javax.servlet.http.HttpServletRequest;


/**
* @author 落樱的悔恨
* @description 针对表【question_submit(题目提交记录)】的数据库操作Service
* @createDate 2023-11-09 16:32:34
*/
public interface QuestionSubmitService extends IService<QuestionSubmit> {
    /**
     * 提交题目
     *
     * @param questionSubmitAddRequest 题目提交创建请求
     * @param loginUser 登录用户
     * @return 提交记录id
     */
    long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, User loginUser);

    /**
     * 获取查询条件
     *
     * @param questionSubmitQueryRequest 题目提交查询请求
     */
    QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest);


    /**
     * 获取封装后的题目提交
     *
     * @param questionSubmit 题目提交
     * @param loginUser 登录用户
     */
    QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser);

    /**
     * 分页获取封装后的题目提交
     *
     * @param questionSubmitPage {@link Page<QuestionSubmit>}
     * @param loginUser 登录用户
     */
    Page<QuestionSubmitVO> getQuestionSubmitVOPage(Page<QuestionSubmit> questionSubmitPage, User loginUser);

    /**
     * 获取个人提交详情
     * @param request
     * @return
     */
    QuestionSubmitDetail getPersonSubmitDetail(HttpServletRequest request);
}
