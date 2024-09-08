package com.luoying.luoojbackendquestionservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luoying.luoojbackendmodel.entity.AcceptedQuestion;
import com.luoying.luoojbackendmodel.entity.User;
import com.luoying.luoojbackendquestionservice.mapper.AcceptedQuestionMapper;
import com.luoying.luoojbackendquestionservice.service.AcceptedQuestionService;
import com.luoying.luoojbackendserviceclient.service.UserFeignClient;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author 落樱的悔恨
 * @description 针对表【accepted_question(题目通过表)】的数据库操作Service实现
 * @createDate 2024-09-02 14:54:22
 */
@Service
public class AcceptedQuestionServiceImpl extends ServiceImpl<AcceptedQuestionMapper, AcceptedQuestion>
        implements AcceptedQuestionService {

    @Resource
    private UserFeignClient userFeignClient;

    /**
     * 判断当前用户是否通过了某道题目
     * @param questionId
     * @param request
     * @return
     */
    @Override
    public Boolean isisAccepted(Long questionId, HttpServletRequest request) {
        // 获取登录用户
        User loginUser = userFeignClient.getLoginUser(request);
        // 构造条件
        LambdaQueryWrapper<AcceptedQuestion> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(AcceptedQuestion::getQuestionId, questionId);
        queryWrapper.eq(AcceptedQuestion::getUserId, loginUser.getId());
        // 查询
        return this.getOne(queryWrapper) != null;
    }
}




