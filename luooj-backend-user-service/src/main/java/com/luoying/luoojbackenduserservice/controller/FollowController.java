package com.luoying.luoojbackenduserservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luoying.luoojbackendcommon.common.BaseResponse;
import com.luoying.luoojbackendcommon.common.ResultUtils;
import com.luoying.luoojbackendmodel.dto.follow.FansQueryRequest;
import com.luoying.luoojbackendmodel.dto.follow.FollowQueryRequest;
import com.luoying.luoojbackendmodel.vo.UserVO;
import com.luoying.luoojbackenduserservice.service.FollowService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 落樱的悔恨
 * 关注接口
 */
@RestController
@RequestMapping("/follow")
@Slf4j
public class FollowController {


    @Resource
    private FollowService followService;

    /**
     * 关注或者取关
     *
     * @param userId   被操作的人
     * @param isFollow 关注/取关
     * @param request
     * @return
     */
    @PostMapping("")
    public BaseResponse<Boolean> follow(Long userId, Boolean isFollow, HttpServletRequest request) {
        return ResultUtils.success(followService.follow(userId, isFollow, request));
    }

    /**
     * 判断是否关注了某用户
     *
     * @param userId  被操作的人
     * @param request
     * @return
     */
    @GetMapping("/or/not")
    public BaseResponse<Boolean> isFollow(Long userId, HttpServletRequest request) {
        return ResultUtils.success(followService.isFollow(userId, request));
    }

    /**
     * 获取当前用户和被操作的用户的共同关注
     *
     * @param userId  被操作的人
     * @param request
     * @return
     */
    @GetMapping("/common")
    public BaseResponse<List<UserVO>> common(Long userId, HttpServletRequest request) {
        return ResultUtils.success(followService.common(userId, request));
    }

    /**
     * 获取当前登录用户的关注列表
     * @param followQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/get/follow/page")
    public BaseResponse<Page<UserVO>> getFollowPage(FollowQueryRequest followQueryRequest, HttpServletRequest request) {
        return ResultUtils.success(followService.getFollowPage(followQueryRequest, request));
    }

    /**
     * 获取当前登录用户的粉丝列表
     * @param fansQueryRequest
     * @param request
     * @return
     */
    @GetMapping("/get/fans/page")
    public BaseResponse<Page<UserVO>> getFansPage(FansQueryRequest fansQueryRequest, HttpServletRequest request) {
        return ResultUtils.success(followService.getFansPage(fansQueryRequest, request));
    }


}
