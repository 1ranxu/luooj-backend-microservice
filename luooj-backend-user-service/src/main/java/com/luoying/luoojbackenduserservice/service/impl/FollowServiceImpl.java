package com.luoying.luoojbackenduserservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luoying.luoojbackendcommon.constant.RedisKey;
import com.luoying.luoojbackendmodel.entity.Follow;
import com.luoying.luoojbackendmodel.entity.User;
import com.luoying.luoojbackendmodel.vo.UserVO;
import com.luoying.luoojbackenduserservice.mapper.FollowMapper;
import com.luoying.luoojbackenduserservice.service.FollowService;
import com.luoying.luoojbackenduserservice.service.UserService;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.luoying.luoojbackendcommon.constant.RedisKey.FOLLOW_KEY;

/**
 * @author 落樱的悔恨
 * @description 针对表【follow(关注表)】的数据库操作Service实现
 * @createDate 2024-03-16 11:23:33
 */
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow>
        implements FollowService {
    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * @param userId   被操作的人
     * @param isFollow 关注/取关
     * @param request
     * @return
     */
    @Override
    public Boolean follow(Long userId, Boolean isFollow, HttpServletRequest request) {
        // 1.获取登录用户
        User loginUser = userService.getLoginUser(request);
        Long fansId = loginUser.getId();
        String key = RedisKey.getKey(FOLLOW_KEY, fansId);
        updateFansAanFollowers(fansId, userId, isFollow);
        // 2.判断是关注还是取关
        if (isFollow) { // 3.关注，新增数据
            Follow follow = new Follow();
            follow.setFansId(fansId);
            follow.setUserId(userId);
            boolean isSuccess = this.save(follow);
            if (isSuccess) {
                // 把关注用户的id，放入redis的set集合
                stringRedisTemplate.opsForSet().add(key, userId.toString());
            }
            return isSuccess;
        } else { // 取关，删除
            LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Follow::getUserId, userId).eq(Follow::getFansId, fansId);
            boolean isSuccess = this.remove(queryWrapper);
            if (isSuccess) {
                // 把关注用户的id，移除redis的set集合
                stringRedisTemplate.opsForSet().remove(key, userId.toString());
            }
            return isSuccess;
        }
    }

    /**
     * 判断是否关注了某用户
     *
     * @param userId  被操作的人
     * @param request
     * @return
     */
    @Override
    public Boolean isFollow(Long userId, HttpServletRequest request) {
        // 1.获取登录用户
        User loginUser = userService.getLoginUser(request);
        Long fansId = loginUser.getId();
        // 2.查询是否关注
        LambdaQueryWrapper<Follow> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Follow::getUserId, userId).eq(Follow::getFansId, fansId);
        Follow follow = this.getOne(queryWrapper);
        return follow == null ? false : true;
    }

    /**
     * 获取当前用户和被操作的用户的共同关注
     *
     * @param userId  被操作的人
     * @param request
     * @return
     */
    @Override
    public List<UserVO> common(Long userId, HttpServletRequest request) {
        // 1.获取登录用户
        User loginUser = userService.getLoginUser(request);
        Long loginUserId = loginUser.getId();
        // 2.求交集
        String key1 = RedisKey.getKey(FOLLOW_KEY, loginUserId);
        String key2 = RedisKey.getKey(FOLLOW_KEY, userId);
        Set<String> intersect = stringRedisTemplate.opsForSet().intersect(key1, key2);
        if (intersect == null || intersect.isEmpty()) {
            return Collections.emptyList();
        }
        // 3.解析id集合
        return intersect.stream().map(s -> userService.getUserVO(userService.getById(Long.valueOf(s)))).collect(Collectors.toList());
    }

    public void updateFansAanFollowers(Long fansId, Long userId, Boolean isFollow) {
        User fans = userService.getById(fansId);
        User user = userService.getById(userId);
        if (isFollow) {
            fans.setFollowers(fans.getFollowers() + 1);
            user.setFans(user.getFans() + 1);
        } else {
            fans.setFollowers(fans.getFollowers() - 1);
            user.setFans(user.getFans() - 1);
        }
        userService.updateById(fans);
        userService.updateById(user);
    }
}




