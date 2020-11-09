package com.imooc.controller;

import com.imooc.pojo.bo.UserBo;
import com.imooc.service.UserService;
import com.imooc.utils.IMOOCJSONResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: 用户信息验证
 * @author: Young
 * @create: 2020-11-09 21:35
 **/

@RestController
@RequestMapping("passport")
public class PassportController {

    @Autowired
    public UserService userService;

    @GetMapping("usernameIsExist")
    public IMOOCJSONResult usernameIsExist(@RequestParam String username) {

        //1.判断用户名是否为空
        if (StringUtils.isBlank(username)) {
            return IMOOCJSONResult.errorMsg("用户名为空");
        }

        //2.查找注册的用户名是否存在
        boolean isExist = userService.queryUsernameIsExist(username);
        if (isExist) {
            return IMOOCJSONResult.errorMsg("用户名已存在");
        }

        //3.请求成功 用户名无重复
        return IMOOCJSONResult.ok();
    }

    @GetMapping("regist")
    public IMOOCJSONResult regist(@RequestBody UserBo userBo) {

        String username = userBo.getUsername();
        String password = userBo.getPassword();
        String confirmPassword = userBo.getConfirmPassword();

        if (StringUtils.isBlank(username) ||
            StringUtils.isBlank(password) ||
            StringUtils.isBlank(confirmPassword)) {
            return IMOOCJSONResult.errorMsg("用户名或者密码不能为空");
        }

        if (password.length() < 6) {
            return IMOOCJSONResult.errorMsg("密码长度不能少于6位");
        }

        if (password.equals(confirmPassword)) {
            return IMOOCJSONResult.errorMsg("两次密码不一致");
        }

        userService.creatUser(userBo);
        //3.请求成功 用户名无重复
        return IMOOCJSONResult.ok();
    }
}
