package com.luoying.luoojbackenduserservice.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luoying.luoojbackendmodel.dto.follow.FansQueryRequest;
import com.luoying.luoojbackendmodel.dto.follow.FollowQueryRequest;
import com.luoying.luoojbackendmodel.entity.Follow;
import com.luoying.luoojbackendmodel.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 落樱的悔恨
 * @description 针对表【follow(关注表)】的数据库操作Service
 * @createDate 2024-03-16 11:23:33
 */
public interface FollowService extends IService<Follow> {
    /**
     * 关注或者取关
     *
     * @param userId   被操作的人
     * @param isFollow 关注/取关
     * @param request
     * @return
     */
    Boolean follow(Long userId, Boolean isFollow, HttpServletRequest request);

    /**
     * 判断是否关注了某用户
     *
     * @param userId  被操作的人
     * @param request
     * @return
     */
    Boolean isFollow(Long userId, HttpServletRequest request);

    /**
     * 获取当前用户和被操作的用户的共同关注
     *
     * @param userId  被操作的人
     * @param request
     * @return
     */
    List<UserVO> common(Long userId, HttpServletRequest request);

    /**
     * 获取当前登录用户的关注列表
     * @param followQueryRequest
     * @param request
     * @return
     */
    Page<UserVO> getFollowPage(FollowQueryRequest followQueryRequest, HttpServletRequest request);

    /**
     * 获取当前登录用户的粉丝列表
     * @param fansQueryRequest
     * @param request
     * @return
     */
    Page<UserVO> getFansPage(FansQueryRequest fansQueryRequest, HttpServletRequest request);
}
