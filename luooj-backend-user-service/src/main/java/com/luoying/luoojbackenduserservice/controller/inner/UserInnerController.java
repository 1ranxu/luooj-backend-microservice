package com.luoying.luoojbackenduserservice.controller.inner;

import com.luoying.luoojbackendmodel.entity.User;
import com.luoying.luoojbackendserviceclient.service.UserFeighClient;
import com.luoying.luoojbackenduserservice.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.Collection;
import java.util.List;

/**
 * @author 落樱的悔恨
 * 该服务仅内部调用，不是给前端的
 */
@RestController
@RequestMapping("/inner")
public class UserInnerController implements UserFeighClient {

    @Resource
    private UserService userService;

    /**
     * 根据id获取用户
     *
     * @param userId 用户id
     * @return {@link User}
     */
    @Override
    @GetMapping("/get/id")
    public User getById(@RequestParam("userId") long userId) {
        return userService.getById(userId);
    }

    /**
     * 根据用户id集合获取用户列表
     *
     * @param ids 用户id集合
     * @return {@link List<User>}
     */
    @Override
    @GetMapping("/get/ids")
    public List<User> listByIds(@RequestParam("ids") Collection<Long> ids) {
        return userService.listByIds(ids);
    }
}
