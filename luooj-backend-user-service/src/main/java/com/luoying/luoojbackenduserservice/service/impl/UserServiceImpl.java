package com.luoying.luoojbackenduserservice.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luoying.luoojbackendcommon.common.ErrorCode;
import com.luoying.luoojbackendcommon.constant.CommonConstant;
import com.luoying.luoojbackendcommon.constant.RedisKey;
import com.luoying.luoojbackendcommon.constant.UserConstant;
import com.luoying.luoojbackendcommon.exception.BusinessException;
import com.luoying.luoojbackendcommon.utils.JwtUtils;
import com.luoying.luoojbackendcommon.utils.SqlUtils;
import com.luoying.luoojbackendmodel.dto.user.*;
import com.luoying.luoojbackendmodel.entity.User;
import com.luoying.luoojbackendmodel.enums.UserRoleEnum;
import com.luoying.luoojbackendmodel.vo.LoginUserVO;
import com.luoying.luoojbackendmodel.vo.UserVO;
import com.luoying.luoojbackendserviceclient.service.QuestionFeignClient;
import com.luoying.luoojbackenduserservice.mapper.UserMapper;
import com.luoying.luoojbackenduserservice.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.luoying.luoojbackendcommon.constant.RedisKey.EMAIL_CAPTCHA_KEY;
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
    private QuestionFeignClient questionFeignClient;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 注册
     * @param userRegisterRequest 用户注册请求
     */
    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest) {
        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        // 1. 校验
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userAccount.length() < 4 || userAccount.length() > 15) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号长度介于4~15位");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8 || userPassword.length() > 16 || checkPassword.length() > 16) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "密码长度介于8~16位");
        }
        // 密码和校验密码相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "两次输入的密码不一致");
        }
        // 昵称、账号、密码只能由英文字母大小写、数字组成
        String regex = "^[a-zA-Z0-9]+$";
        if (!userAccount.matches(regex) || !userPassword.matches(regex) || !checkPassword.matches(regex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "昵称、账号、密码只能由英文字母大小写、数字组成");
        }
        synchronized (userAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", userAccount);
            long count = this.baseMapper.selectCount(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
            }
            // 2. 加密
            String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
            // 3. 插入数据
            User user = new User();
            user.setUserAccount(userAccount);
            user.setUserPassword(encryptPassword);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            long userId = user.getId();
            // 4. 创建 个人通过题目表 和 个人提交表
            String acceptedQuestionTable = "accepted_question_" + userId;
            String questionSubmitTable = "question_submit_" + userId;
            // 查询 题目通过表 是否存在
            if (questionFeignClient.existAcceptedQuestionTable(acceptedQuestionTable)
                    || questionFeignClient.existQuestionSubmitTable(questionSubmitTable)) {
                // 删除旧表
                questionFeignClient.dropAcceptedQuestionTable(acceptedQuestionTable);
                questionFeignClient.dropQuestionSubmitTable(questionSubmitTable);
            }
            // 新建 题目通过表 和 个人提交表
            questionFeignClient.createAcceptedQuestionTable(acceptedQuestionTable);
            questionFeignClient.createQuestionSubmitTable(questionSubmitTable);
            return userId;
        }
    }

    /**
     * 登录
     * @param userLoginRequest 用户登录请求
     * @param request          {@link HttpServletRequest}
     */
    @Override
    public LoginUserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request) {
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        // 1. 校验
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
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户不存在或密码错误");
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

    @Override
    public long userEmailRegister(UserEmailRegisterRequest userEmailRegisterRequest) {
        String userName = userEmailRegisterRequest.getUserName();
        String emailAccount = userEmailRegisterRequest.getEmailAccount();
        String captcha = userEmailRegisterRequest.getCaptcha();
        // 1. 校验
        if (StringUtils.isAnyBlank(userName, emailAccount, captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        if (userName.length() < 4 || userName.length() > 15) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "昵称长度介于4~15位");
        }
        String regex = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
        if (!emailAccount.matches(regex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的邮箱地址");
        }

        String cacheCaptcha = stringRedisTemplate.opsForValue().get(RedisKey.getKey(EMAIL_CAPTCHA_KEY, emailAccount));
        if (StringUtils.isBlank(cacheCaptcha)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码已过期,请重新获取");
        }
        if (!cacheCaptcha.equals(captcha)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码错误");
        }
        synchronized (emailAccount.intern()) {
            // 账户不能重复
            QueryWrapper<User> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("userAccount", emailAccount);
            long count = this.count(queryWrapper);
            if (count > 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "该邮箱已经被注册了");
            }
            // 2. 插入数据
            User user = new User();
            user.setUserName(userName);
            user.setUserAccount(emailAccount);
            user.setEmail(emailAccount);
            boolean saveResult = this.save(user);
            if (!saveResult) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "注册失败，数据库错误");
            }
            long userId = user.getId();
            // 3. 创建 个人通过题目表 和 个人提交表
            String acceptedQuestionTable = "accepted_question_" + userId;
            String questionSubmitTable = "question_submit_" + userId;
            // 查询 题目通过表 是否存在
            if (questionFeignClient.existAcceptedQuestionTable(acceptedQuestionTable)
                    || questionFeignClient.existQuestionSubmitTable(questionSubmitTable)) {
                // 删除旧表
                questionFeignClient.dropAcceptedQuestionTable(acceptedQuestionTable);
                questionFeignClient.dropQuestionSubmitTable(questionSubmitTable);
            }
            // 新建 题目通过表 和 个人提交表
            questionFeignClient.createAcceptedQuestionTable(acceptedQuestionTable);
            questionFeignClient.createQuestionSubmitTable(questionSubmitTable);
            return user.getId();
        }
    }

    @Override
    public LoginUserVO userEmailLogin(UserEmailLoginRequest userEmailLoginRequest, HttpServletRequest request) {
        String emailAccount = userEmailLoginRequest.getEmailAccount();
        String captcha = userEmailLoginRequest.getCaptcha();
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
        String cacheCaptcha = stringRedisTemplate.opsForValue().get(RedisKey.getKey(EMAIL_CAPTCHA_KEY, emailAccount));
        if (StringUtils.isBlank(cacheCaptcha)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码已过期,请重新获取");
        }
        captcha = captcha.trim();
        if (!cacheCaptcha.equals(captcha)) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "验证码输入有误");
        }
        // 2. 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("email", emailAccount);
        User user = this.getOne(queryWrapper);
        // 用户不存在
        if (user == null) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "该邮箱未绑定账号，请先绑定账号");
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
        String unionId = userQueryRequest.getUnionId();
        String mpOpenId = userQueryRequest.getMpOpenId();
        String userName = userQueryRequest.getUserName();
        String userProfile = userQueryRequest.getUserProfile();
        String userRole = userQueryRequest.getUserRole();
        String sortField = userQueryRequest.getSortField();
        String sortOrder = userQueryRequest.getSortOrder();
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(StringUtils.isNotBlank(unionId), "unionId", unionId);
        queryWrapper.eq(StringUtils.isNotBlank(mpOpenId), "mpOpenId", mpOpenId);
        queryWrapper.eq(StringUtils.isNotBlank(userRole), "userRole", userRole);
        queryWrapper.like(StringUtils.isNotBlank(userProfile), "userProfile", userProfile);
        queryWrapper.like(StringUtils.isNotBlank(userName), "userName", userName);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }


    /**
     *
     * @param userList 用户集合
     */
    @Override
    public List<UserVO> getUserVO(List<User> userList) {
        if (CollectionUtils.isEmpty(userList)) {
            return new ArrayList<>();
        }
        return userList.stream().map(this::getUserVO).collect(Collectors.toList());
    }

    /**
     *
     * @param request {@link HttpServletRequest}
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // 仅管理员可查询
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        return isAdmin(user);
    }

    /**
     *
     * @param user 用户信息
     */
    @Override
    public boolean isAdmin(User user) {
        return user != null && UserRoleEnum.ADMIN.getValue().equals(user.getUserRole());
    }

    /**
     * 绑定邮箱
     */
    @Override
    public UserVO userBindEmail(UserBindEmailRequest userBindEmailRequest, User loginUser) {
        String emailAccount = userBindEmailRequest.getEmailAccount();
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
        String cacheCaptcha = stringRedisTemplate.opsForValue().get(RedisKey.getKey(EMAIL_CAPTCHA_KEY, emailAccount));
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
    public UserVO userUnBindEmail(UserUnBindEmailRequest userUnBindEmailRequest, User loginUser) {
        String emailAccount = userUnBindEmailRequest.getEmailAccount();
        String captcha = userUnBindEmailRequest.getCaptcha();
        // 1. 校验
        if (StringUtils.isAnyBlank(emailAccount, captcha)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 校验邮箱
        String regex = "^[a-zA-Z0-9_-]+@[a-zA-Z0-9_-]+(\\.[a-zA-Z0-9_-]+)+$";
        if (!emailAccount.matches(regex)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的邮箱地址");
        }
        // 校验验证码
        String cacheCaptcha = stringRedisTemplate.opsForValue().get(RedisKey.getKey(EMAIL_CAPTCHA_KEY, emailAccount));
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
        boolean bindEmailResult = this.updateById(user);
        if (!bindEmailResult) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "邮箱解绑失败,请稍后再试！");
        }
        loginUser.setEmail("");
        return getUserVO(loginUser);
    }


}
