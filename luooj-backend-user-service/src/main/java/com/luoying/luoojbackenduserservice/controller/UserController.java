package com.luoying.luoojbackenduserservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luoying.luoojbackendcommon.annotation.AuthCheck;
import com.luoying.luoojbackendcommon.common.BaseResponse;
import com.luoying.luoojbackendcommon.common.DeleteRequest;
import com.luoying.luoojbackendcommon.common.ErrorCode;
import com.luoying.luoojbackendcommon.common.ResultUtils;
import com.luoying.luoojbackendcommon.constant.UserConstant;
import com.luoying.luoojbackendcommon.exception.BusinessException;
import com.luoying.luoojbackendcommon.exception.ThrowUtils;
import com.luoying.luoojbackendmodel.dto.user.*;
import com.luoying.luoojbackendmodel.entity.User;
import com.luoying.luoojbackendmodel.vo.LoginUserVO;
import com.luoying.luoojbackendmodel.vo.UserVO;
import com.luoying.luoojbackenduserservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.luoying.luoojbackendcommon.constant.UserConstant.SALT;

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
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求
     * @return 用户id
     */
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        // 判空
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 注册
        long result = userService.userRegister(userRegisterRequest);
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求
     * @param request          {@link HttpServletRequest}
     * @return {@link BaseResponse<LoginUserVO> 登录用户信息(脱敏)}
     */
    @PostMapping("/login")
    public BaseResponse<LoginUserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        // 判空
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 登录
        LoginUserVO loginUserVO = userService.userLogin(userLoginRequest, request);
        return ResultUtils.success(loginUserVO);
    }


    /**
     * 用户登出
     *
     * @param request {@link HttpServletRequest}
     */
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        // 判空
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //登出
        boolean result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    /**
     * 获取当前登录用户
     *
     * @param request {@link HttpServletRequest}
     * @return {@link BaseResponse<LoginUserVO> 登录用户信息(脱敏)}
     */
    @GetMapping("/get/login")
    public BaseResponse<LoginUserVO> getLoginUser(HttpServletRequest request) {
        User user = userService.getLoginUser(request);
        return ResultUtils.success(userService.getLoginUserVO(user));
    }

    // endregion

    // region 增删改查

    /**
     * 创建用户
     *
     * @param userAddRequest 创建用户请求
     * @param request        {@link HttpServletRequest}
     * @return 用户id
     */
    @PostMapping("/add")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Long> addUser(@RequestBody UserAddRequest userAddRequest, HttpServletRequest request) {
        // 校验
        if (userAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 拷贝
        User user = new User();
        BeanUtils.copyProperties(userAddRequest, user);
        // 保存
        boolean result = userService.save(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(user.getId());
    }

    /**
     * 删除用户
     *
     * @param deleteRequest 删除用户请求
     * @param request       {@link HttpServletRequest}
     */
    @PostMapping("/delete")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> deleteUser(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        // 校验
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 删除
        boolean b = userService.removeById(deleteRequest.getId());
        return ResultUtils.success(b);
    }

    /**
     * 更新用户
     *
     * @param userUpdateRequest 更新用户请求
     * @param request           {@link HttpServletRequest}
     */
    @PostMapping("/update")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> updateUser(@RequestBody UserUpdateRequest userUpdateRequest,
                                            HttpServletRequest request) {
        // 校验
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        if (!userUpdateRequest.getId().equals(loginUser.getId()) && !UserConstant.ADMIN_ROLE.equals(loginUser.getUserRole())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR, "只有本人或管理员可以修改");
        }
        // 拷贝
        User user = new User();
        BeanUtils.copyProperties(userUpdateRequest, user);
        // 加密
        if (StringUtils.isNotBlank(userUpdateRequest.getUserPassword())) {
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userUpdateRequest.getUserPassword()).getBytes());
            user.setUserPassword(encryptPassword);
        }
        // 更新
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 根据 id 获取用户
     *
     * @param id      用户id
     * @param request {@link HttpServletRequest}
     * @return 用户信息(未脱敏)
     */
    @GetMapping("/get")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<User> getUserById(long id, HttpServletRequest request) {
        // 校验
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 查询
        User user = userService.getById(id);
        ThrowUtils.throwIf(user == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(user);
    }

    /**
     * 根据 id 获取包装类
     *
     * @param id      用户id
     * @param request {@link HttpServletRequest}
     * @return 用户信息(脱敏)
     */
    @GetMapping("/get/vo")
    public BaseResponse<UserVO> getUserVOById(long id, HttpServletRequest request) {
        BaseResponse<User> response = getUserById(id, request);
        User user = response.getData();
        return ResultUtils.success(userService.getUserVO(user));
    }

    /**
     * 分页获取用户列表（仅管理员）
     *
     * @param userQueryRequest 分页获取用户列表请求
     * @param request          {@link HttpServletRequest}
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<User>> listUserByPage(@RequestBody UserQueryRequest userQueryRequest,
                                                   HttpServletRequest request) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        Page<User> userPage = userService.page(new Page<>(current, size),
                userService.getQueryWrapper(userQueryRequest));
        return ResultUtils.success(userPage);
    }

    /**
     * 分页获取脱敏用户封装列表
     *
     * @param userQueryRequest 分页获取用户列表请求
     * @param request          {@link HttpServletRequest}
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<UserVO>> listUserVOByPage(@RequestBody UserQueryRequest userQueryRequest,
                                                       HttpServletRequest request) {
        // 判空
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(pageSize > 50, ErrorCode.PARAMS_ERROR);
        // 分页查询
        Page<User> userPage = userService.page(new Page<>(current, pageSize),
                userService.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());
        // 脱敏
        List<UserVO> userVO = userService.getUserVO(userPage.getRecords());
        userVOPage.setRecords(userVO);
        // 返回
        return ResultUtils.success(userVOPage);
    }

    // endregion

    /**
     * 更新个人信息
     *
     * @param userUpdateMyRequest 用户个人信息更新请求
     * @param request
     * @return
     */
    @PostMapping("/update/my")
    public BaseResponse<Boolean> updateMyUser(@RequestBody UserUpdateMyRequest userUpdateMyRequest,
                                              HttpServletRequest request) {
        // 判空
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);

        // 拷贝
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);

        // 加密
        if (StringUtils.isNotBlank(userUpdateMyRequest.getUserPassword())) {
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userUpdateMyRequest.getUserPassword()).getBytes());
            user.setUserPassword(encryptPassword);
        }
        user.setId(loginUser.getId());

        // 更新
        boolean result = userService.updateById(user);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }
}
