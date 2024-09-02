package com.luoying.luoojbackenduserservice.controller;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.luoying.luoojbackendcommon.annotation.AuthCheck;
import com.luoying.luoojbackendcommon.common.*;
import com.luoying.luoojbackendcommon.constant.EmailConstant;
import com.luoying.luoojbackendcommon.constant.RedisKey;
import com.luoying.luoojbackendcommon.constant.UserConstant;
import com.luoying.luoojbackendcommon.exception.BusinessException;
import com.luoying.luoojbackendcommon.exception.ThrowUtils;
import com.luoying.luoojbackendcommon.utils.EmailUtil;
import com.luoying.luoojbackendmodel.dto.user.*;
import com.luoying.luoojbackendmodel.entity.User;
import com.luoying.luoojbackendmodel.vo.LoginUserVO;
import com.luoying.luoojbackendmodel.vo.UserVO;
import com.luoying.luoojbackenduserservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.luoying.luoojbackendcommon.constant.RedisKey.EMAIL_CAPTCHA_KEY;
import static com.luoying.luoojbackendcommon.constant.RedisKey.EMAIL_CAPTCHA_KEY_TTL;
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

    @Resource
    private JavaMailSender mailSender;

    @Resource
    private StringRedisTemplate stringRedisTemplate;


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
     * 邮箱账号注册
     *
     * @param userEmailRegisterRequest 邮箱账号注册请求
     */
    @PostMapping("/email/register")
    public BaseResponse<Long> userEmailRegister(@RequestBody UserEmailRegisterRequest userEmailRegisterRequest) {
        // 判空
        if (userEmailRegisterRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 邮箱账号注册
        long result = userService.userEmailRegister(userEmailRegisterRequest);
        // redis删除验证码缓存
        stringRedisTemplate.delete(RedisKey.getKey(EMAIL_CAPTCHA_KEY, userEmailRegisterRequest.getEmailAccount()));
        return ResultUtils.success(result);
    }

    /**
     * 邮箱账号登录
     *
     * @param userEmailLoginRequest 邮箱账号登录
     * @param request               http请求
     */
    @PostMapping("/email/login")
    public BaseResponse<LoginUserVO> userEmailLogin(@RequestBody UserEmailLoginRequest userEmailLoginRequest, HttpServletRequest request) {
        // 判空
        if (userEmailLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 邮箱账号登录
        LoginUserVO user = userService.userEmailLogin(userEmailLoginRequest, request);
        // redis删除验证码缓存
        stringRedisTemplate.delete(RedisKey.getKey(EMAIL_CAPTCHA_KEY, userEmailLoginRequest.getEmailAccount()));
        return ResultUtils.success(user);
    }

    /**
     * 获取验证码
     *
     * @param emailAccount 邮箱账号
     */
    @GetMapping("/getCaptcha")
    public BaseResponse<Boolean> getCaptcha(@RequestParam("emailAccount") String emailAccount) {
        if (StringUtils.isBlank(emailAccount)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String regex = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
        if (!emailAccount.matches(regex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的邮箱地址");
        }
        // 获取验证码 存入redis
        String captcha = RandomUtil.randomNumbers(6);
        stringRedisTemplate.opsForValue()
                .set(RedisKey.getKey(EMAIL_CAPTCHA_KEY, emailAccount), captcha, EMAIL_CAPTCHA_KEY_TTL, TimeUnit.MINUTES);
        // 发送邮件
        try {
            sendEmail(emailAccount, captcha);
            return ResultUtils.success(true);
        } catch (Exception e) {
            log.error("【发送验证码失败】" + e.getMessage());
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码获取失败");
        }
    }

    private void sendEmail(String emailAccount, String captcha) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        // 邮箱发送内容组成
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        // 邮件主题
        helper.setSubject(EmailConstant.EMAIL_SUBJECT);
        // 正文
        helper.setText(EmailUtil.buildEmailContent(EmailConstant.EMAIL_HTML_CONTENT_PATH, captcha), true);
        // 收件人
        helper.setTo(emailAccount);
        // 发件人
        helper.setFrom(EmailConstant.EMAIL_TITLE + '<' + "1574925401@qq.com" + '>');
        mailSender.send(message);
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
        // 加密
        if (StringUtils.isNotBlank(userAddRequest.getUserPassword())) {
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userAddRequest.getUserPassword()).getBytes());
            user.setUserPassword(encryptPassword);
        }
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


    /**
     * 解封
     *
     * @param idRequest id请求
     */
    @PostMapping("/normal")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> normalUser(@RequestBody IdRequest idRequest) {
        // 校验
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 根据id查询用户
        Long id = idRequest.getId();
        User user = userService.getById(id);
        if (user == null) {// 用户不存在
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 用户角色设置为user
        user.setUserRole(UserConstant.DEFAULT_ROLE);
        return ResultUtils.success(userService.updateById(user));
    }

    /**
     * 封号
     *
     * @param idRequest id请求
     */
    @PostMapping("/ban")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> banUser(@RequestBody IdRequest idRequest) {
        // 校验
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 根据Id查询用户
        Long id = idRequest.getId();
        User user = userService.getById(id);
        if (user == null) {// 用户不存在
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 用户角色设置为ban
        user.setUserRole(UserConstant.BAN_ROLE);
        return ResultUtils.success(userService.updateById(user));
    }

    /**
     * 绑定邮箱
     *
     * @param userBindEmailRequest 绑定邮箱请求
     * @param request              http请求
     */
    @PostMapping("/email/bind")
    public BaseResponse<UserVO> userBindEmail(@RequestBody UserBindEmailRequest userBindEmailRequest, HttpServletRequest request) {
        // 判空
        if (userBindEmailRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        // 绑定邮箱
        UserVO user = userService.userBindEmail(userBindEmailRequest, loginUser);
        return ResultUtils.success(user);
    }

    /**
     * 解除邮箱绑定
     *
     * @param userUnBindEmailRequest 解除邮箱绑定请求
     * @param request                http请求
     */
    @PostMapping("/email/unbind")
    public BaseResponse<UserVO> userUnBindEmail(@RequestBody UserUnBindEmailRequest userUnBindEmailRequest, HttpServletRequest request) {
        // 判空
        if (userUnBindEmailRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取登录用户
        User loginUser = userService.getLoginUser(request);
        // 解除邮箱绑定
        UserVO user = userService.userUnBindEmail(userUnBindEmailRequest, loginUser);
        // 删除验证码缓存
        stringRedisTemplate.delete(RedisKey.getKey(EMAIL_CAPTCHA_KEY, userUnBindEmailRequest.getEmailAccount()));
        return ResultUtils.success(user);
    }
}
