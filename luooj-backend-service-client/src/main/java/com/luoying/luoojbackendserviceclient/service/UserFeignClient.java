package com.luoying.luoojbackendserviceclient.service;


import com.luoying.luoojbackendcommon.common.ErrorCode;
import com.luoying.luoojbackendcommon.exception.BusinessException;
import com.luoying.luoojbackendmodel.entity.User;
import com.luoying.luoojbackendmodel.enums.UserRoleEnum;
import com.luoying.luoojbackendmodel.vo.UserVO;
import org.springframework.beans.BeanUtils;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.List;

import static com.luoying.luoojbackendcommon.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author 落樱的悔恨
 * 用户服务
 */
@FeignClient(name = "luooj-backend-user-service", path = "/api/user/inner")
public interface UserFeignClient {

    /**
     * 根据id获取用户
     *
     * @param userId 用户id
     */
    @GetMapping("/get/id")
    User getById(@RequestParam("userId") long userId);

    /**
     * 根据id集合获取用户列表
     * @param ids 用户id集合
     */
    @GetMapping("/get/ids")
    List<User> listByIds(@RequestParam("ids") Collection<Long> ids);

    /**
     * 获取当前登录用户
     *
     * @param request {@link HttpServletRequest}
     */
    default User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }


    /**
     * 是否为管理员
     *
     * @param user {@link User}
     */
    default boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 获取脱敏的用户信息
     *
     * @param user {@link User}
     */
    default UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

}
