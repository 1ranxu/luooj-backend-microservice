package com.luoying.luoojbackendquestionservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luoying.luoojbackendcommon.annotation.AuthCheck;
import com.luoying.luoojbackendcommon.common.BaseResponse;
import com.luoying.luoojbackendcommon.common.DeleteRequest;
import com.luoying.luoojbackendcommon.common.ResultUtils;
import com.luoying.luoojbackendcommon.constant.UserConstant;
import com.luoying.luoojbackendmodel.dto.accepted_question.AcceptedQuestionQueryRequest;
import com.luoying.luoojbackendmodel.entity.AcceptedQuestion;
import com.luoying.luoojbackendmodel.vo.AcceptedQuestionDetailVO;
import com.luoying.luoojbackendquestionservice.service.AcceptedQuestionService;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @Author 落樱的悔恨
 * @Date 2024/9/8 13:42
 */
@RestController
@RequestMapping("/accepted_question")
public class AcceptedQuestionController {
    @Resource
    private AcceptedQuestionService acceptedQuestionService;

    /**
     * 删除通过记录（仅管理员）
     *
     * @param deleteRequest 删除请求
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteAcceptedQuestion(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        return ResultUtils.success(acceptedQuestionService.deleteAcceptedQuestion(deleteRequest, request));
    }

    /**
     * 分页获取通过记录（仅管理员）
     *
     * @param acceptedQuestionQueryRequest 题目查询请求
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<AcceptedQuestion>> listAcceptedQuestionByPage(@RequestBody AcceptedQuestionQueryRequest acceptedQuestionQueryRequest) {
        return ResultUtils.success(acceptedQuestionService.listAcceptedQuestionByPage(acceptedQuestionQueryRequest));
    }

    /**
     * 获取用户通过题目的详情
     *
     * @param request
     * @return
     */
    @GetMapping("/get/user_accepted_question/detail")
    public BaseResponse<AcceptedQuestionDetailVO> getAcceptedQuestionDetail(@RequestParam("userId") Long userId, HttpServletRequest request) {
        return ResultUtils.success(acceptedQuestionService.getAcceptedQuestionDetail(userId, request));
    }

    /**
     * 获取用户的排名（通过题目数量）
     * @param userId
     * @param request
     * @return
     */
    @GetMapping("/get/user_accepted_question/ranking")
    public BaseResponse<Long> getAcceptedQuestionRanking(@RequestParam("userId") Long userId,HttpServletRequest request) {
        return ResultUtils.success(acceptedQuestionService.getAcceptedQuestionRanking(userId,request));
    }

    /**
     * 判断当前用户是否通过了某道题目
     *
     * @param questionId
     * @param request
     * @return
     */
    @GetMapping("/isAccepted")
    public BaseResponse<Boolean> isAccepted(@RequestParam("questionId") Long questionId, HttpServletRequest request) {
        return ResultUtils.success(acceptedQuestionService.isAccepted(questionId, request));
    }
}
