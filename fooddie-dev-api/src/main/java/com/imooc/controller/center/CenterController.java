package com.imooc.controller.center;

import com.imooc.pojo.Users;
import com.imooc.service.center.CenterUserService;
import com.imooc.utils.IMOOCJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: 用户中心
 * @author: Young
 * @create: 2020-11-16 08:37
 **/

@Api(value = "用户中心", tags = {"用户中心的api接口"})
@RestController
@RequestMapping("center")
public class CenterController {

    @Autowired
    private CenterUserService centerUserService;

    @ApiOperation(value="获取用户信息",notes = "获取用户信息",httpMethod = "GET")
    @GetMapping("userInfo")
    public IMOOCJSONResult userInfo(
                    @ApiParam(name = "userId",value = "用户id", required = true)
                    @RequestParam String userId) {
        Users users = centerUserService.queryUserInfo(userId);
        return IMOOCJSONResult.ok(users);
    }

}
