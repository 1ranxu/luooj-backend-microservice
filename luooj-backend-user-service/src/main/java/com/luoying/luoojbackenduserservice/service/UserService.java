package com.luoying.luoojbackenduserservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luoying.luoojbackendmodel.dto.user.UserLoginRequest;
import com.luoying.luoojbackendmodel.dto.user.UserQueryRequest;
import com.luoying.luoojbackendmodel.dto.user.UserRegisterRequest;
import com.luoying.luoojbackendmodel.entity.User;
import com.luoying.luoojbackendmodel.vo.LoginUserVO;
import com.luoying.luoojbackendmodel.vo.UserVO;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author 落樱的悔恨
 * 用户服务
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求
     * @return 新用户 id
     */
    long userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求
     * @param request          {@link HttpServletRequest}
     * @return 登录用户信息(脱敏)
     */
    LoginUserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request);

    /**
     * 用户登出
     *
     * @param request {@link HttpServletRequest}
     */
    boolean userLogout(HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request {@link HttpServletRequest}
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     * @param user 用户信息
     * @return 脱敏的用户信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 获取脱敏的用户信息
     *
     * @param user 用户信息（未脱敏）
     * @return 用户信息（脱敏）
     */
    UserVO getUserVO(User user);


    /**
     * 获取查询条件
     *
     * @param userQueryRequest 用户查询请求
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList 用户集合
     */
    List<UserVO> getUserVO(List<User> userList);




    /**
     * 是否为管理员
     *
     * @param request {@link HttpServletRequest}
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * 是否为管理员
     *
     * @param user 用户信息
     */
    boolean isAdmin(User user);
}
