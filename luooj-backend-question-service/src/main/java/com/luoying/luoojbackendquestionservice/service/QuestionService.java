package com.luoying.luoojbackendquestionservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luoying.luoojbackendcommon.common.DeleteRequest;
import com.luoying.luoojbackendmodel.dto.question.QuestionAddRequest;
import com.luoying.luoojbackendmodel.dto.question.QuestionEditRequest;
import com.luoying.luoojbackendmodel.dto.question.QuestionQueryRequest;
import com.luoying.luoojbackendmodel.dto.question.QuestionUpdateRequest;
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
     * 创建题目（仅管理员）
     *
     * @param questionAddRequest
     * @param request
     * @return
     */
    Long addQuestion(QuestionAddRequest questionAddRequest, HttpServletRequest request);

    /**
     * 校验参数
     *
     * @param question 题目
     * @param add      是否为新增
     */
    void validQuestion(Question question, boolean add);

    /**
     * 删除题目（仅管理员）
     *
     * @param deleteRequest
     * @return
     */
    Boolean deleteQuestion(DeleteRequest deleteRequest, HttpServletRequest request);

    /**
     * 点赞题目
     *
     * @param id
     * @param request
     * @return
     */
    Boolean likeQuestion(Long id, HttpServletRequest request);

    /**
     * 更新题目（仅管理员）
     *
     * @param questionUpdateRequest
     * @return
     */
    Boolean updateQuestion(QuestionUpdateRequest questionUpdateRequest);

    /**
     * 根据 id 获取题目（仅管理员）
     *
     * @param id
     * @return
     */
    Question getQuestionById(long id);

    /**
     * 根据 id 获取封装后的题目
     *
     * @param id
     * @param request
     * @return
     */
    QuestionVO getQuestionVOById(long id, HttpServletRequest request);

    /**
     * 分页获取题目列表（仅管理员）
     *
     * @param questionQueryRequest
     * @return
     */
    Page<Question> listQuestionByPage(QuestionQueryRequest questionQueryRequest);

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
     * @param request  {@link HttpServletRequest}
     */
    QuestionVO getQuestionVO(Question question, HttpServletRequest request);

    /**
     * 分页获取封装后的题目
     *
     * @param questionQueryRequest
     * @param request
     * @return
     */
    Page<QuestionVO> getQuestionVOPage(QuestionQueryRequest questionQueryRequest, HttpServletRequest request);

    /**
     * 更新题目（仅管理员）
     *
     * @param questionEditRequest
     * @param request
     * @return
     */
    Boolean editQuestion(QuestionEditRequest questionEditRequest, HttpServletRequest request);

    /**
     * 根据题目id查询题目
     *
     * @param id 题目id
     */
    Question queryById(long id);

    /**
     * 获取上一道题目
     *
     * @param questionId
     * @return
     */
    Long getPrevQuestion(long questionId);

    /**
     * 获取下一道题目
     *
     * @param questionId
     * @return
     */
    Long getNextQuestion(long questionId);

    /**
     * 随机获取一道题目
     *
     * @return
     */
    Long getRandomQuestion();
}
