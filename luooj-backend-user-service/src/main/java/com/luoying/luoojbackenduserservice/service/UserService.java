package com.luoying.luoojbackenduserservice.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.luoying.luoojbackendcommon.common.DeleteRequest;
import com.luoying.luoojbackendcommon.common.IdRequest;
import com.luoying.luoojbackendmodel.dto.user.*;
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
     * 注册
     *
     * @param registerRequest
     * @return
     */
    long userEmailRegister(RegisterRequest registerRequest);

    /**
     * 账号登录
     *
     * @param userAccountLoginRequest 用户登录请求
     * @param request                 {@link HttpServletRequest}
     * @return 登录用户信息(脱敏)
     */
    LoginUserVO userLogin(UserAccountLoginRequest userAccountLoginRequest, HttpServletRequest request);

    /**
     * 邮箱账号登录
     *
     * @return 脱敏后的用户信息
     */
    LoginUserVO userEmailLogin(UserEmailLoginRequest userEmailLoginRequest, HttpServletRequest request);

    /**
     * 发送邮件
     *
     * @param email
     * @param captcha
     */
    Boolean sendEmail(String email, String captcha);

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
     *
     * @param user 用户信息
     * @return 脱敏的用户信息
     */
    LoginUserVO getLoginUserVO(User user);

    /**
     * 创建用户
     *
     * @param userAddRequest
     * @return
     */
    Boolean addUser(UserAddRequest userAddRequest);

    /**
     * 删除用户
     *
     * @param deleteRequest
     * @return
     */
    Boolean deleteUser(DeleteRequest deleteRequest);

    /**
     * 更新用户
     *
     * @param userUpdateRequest
     * @param request
     * @return
     */
    Boolean updateUser(UserUpdateRequest userUpdateRequest, HttpServletRequest request);

    /**
     * 用户修改密码
     *
     * @param userPasswordUpdateRequest
     * @param request
     * @return
     */
    Boolean updateUserPassword(UserPasswordUpdateRequest userPasswordUpdateRequest, HttpServletRequest request);

    /**
     * 根据 id 获取用户
     *
     * @param id
     * @return
     */
    User getUserById(long id);

    /**
     * 获取脱敏的用户信息
     *
     * @param user 用户信息（未脱敏）
     * @return 用户信息（脱敏）
     */
    UserVO getUserVO(User user);

    /**
     * 根据 id 获取包装类
     *
     * @param id
     * @return
     */
    UserVO getUserVOById(long id);

    /**
     * 获取查询条件
     *
     * @param userQueryRequest 用户查询请求
     */
    QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest);

    /**
     * 分页获取用户列表（仅管理员）
     *
     * @param userQueryRequest
     * @return
     */
    Page<User> listUserByPage(UserQueryRequest userQueryRequest);

    /**
     * 获取脱敏的用户信息
     *
     * @param userList 用户集合
     */
    List<UserVO> getUserVOList(List<User> userList);

    /**
     * 分页获取脱敏用户封装列表
     *
     * @param userQueryRequest
     * @return
     */
    Page<UserVO> listUserVOByPage(UserQueryRequest userQueryRequest);

    /**
     * 更新个人信息
     *
     * @param userUpdateMyRequest
     * @param request
     * @return
     */
    Boolean updatePersonalInfo(UserUpdateMyRequest userUpdateMyRequest, HttpServletRequest request);

    /**
     * 解封
     *
     * @param idRequest
     * @return
     */
    Boolean normalUser(IdRequest idRequest);

    /**
     * 封号
     *
     * @param idRequest
     * @return
     */
    Boolean banUser(IdRequest idRequest);

    /**
     * 绑定邮箱
     */
    UserVO userBindEmail(UserBindEmailRequest userBindEmailRequest, HttpServletRequest request);

    /**
     * 解除邮箱绑定
     */
    UserVO userUnBindEmail(UserUnBindEmailRequest userUnBindEmailRequest, HttpServletRequest request);
}
