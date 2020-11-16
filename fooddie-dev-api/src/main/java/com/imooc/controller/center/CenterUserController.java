package com.imooc.controller.center;

import com.imooc.pojo.Users;
import com.imooc.pojo.bo.center.CenterUserBO;
import com.imooc.service.center.CenterUserService;
import com.imooc.utils.CookieUtils;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.JsonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: 用户中心
 * @author: Young
 * @create: 2020-11-16 08:37
 **/

@Api(value = "用户信息接口", tags = {"用户信息接口的api接口"})
@RestController
@RequestMapping("userInfo")
public class CenterUserController {

    @Autowired
    private CenterUserService centerUserService;

    @ApiOperation(value="修改用户信息",notes = "修改用户信息",httpMethod = "POST")
    @PostMapping("update")
    public IMOOCJSONResult update(
                    @ApiParam(name = "userId",value = "用户id", required = true)
                    @RequestParam String userId,
                    @RequestBody @Valid CenterUserBO centerUserBO,
                    BindingResult result,
                    HttpServletRequest request, HttpServletResponse response) {

        //用户信息验证
        if (result.hasErrors()) {
            Map<String, String> errorMap = getErrors(result);
            return IMOOCJSONResult.errorMap(errorMap);
        }

        Users users = centerUserService.updateUserInfo(userId, centerUserBO);
        users = setNullProperty(users);

        CookieUtils.setCookie(request,response,"user", JsonUtils.objectToJson(users),true);

        //TODO 后续增加令牌token,整合redis,分布式会话

        return IMOOCJSONResult.ok();
    }

    /**
     * bindingResult错误集合封装map
     * @param result
     * @return
     */
    private Map<String, String> getErrors(BindingResult result) {
        Map<String, String> map = new HashMap<>();
        List<FieldError> fieldErrors = result.getFieldErrors();
        for (FieldError fieldError : fieldErrors){
            //错误属性
            String field = fieldError.getField();
            //错误信息
            String defaultMessage = fieldError.getDefaultMessage();
            map.put(field, defaultMessage);
        }
        return map;
    }

    /**
     * 用户敏感信息清空
     * @param users
     */
    private Users setNullProperty(Users users) {
        users.setPassword(null);
        users.setMobile(null);
        users.setEmail(null);
        users.setUpdatedTime(null);
        users.setCreatedTime(null);
        users.setBirthday(null);
        return users;
    }

}
