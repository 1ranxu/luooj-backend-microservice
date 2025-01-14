package com.luoying.luoojbackenduserservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luoying.luoojbackendcommon.annotation.AuthCheck;
import com.luoying.luoojbackendcommon.common.*;
import com.luoying.luoojbackendcommon.constant.UserConstant;
import com.luoying.luoojbackendcommon.exception.BusinessException;
import com.luoying.luoojbackendmodel.dto.user.*;
import com.luoying.luoojbackendmodel.entity.User;
import com.luoying.luoojbackendmodel.vo.LoginUserVO;
import com.luoying.luoojbackendmodel.vo.UserVO;
import com.luoying.luoojbackenduserservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author 落樱的悔恨
 * 用户接口
 */
@RestController
@RequestMapping("/")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;


    // region 登录相关

    /**
     * 注册
     *
     * @param registerRequest 邮箱账号注册请求
     */
    @PostMapping("/register")
    public BaseResponse<Long> userEmailRegister(@RequestBody RegisterRequest registerRequest) {
        return ResultUtils.success(userService.userEmailRegister(registerRequest));
    }

    /**
     * 账号登录
     *
     * @param userAccountLoginRequest 账号登录请求
     * @param request                 {@link HttpServletRequest}
     * @return {@link BaseResponse<LoginUserVO> 登录用户信息(脱敏)}
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserAccountLoginRequest userAccountLoginRequest, HttpServletRequest request) {
        return ResultUtils.success(userService.userLogin(userAccountLoginRequest, request));
    }

    /**
     * 邮箱登录
     *
     * @param userEmailLoginRequest 邮箱登录
     * @param request               http请求
     */
    @PostMapping("/email/login")
    public BaseResponse<LoginUserVO> userEmailLogin(@RequestBody UserEmailLoginRequest userEmailLoginRequest, HttpServletRequest request) {
        // 邮箱账号登录
        return ResultUtils.success(userService.userEmailLogin(userEmailLoginRequest, request));
    }

    /**
     * 获取验证码
     *
     * @param email 邮箱账号
     */
    @GetMapping("/getCaptcha")
    public BaseResponse<Boolean> getCaptcha(@RequestParam("email") String email, @RequestParam("op") String op) {
        try {
            return ResultUtils.success(userService.sendEmail(email, op));
        } catch (Exception e) {
            log.error("【发送验证码失败】" + e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码获取失败");
        }
    }


    /**
     * 用户登出
     *
     * @param request {@link HttpServletRequest}
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        return ResultUtils.success(userService.userLogout(request));
    }

    /**
     * 获取当前登录用户
     *
     * @param request {@link HttpServletRequest}
     * @return {@link BaseResponse<LoginUserVO> 登录用户信息(脱敏)}
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        return ResultUtils.success(userService.getLoginUserVO(userService.getLoginUser(request)));
    }

    // endregion

    /**
     * 创建用户
     *
     * @param userAddRequest 创建用户请求
     * @return 用户id
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> addUser(@RequestBody UserAddRequest userAddRequest) {
        return ResultUtils.success(userService.addUser(userAddRequest));
    }

    /**
     * 删除用户
     *
     * @param deleteRequest 删除用户请求
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest) {
        return ResultUtils.success(userService.deleteUser(deleteRequest));
    }

    /**
     * 更新用户
     *
     * @param userUpdateRequest 更新用户请求
     * @param request           {@link HttpServletRequest}
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        return ResultUtils.success(userService.updateUser(userUpdateRequest, request));
    }

    /**
     * 用户修改密码
     *
     * @param userPasswordUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update/password")
    public BaseResponse<Boolean> updateUserPassword(@RequestBody UserPasswordUpdateRequest userPasswordUpdateRequest, HttpServletRequest request) {
        return ResultUtils.success(userService.updateUserPassword(userPasswordUpdateRequest, request));
    }

    /**
     * 根据 id 获取用户
     *
     * @param id 用户id
     * @return 用户信息(未脱敏)
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id) {
        return ResultUtils.success(userService.getUserById(id));
    }

    /**
     * 根据 id 获取包装类
     *
     * @param id 用户id
     * @return 用户信息(脱敏)
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id) {
        return ResultUtils.success(userService.getUserVOById(id));
    }

    /**
     * 分页获取用户列表（仅管理员）
     *
     * @param userQueryRequest 分页获取用户列表请求
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest) {
        return ResultUtils.success(userService.listUserByPage(userQueryRequest));
    }

    /**
     * 分页获取脱敏用户封装列表
     *
     * @param userQueryRequest 分页获取用户列表请求
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest) {
        return ResultUtils.success(userService.listUserVOByPage(userQueryRequest));
    }

    /**
     * 更新个人信息
     *
     * @param userUpdateMyRequest 用户个人信息更新请求
     * @param request
     * @return
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updatePersonalInfo(@RequestBody UserUpdateMyRequest userUpdateMyRequest, HttpServletRequest request) {
        return ResultUtils.success(userService.updatePersonalInfo(userUpdateMyRequest, request));
    }


    /**
     * 解封
     *
     * @param idRequest id请求
     */
    @PostMapping("/normal")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> normalUser(@RequestBody IdRequest idRequest) {
        return ResultUtils.success(userService.normalUser(idRequest));
    }

    /**
     * 封号
     *
     * @param idRequest id请求
     */
    @PostMapping("/ban")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> banUser(@RequestBody IdRequest idRequest) {
        return ResultUtils.success(userService.banUser(idRequest));
    }

    /**
     * 绑定邮箱
     *
     * @param userBindEmailRequest 绑定邮箱请求
     */
    @PostMapping("/email/bind")
    public BaseResponse<UserVO> userBindEmail(@RequestBody UserBindEmailRequest userBindEmailRequest, HttpServletRequest request) {
        return ResultUtils.success(userService.userBindEmail(userBindEmailRequest, request));
    }

    /**
     * 解除邮箱绑定
     *
     * @param userUnBindEmailRequest 解除邮箱绑定请求
     * @param request                http请求
     */
    @PostMapping("/email/unbind")
    public BaseResponse<UserVO> userUnBindEmail(@RequestBody UserUnBindEmailRequest userUnBindEmailRequest, HttpServletRequest request) {
        return ResultUtils.success(userService.userUnBindEmail(userUnBindEmailRequest, request));
    }
}
