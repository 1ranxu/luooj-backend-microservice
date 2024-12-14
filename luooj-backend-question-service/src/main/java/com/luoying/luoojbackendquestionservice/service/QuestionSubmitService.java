package com.luoying.luoojbackendquestionservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luoying.luoojbackendmodel.codesanbox.RunCodeRequest;
import com.luoying.luoojbackendmodel.codesanbox.RunCodeResponse;
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
     * @param questionSubmitAddRequest 题目提交创建请求
     * @param request
     * @return
     */
    long doQuestionSubmit(QuestionSubmitAddRequest questionSubmitAddRequest, HttpServletRequest request);

    /**
     * 获取查询条件
     *
     * @param questionSubmitQueryRequest 题目提交查询请求
     */
    QueryWrapper<QuestionSubmit> getQueryWrapper(QuestionSubmitQueryRequest questionSubmitQueryRequest);

    /**
     * 分页获取封装后的题目提交
     * @param questionSubmitQueryRequest
     * @param request
     * @return
     */
    Page<QuestionSubmitVO> getQuestionSubmitVOPage(QuestionSubmitQueryRequest questionSubmitQueryRequest, HttpServletRequest request);

    /**
     * 在线运行代码
     * @param runCodeRequest 运行代码请求
     * @return
     */
    RunCodeResponse questionRunOnline(RunCodeRequest runCodeRequest);


    /**
     * 获取个人提交详情
     * @param userId
     * @param request
     * @return
     */
    QuestionSubmitDetail getPersonSubmitDetail(Long userId, HttpServletRequest request);

    /**
     * 获取封装后的题目提交
     *
     * @param questionSubmit 题目提交
     * @param loginUser 登录用户
     */
    QuestionSubmitVO getQuestionSubmitVO(QuestionSubmit questionSubmit, User loginUser);







}
