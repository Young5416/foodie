package com.imooc.controller;

import com.imooc.pojo.Users;
import com.imooc.pojo.bo.ShopcartBO;
import com.imooc.pojo.bo.UserBo;
import com.imooc.service.UserService;
import com.imooc.utils.CookieUtils;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.MD5Utils;
import com.imooc.utils.RedisOperator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description: 用户信息验证
 * @author: Young
 * @create: 2020-11-09 21:35
 **/

@Api(value = "注册登录", tags = {"注册登录相关接口"})
@RestController
@RequestMapping("passport")
public class PassportController extends BaseController{

    @Autowired
    public UserService userService;

    @Autowired
    public RedisOperator redisOperator;

    @ApiOperation(value = "用户名是否存在", notes = "用户名是否存在", httpMethod = "GET ")
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

    @ApiOperation(value = "用户注册", notes = "用户注册", httpMethod = "POST")
    @PostMapping("regist")
    public IMOOCJSONResult regist(@RequestBody UserBo userBo,HttpServletRequest request, HttpServletResponse response) {

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

        if (!password.equals(confirmPassword)) {
            return IMOOCJSONResult.errorMsg("两次密码不一致");
        }

        Users users = userService.creatUser(userBo);

        users = setNullProperty(users);

        CookieUtils.setCookie(request,response,"user", JsonUtils.objectToJson(users),true);
        //3.请求成功 用户名无重复
        return IMOOCJSONResult.ok();
    }

    @ApiOperation(value = "用户登录", notes = "用户登录", httpMethod = "POST")
    @PostMapping("login")
    public IMOOCJSONResult login(@RequestBody UserBo userBo, HttpServletRequest request, HttpServletResponse response) throws Exception {

        String username = userBo.getUsername();
        String password = userBo.getPassword();

        if (StringUtils.isBlank(username) ||
            StringUtils.isBlank(password)) {
            return IMOOCJSONResult.errorMsg("用户名或者密码不能为空");
        }

        Users users = userService.queryUserForLogin(username, MD5Utils.getMD5Str(password));
        if (users == null) {
            return IMOOCJSONResult.errorMsg("用户名或者密码错误");
        }
        users = setNullProperty(users);

        CookieUtils.setCookie(request,response,"user", JsonUtils.objectToJson(users),true);

        //TODO 生成用户toke 存入redis会话
        //同步购物车数据
        syncShopcartData(users.getId(),request, response);

        return IMOOCJSONResult.ok(users);
    }

    /**
     *@Description 注册登录后,同步cookie和redis中的数据
     *@Param  userId,request,response
     *@return
     *@Author  Young
     *@Date  2020/11/23
     *@Change
     */
    private void syncShopcartData(String userId,HttpServletRequest request, HttpServletResponse response) {
        /**
         * 1.reids购物车无数据 ,cookie购物车无数据 无操作
         *                    cookie购物车有数据 同步到redis中
         * 2.reids购物车有数据,cookie购物车无数据,同步到cookie中
         *                   cookie购物车有数据,遍历所有商品,已cookie商品为主,将商品数据同步到redis中
         * 3.同步到redis后,覆盖本地cookie
         */
        //从redis中获取购物车
        String shopCartJsonRedis = redisOperator.get(FOODIE_SHOPCART + ":" + userId);

        //从cookie中获取购物车
        String shopCartJsonCookie = CookieUtils.getCookieValue(request, FOODIE_SHOPCART, true);

        if (StringUtils.isBlank(shopCartJsonRedis)) {
            //redis为空同步cookie数据
            if (StringUtils.isNotBlank(shopCartJsonCookie)) {
                redisOperator.set(FOODIE_SHOPCART + ":" + userId,shopCartJsonCookie);
            }
        } else {

            if (StringUtils.isNotBlank(shopCartJsonCookie)) {
                //redis有数据 cookie有数据 合并
                /**
                 * 1.交集的商品 cookie直接覆盖redis
                 * 2.将该商品j加入待删除list
                 * 3.cookie中清除所有待删除list
                 * 4.合并redis和cookie
                 * 5.更新redis和cookie
                 */
                List<ShopcartBO> shopCartRedis = JsonUtils.jsonToList(shopCartJsonRedis, ShopcartBO.class);
                List<ShopcartBO> shopCartCookie = JsonUtils.jsonToList(shopCartJsonCookie, ShopcartBO.class);

                List<ShopcartBO> toBeRemoveShopCart = new ArrayList<>();

                for (ShopcartBO reidsShopcartBO : shopCartRedis) {
                    String redisSpecId = reidsShopcartBO.getSpecId();

                    for (ShopcartBO cookieShopcartBO : shopCartCookie) {
                        if (redisSpecId.equals(cookieShopcartBO.getSpecId())) {
                            //覆盖购买数量
                            reidsShopcartBO.setBuyCounts(cookieShopcartBO.getBuyCounts());
                            //加入待删除列表
                            toBeRemoveShopCart.add(cookieShopcartBO);
                        }
                    }
                }

                //cookie删除覆盖过redis重复的数据
                shopCartCookie.removeAll(toBeRemoveShopCart);
                //合并redis和cookie
                shopCartRedis.addAll(shopCartCookie);
                //更新redis和cookie
                CookieUtils.setCookie(request,response, FOODIE_SHOPCART,JsonUtils.objectToJson(shopCartRedis),true);
                redisOperator.set(FOODIE_SHOPCART + ":" + userId,JsonUtils.objectToJson(shopCartRedis));

            } else {
                //redis有数据 cookie无数据 覆盖cookie
                CookieUtils.setCookie(request,response, FOODIE_SHOPCART,shopCartJsonRedis,true);
            }
        }
    }

    @ApiOperation(value = "用户退出登录", notes = "用户退出登录", httpMethod = "POST")
    @PostMapping("logout")
    public IMOOCJSONResult logout(@RequestParam String userId,HttpServletRequest request,HttpServletResponse response){
        CookieUtils.deleteCookie(request,response,"user");

        //用户退出登录 清除购物车
        CookieUtils.deleteCookie(request, response, FOODIE_SHOPCART);
        //TODO 分布式会话中清除用户数据

        return IMOOCJSONResult.ok();
    }

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
