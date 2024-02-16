package com.luoying.luoojbackenduserservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.CollectionUtils;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.luoying.luoojbackendcommon.common.ErrorCode;
import com.luoying.luoojbackendcommon.constant.CommonConstant;
import com.luoying.luoojbackendcommon.exception.BusinessException;
import com.luoying.luoojbackendcommon.utils.JwtUtils;
import com.luoying.luoojbackendcommon.utils.SqlUtils;
import com.luoying.luoojbackendmodel.dto.user.UserLoginRequest;
import com.luoying.luoojbackendmodel.dto.user.UserQueryRequest;
import com.luoying.luoojbackendmodel.dto.user.UserRegisterRequest;
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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.luoying.luoojbackendcommon.constant.UserConstant.SALT;
import static com.luoying.luoojbackendcommon.constant.UserConstant.USER_LOGIN_STATE;

/**
 * @author 落樱的悔恨
 * 用户服务实现
 */
@Service
@Slf4j
@Transactional
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {



    @Resource
    private QuestionFeignClient questionFeignClient;

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





}
