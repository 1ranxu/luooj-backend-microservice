package com.luoying.luoojbackendquestionservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luoying.luoojbackendmodel.dto.question_list.QuestionListAddRequest;
import com.luoying.luoojbackendmodel.dto.question_list.QuestionListQueryRequest;
import com.luoying.luoojbackendmodel.dto.question_list.QuestionListUpdateRequest;
import com.luoying.luoojbackendmodel.entity.QuestionList;
import com.luoying.luoojbackendmodel.vo.QuestionListVO;

import javax.servlet.http.HttpServletRequest;

/**
 * @author 落樱的悔恨
 * @description 针对表【question_list(题单)】的数据库操作Service
 * @createDate 2024-09-02 14:30:38
 */
public interface QuestionListService extends IService<QuestionList> {

    /**
     * 创建题单
     *
     * @param questionListAddRequest
     * @param request
     * @return
     */
    Boolean addQuestionList(QuestionListAddRequest questionListAddRequest, HttpServletRequest request);

    /**
     * 删除题单
     *
     * @param id
     * @param request
     * @return
     */
    Boolean deleteQuestionList(Long id, HttpServletRequest request);

    /**
     * 更新题单
     *
     * @param questionListUpdateRequest
     * @param request
     * @return
     */
    Boolean updateQuestionList(QuestionListUpdateRequest questionListUpdateRequest, HttpServletRequest request);

    /**
     * 获取题单
     *
     * @param id
     * @return
     */
    QuestionList getQuestionListById(Long id);

    /**
     * 获取查询条件
     *
     * @param questionListQueryRequest
     * @return
     */
    QueryWrapper<QuestionList> getQueryWrapper(QuestionListQueryRequest questionListQueryRequest);

    /**
     * 分页获取题单（仅管理员）
     *
     * @param questionListQueryRequest
     * @return
     */
    Page<QuestionList> listQuestionListByPage(QuestionListQueryRequest questionListQueryRequest);

    /**
     * 分页获取题单
     *
     * @param questionListQueryRequest
     * @return
     */
    Page<QuestionListVO> listQuestionListByPageUser(QuestionListQueryRequest questionListQueryRequest, HttpServletRequest request);
}
