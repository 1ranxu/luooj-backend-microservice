package com.luoying.luoojbackenduserservice.service.impl;

import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luoying.luoojbackendcommon.common.DeleteRequest;
import com.luoying.luoojbackendcommon.common.ErrorCode;
import com.luoying.luoojbackendcommon.common.IdRequest;
import com.luoying.luoojbackendcommon.constant.CommonConstant;
import com.luoying.luoojbackendcommon.constant.EmailConstant;
import com.luoying.luoojbackendcommon.constant.RedisKey;
import com.luoying.luoojbackendcommon.constant.UserConstant;
import com.luoying.luoojbackendcommon.exception.BusinessException;
import com.luoying.luoojbackendcommon.exception.ThrowUtils;
import com.luoying.luoojbackendcommon.utils.EmailUtil;
import com.luoying.luoojbackendcommon.utils.JwtUtils;
import com.luoying.luoojbackendcommon.utils.SqlUtils;
import com.luoying.luoojbackendmodel.dto.user.*;
import com.luoying.luoojbackendmodel.entity.User;
import com.luoying.luoojbackendmodel.vo.LoginUserVO;
import com.luoying.luoojbackendmodel.vo.UserVO;
import com.luoying.luoojbackenduserservice.mapper.UserMapper;
import com.luoying.luoojbackenduserservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.luoying.luoojbackendcommon.constant.EmailConstant.*;
import static com.luoying.luoojbackendcommon.constant.RedisKey.*;
import static com.luoying.luoojbackendcommon.constant.UserConstant.SALT;
import static com.luoying.luoojbackendcommon.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author 落樱的悔恨
 * 用户服务实现
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private JavaMailSender mailSender;

    /**
     * 注册
     *
     * @param registerRequest
     * @return
     */
    @Override
    public long userEmailRegister(RegisterRequest registerRequest) {
        // 判空
        if (registerRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 1. 校验
        String userName = registerRequest.getUserName();
        String userAccount = registerRequest.getUserAccount();
        String userPassword = registerRequest.getUserPassword();
        String checkPassword = registerRequest.getCheckPassword();
        String email = registerRequest.getEmail();
        String captcha = registerRequest.getCaptcha();
        if (StringUtils.isAnyBlank(userName, userAccount, userPassword, checkPassword, email, captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "任何一项都不能为空");
        }
        if (userName.length() < 4 || userName.length() > 15) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "昵称长度介于4~15位");
        }
        if (userAccount.length() < 4 || userAccount.length() > 15) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度介于4~15位");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8 || userPassword.length() > 16 || checkPassword.length() > 16) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度介于8~16位");
        }
        // 密码和确认密码需相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 账号、密码只能由英文字母大小写、数字组成
        String regex = "^[a-zA-Z0-9]+$";
        if (!userAccount.matches(regex) || !userPassword.matches(regex) || !checkPassword.matches(regex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "昵称、账号、密码只能由英文字母大小写、数字组成");
        }
        // 校验邮箱格式
        regex = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
        if (!email.matches(regex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的邮箱地址");
        }
        // 校验验证码
        String cacheCaptcha = stringRedisTemplate.opsForValue().get(RedisKey.getKey(REGISTER_EMAIL_CAPTCHA_KEY, email));
        if (StringUtils.isBlank(cacheCaptcha)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码已过期,请重新获取");
        }
        if (!cacheCaptcha.equals(captcha)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码错误");
        }
        synchronized (email.intern()) {
            // 账号不能重复
            LambdaQueryWrapper<User> queryWrapper1 = new LambdaQueryWrapper<>();
            queryWrapper1.eq(User::getUserAccount, userAccount);
            long count = this.count(queryWrapper1);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该账号已经被注册了");
            }
            // 邮箱不能重复
            LambdaQueryWrapper<User> queryWrapper2 = new LambdaQueryWrapper<>();
            queryWrapper2.eq(User::getEmail, email);
            count = this.count(queryWrapper2);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该邮箱已经被注册了");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 插入数据
            User user = new User();
            user.setUserName(userName);
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            user.setEmail(email);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            // 4. redis删除缓存验证码
            stringRedisTemplate.delete(RedisKey.getKey(REGISTER_EMAIL_CAPTCHA_KEY, registerRequest.getEmail()));
            return user.getId();
        }
    }

    /**
     * 账号登录
     *
     * @param userAccountLoginRequest 用户登录请求
     * @param request                 {@link HttpServletRequest}
     */
    @Override
    public LoginUserVO userLogin(UserAccountLoginRequest userAccountLoginRequest, HttpServletRequest request) {
        // 判空
        if (userAccountLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 1. 校验
        String userAccount = userAccountLoginRequest.getUserAccount();
        String userPassword = userAccountLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4 || userAccount.length() > 15) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度介于4~15位");
        }
        if (userPassword.length() < 8 || userPassword.length() > 16) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度介于8~16位");
        }
        // 账号、密码只能由英文字母大小写、数字组成
        String regex = "^[a-zA-Z0-9]+$";
        if (!userAccount.matches(regex) || !userPassword.matches(regex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号、密码只能由英文字母大小写、数字组成");
        }
        // 2. 加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            log.info("user login failed, userAccount cannot match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号或密码错误");
        }
        // 账号被封禁
        if (UserConstant.BAN_ROLE.equals(user.getUserRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "账号已封禁");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        // 4、将登录信息保存在token中，通过JWT生成token(存入id和账号)
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("id", user.getId());
        tokenMap.put("userAccount", user.getUserAccount());
        String token = JwtUtils.getToken(tokenMap);
        // 5、构造返回值
        LoginUserVO loginUserVO = this.getLoginUserVO(user);
        loginUserVO.setToken(token);
        return loginUserVO;
    }


    /**
     * 邮箱登录
     *
     * @param userEmailLoginRequest
     * @param request
     * @return
     */
    @Override
    public LoginUserVO userEmailLogin(UserEmailLoginRequest userEmailLoginRequest, HttpServletRequest request) {
        // 判空
        if (userEmailLoginRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 1. 校验
        String email = userEmailLoginRequest.getEmail();
        String captcha = userEmailLoginRequest.getCaptcha();
        if (StringUtils.isAnyBlank(email, captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 校验邮箱
        String regex = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
        if (!email.matches(regex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的邮箱地址");
        }
        // 校验验证码
        String cacheCaptcha = stringRedisTemplate.opsForValue().get(RedisKey.getKey(LOGIN_EMAIL_CAPTCHA_KEY, email));
        if (StringUtils.isBlank(cacheCaptcha)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码已过期,请重新获取");
        }
        captcha = captcha.trim();
        if (!cacheCaptcha.equals(captcha)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码输入有误");
        }
        // 2. 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", email);
        User user = this.getOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该邮箱未注册账号，请先注册账号");
        }
        // 账号被封禁
        if (UserConstant.BAN_ROLE.equals(user.getUserRole())) {
            throw new BusinessException(ErrorCode.FORBIDDEN_ERROR, "账号已封禁");
        }
        // 3. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        // 4、将登录信息保存在token中，通过JWT生成token(存入id和账号)
        Map<String, Object> tokenMap = new HashMap<>();
        tokenMap.put("id", user.getId());
        tokenMap.put("userAccount", user.getUserAccount());
        String token = JwtUtils.getToken(tokenMap);
        // 5、构造返回值
        LoginUserVO loginUserVO = this.getLoginUserVO(user);
        loginUserVO.setToken(token);
        // 6、redis删除验证码缓存
        stringRedisTemplate.delete(RedisKey.getKey(LOGIN_EMAIL_CAPTCHA_KEY, userEmailLoginRequest.getEmail()));
        return loginUserVO;
    }

    /**
     * 发送邮件
     *
     * @param email
     * @param op
     */
    @Override
    public Boolean sendEmail(String email, String op) {
        if (StringUtils.isBlank(email)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String regex = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
        if (!email.matches(regex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的邮箱地址");
        }
        String key = "";
        long ttl = 0;
        switch (op) {
            case REGISTER:
                key = RedisKey.getKey(REGISTER_EMAIL_CAPTCHA_KEY, email);
                ttl = REGISTER_EMAIL_CAPTCHA_KEY_TTL;
                break;
            case LOGIN:
                key = RedisKey.getKey(LOGIN_EMAIL_CAPTCHA_KEY, email);
                ttl = LOGIN_EMAIL_CAPTCHA_KEY_TTL;
                break;
            case UPDATE_PASSWORD:
                key = RedisKey.getKey(UPDATE_PASSWORD_EMAIL_CAPTCHA_KEY, email);
                ttl = UPDATE_PASSWORD_EMAIL_CAPTCHA_KEY_TTL;
                break;
            case UPDATE_EMAIL:
                key = RedisKey.getKey(UPDATE_EMAIL_EMAIL_CAPTCHA_KEY, email);
                ttl = UPDATE_EMAIL_EMAIL_CAPTCHA_KEY_TTL;
                break;
        }
        // 获取验证码 存入redis
        String captcha = RandomUtil.randomNumbers(6);
        stringRedisTemplate.opsForValue().set(key, captcha, ttl, TimeUnit.MINUTES);
        // 发送邮件
        MimeMessage message = mailSender.createMimeMessage();
        try {
            // 邮箱发送内容组成
            MimeMessageHelper helper = new MimeMessageHelper(message, true);
            // 邮件主题
            helper.setSubject(EmailConstant.EMAIL_SUBJECT);
            // 正文
            helper.setText(EmailUtil.buildEmailContent(EmailConstant.EMAIL_HTML_CONTENT_PATH, captcha), true);
            // 收件人
            helper.setTo(email);
            // 发件人
            helper.setFrom(EmailConstant.EMAIL_TITLE + '<' + "1574925401@qq.com" + '>');
            mailSender.send(message);
            return true;
        } catch (MessagingException e) {
            return false;
        }
    }

    /**
     * 用户登出
     *
     * @param request {@link HttpServletRequest}
     */
    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }

    /**
     * 获取当前登录用户
     *
     * @param request {@link HttpServletRequest}
     */
    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        // 登录用户不存在
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN_ERROR);
        }
        return currentUser;
    }

    /**
     * 获取脱敏的已登录用户信息
     *
     * @param user 用户信息
     * @return 脱敏的用户信息
     */
    @Override
    public LoginUserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        LoginUserVO loginUserVO = new LoginUserVO();
        BeanUtils.copyProperties(user, loginUserVO);
        return loginUserVO;
    }

    /**
     * 创建用户
     *
     * @param userAddRequest
     * @return
     */
    @Override
    public Boolean addUser(UserAddRequest userAddRequest) {
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
        return this.save(user);
    }

    /**
     * 删除用户
     *
     * @param deleteRequest
     * @return
     */
    @Override
    public Boolean deleteUser(DeleteRequest deleteRequest) {
        // 校验
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        return this.removeById(deleteRequest.getId());
    }

    /**
     * 更新用户
     *
     * @param userUpdateRequest
     * @param request
     * @return
     */
    @Override
    public Boolean updateUser(UserUpdateRequest userUpdateRequest, HttpServletRequest request) {
        // 校验
        if (userUpdateRequest == null || userUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取登录用户
        User loginUser = this.getLoginUser(request);
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
        }else{
            user.setUserPassword(null);
        }
        // 更新
        return this.updateById(user);
    }

    /**
     * 用户修改密码
     *
     * @param userPasswordUpdateRequest
     * @param request
     * @return
     */
    @Override
    public Boolean updateUserPassword(UserPasswordUpdateRequest userPasswordUpdateRequest, HttpServletRequest request) {
        if (userPasswordUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取登录用户
        User loginUser = this.getLoginUser(request);
        // 获取前端参数
        String newPassword = userPasswordUpdateRequest.getNewPassword();
        String checkPassword = userPasswordUpdateRequest.getCheckPassword();
        String captcha = userPasswordUpdateRequest.getCaptcha();
        // 判空
        if (StringUtils.isAnyBlank(newPassword, checkPassword, captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 校验新密码长度
        if (newPassword.length() < 8 || checkPassword.length() < 8 || newPassword.length() > 16 || checkPassword.length() > 16) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度介于8~16位");
        }
        // 密码只能由英文字母大小写、数字组成
        String regex = "^[a-zA-Z0-9]+$";
        if (!newPassword.matches(regex) || !checkPassword.matches(regex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码只能由英文字母大小写、数字组成");
        }
        // 判断新密码和校验密码是否一致
        if (!newPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 校验验证码
        String cacheCaptcha = stringRedisTemplate.opsForValue().get(RedisKey.getKey(UPDATE_PASSWORD_EMAIL_CAPTCHA_KEY, loginUser.getEmail()));
        if (StringUtils.isBlank(cacheCaptcha)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码已过期,请重新获取");
        }
        captcha = captcha.trim();
        if (!cacheCaptcha.equals(captcha)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码输入有误");
        }
        // 密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + newPassword).getBytes());
        // 保存到数据库
        User user = new User();
        user.setId(loginUser.getId());
        user.setUserPassword(encryptPassword);
        return this.updateById(user);
    }

    /**
     * 根据 id 获取用户
     *
     * @param id
     * @return
     */
    @Override
    public User getUserById(long id) {
        // 校验
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 查询
        return this.getById(id);
    }

    /**
     * 获取脱敏的用户信息
     *
     * @param user 用户信息（未脱敏）
     * @return 用户信息（脱敏）
     */
    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    /**
     * 根据 id 获取脱敏用户
     *
     * @param id
     * @return
     */
    @Override
    public UserVO getUserVOById(long id) {
        return this.getUserVO(this.getById(id));
    }

    /**
     * 获取查询条件
     *
     * @param userQueryRequest 用户查询请求
     * @return
     */
    @Override
    public QueryWrapper<User> getQueryWrapper(UserQueryRequest userQueryRequest) {
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "请求参数为空");
        }
        Long id = userQueryRequest.getId();
        String userAccount = userQueryRequest.getUserAccount();
        String email = userQueryRequest.getEmail();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        Integer gender = userQueryRequest.getGender();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(userAccount), "userAccount", userAccount);
        queryWrapper.eq(StringUtils.isNotBlank(email), "email", email);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.eq(Objects.nonNull(gender), "gender", gender);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC), sortField);
        return queryWrapper;
    }


    /**
     * 分页获取用户列表（仅管理员）
     *
     * @param userQueryRequest
     * @return
     */
    @Override
    public Page<User> listUserByPage(UserQueryRequest userQueryRequest) {
        long current = userQueryRequest.getCurrent();
        long size = userQueryRequest.getPageSize();
        return this.page(new Page<>(current, size), this.getQueryWrapper(userQueryRequest));
    }

    /**
     * 获取脱敏的用户集合
     *
     * @param userList 用户集合
     * @return
     */
    @Override
    public List<UserVO> getUserVOList(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    /**
     * 分页获取脱敏用户封装列表
     *
     * @param userQueryRequest
     * @return
     */
    @Override
    public Page<UserVO> listUserVOByPage(UserQueryRequest userQueryRequest) {
        // 判空
        if (userQueryRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long current = userQueryRequest.getCurrent();
        long pageSize = userQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(pageSize > 50, ErrorCode.PARAMS_ERROR);
        // 分页查询
        Page<User> userPage = this.page(new Page<>(current, pageSize), this.getQueryWrapper(userQueryRequest));
        Page<UserVO> userVOPage = new Page<>(current, pageSize, userPage.getTotal());
        // 脱敏
        List<UserVO> userVO = this.getUserVOList(userPage.getRecords());
        userVOPage.setRecords(userVO);
        return userVOPage;
    }

    /**
     * 更新个人信息
     *
     * @param userUpdateMyRequest
     * @param request
     * @return
     */
    @Override
    public Boolean updatePersonalInfo(UserUpdateMyRequest userUpdateMyRequest, HttpServletRequest request) {
        // 判空
        if (userUpdateMyRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取登录用户
        User loginUser = this.getLoginUser(request);

        // 拷贝
        User user = new User();
        BeanUtils.copyProperties(userUpdateMyRequest, user);
        user.setId(loginUser.getId());

        // 更新
        return this.updateById(user);
    }

    /**
     * 解封
     *
     * @param idRequest
     * @return
     */
    @Override
    public Boolean normalUser(IdRequest idRequest) {
        // 校验
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 根据id查询用户
        Long id = idRequest.getId();
        User user = this.getById(id);
        if (user == null) {// 用户不存在
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 用户角色设置为user
        user.setUserRole(UserConstant.DEFAULT_ROLE);
        return this.updateById(user);
    }

    /**
     * 封号
     *
     * @param idRequest
     * @return
     */
    @Override
    public Boolean banUser(IdRequest idRequest) {
        // 校验
        if (idRequest == null || idRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 根据Id查询用户
        Long id = idRequest.getId();
        User user = this.getById(id);
        if (user == null) {// 用户不存在
            throw new BusinessException(ErrorCode.NOT_FOUND_ERROR);
        }
        // 用户角色设置为ban
        user.setUserRole(UserConstant.BAN_ROLE);
        return this.updateById(user);
    }

    /**
     * 绑定邮箱
     */
    @Override
    public UserVO userBindEmail(UserBindEmailRequest userBindEmailRequest, HttpServletRequest request) {
        // 判空
        if (userBindEmailRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取登录用户
        User loginUser = this.getLoginUser(request);
        String emailAccount = userBindEmailRequest.getEmail();
        String captcha = userBindEmailRequest.getCaptcha();
        // 1. 校验
        if (StringUtils.isAnyBlank(emailAccount, captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 校验邮箱
        String regex = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
        if (!emailAccount.matches(regex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的邮箱地址");
        }
        // 校验验证码
        String cacheCaptcha = stringRedisTemplate.opsForValue().get(RedisKey.getKey(UPDATE_EMAIL_EMAIL_CAPTCHA_KEY, emailAccount));
        if (StringUtils.isBlank(cacheCaptcha)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码已过期,请重新获取");
        }
        captcha = captcha.trim();
        if (!cacheCaptcha.equals(captcha)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码输入有误");
        }
        // 判断用户是否重复绑定相同邮箱
        if (loginUser.getEmail() != null && emailAccount.equals(loginUser.getEmail())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该账号已绑定此邮箱,请使用其他的邮箱！");
        }
        // 判断该邮箱是否已经被他人绑定
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", emailAccount);
        User user = this.getOne(queryWrapper);
        if (user != null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "此邮箱已被他人绑定,请使用其他的邮箱！");
        }
        // 2. 绑定邮箱
        user = new User();
        user.setId(loginUser.getId());
        user.setEmail(emailAccount);
        boolean result = this.updateById(user);
        if (!result) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "邮箱绑定失败,请稍后再试！");
        }
        loginUser.setEmail(emailAccount);
        return getUserVO(loginUser);
    }

    /**
     * 解除邮箱绑定
     */
    @Override
    public UserVO userUnBindEmail(UserUnBindEmailRequest userUnBindEmailRequest, HttpServletRequest request) {
        // 判空
        if (userUnBindEmailRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 获取登录用户
        User loginUser = this.getLoginUser(request);
        // 1. 校验
        String emailAccount = userUnBindEmailRequest.getEmail();
        String captcha = userUnBindEmailRequest.getCaptcha();
        if (StringUtils.isAnyBlank(emailAccount, captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 校验邮箱
        String regex = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
        if (!emailAccount.matches(regex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的邮箱地址");
        }
        // 校验验证码
        String cacheCaptcha = stringRedisTemplate.opsForValue().get(RedisKey.getKey(UPDATE_EMAIL_EMAIL_CAPTCHA_KEY, emailAccount));
        if (StringUtils.isBlank(cacheCaptcha)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码已过期,请重新获取");
        }
        captcha = captcha.trim();
        if (!cacheCaptcha.equals(captcha)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码输入有误");
        }
        // 判断用户是否绑定该邮箱
        if (loginUser.getEmail() == null || !emailAccount.equals(loginUser.getEmail())) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该账号未绑定此邮箱");
        }
        // 解除绑定
        User user = new User();
        user.setId(loginUser.getId());
        user.setEmail("");
        boolean unBindEmailResult = this.updateById(user);
        if (!unBindEmailResult) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "邮箱解绑失败,请稍后再试！");
        }
        loginUser.setEmail("");
        // 删除验证码缓存
        stringRedisTemplate.delete(RedisKey.getKey(UPDATE_EMAIL_EMAIL_CAPTCHA_KEY, userUnBindEmailRequest.getEmail()));
        return getUserVO(loginUser);
    }
}
