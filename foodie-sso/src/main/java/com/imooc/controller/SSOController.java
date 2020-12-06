package com.imooc.controller;

import com.imooc.pojo.Users;
import com.imooc.pojo.vo.UsersVO;
import com.imooc.service.UserService;
import com.imooc.utils.CookieUtils;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.MD5Utils;
import com.imooc.utils.RedisOperator;
import java.util.UUID;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * @description:
 * @author: Young
 * @create: 2020-12-06 12:17
 **/
@Controller
public class SSOController {

    @Autowired
    private UserService userService;

    @Autowired
    private RedisOperator redisOperator;

    public static final String REDIS_USER_TOKEN = "redis_user_token";
    public static final String REDIS_USER_TICKET = "redis_user_ticket";
    public static final String REDIS_TMP_TICKET = "redis_tmp_ticket";
    public static final String COOKIE_USER_TICKET = "cookie_user_ticket";

    @GetMapping("login")
    public String login(String returnUrl, Model model, HttpServletRequest request, HttpServletResponse response) {
        model.addAttribute("returnUrl",returnUrl);

        //1. 获取userTicket门票m吗如果cookie能获取到 证明用户登录过 此时签发一个一次性票据
        String userTicket = getCookie(request, COOKIE_USER_TICKET);
        boolean isVerify = verifyUserTicket(userTicket);
        if (isVerify) {
            String tmpTicket = createTmpTicket();
            return "redirect:" + returnUrl + "?tmpTicket=" + tmpTicket;
        }

        //2. 用户从未登录,第一次进入则跳转到cas的统一登录页面
        return "login";
    }

    @PostMapping("logout")
    @ResponseBody
    public IMOOCJSONResult logout(String userId,HttpServletRequest request,HttpServletResponse response) {
        //0. 获取cas的用户门票
        String userTicket = getCookie(request, COOKIE_USER_TICKET);

        //1. 清除userTicket票据,redis/cookie
        deleteCookie(COOKIE_USER_TICKET, response);
        redisOperator.del(REDIS_USER_TICKET + ":" + userTicket);

        //2. 清除用户全局会话(分布式会话)
        redisOperator.del(REDIS_USER_TOKEN + ":" + userId);

        return IMOOCJSONResult.ok();
    }

    private boolean verifyUserTicket(String userTicket) {
        //0. 验证cas门票不能为空
        if (StringUtils.isBlank(userTicket)) {
            return false;
        }

        //1. 验证cas门票是否有效
        String userId = redisOperator.get(REDIS_USER_TICKET + ":" + userTicket);
        if (StringUtils.isBlank(userId)) {
            return false;
        }

        //2. 验证门票对应的会话是否存在
        String userRedis = redisOperator.get(REDIS_USER_TOKEN + ":" + userId);
        if (StringUtils.isBlank(userRedis)) {
            return false;
        }

        return true;
    }

    /**
     * CAS的统一登录接口
     *         目的:
     *          1.登录后创建用户的全局会话                  ->uniqueToken
     *          2.创建用户全局门票,用以表示在cas端登录       ->userTicket
     *          1.创建用户临时票据,用于回跳回传             ->tempTicket
     * @param username
     * @param password
     * @param returnUrl
     * @param model
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @PostMapping("doLogin")
    public String doLogin(String username,String password,String returnUrl, Model model, HttpServletRequest request, HttpServletResponse response)
        throws Exception {

        model.addAttribute("returnUrl",returnUrl);

        //0.判断用户信息
        if (StringUtils.isBlank(username) ||
            StringUtils.isBlank(password)) {
            model.addAttribute("errMsg","用户名或密码不能为空");
            return "login";
        }

        //1.实现登录
        Users users = userService.queryUserForLogin(username, MD5Utils.getMD5Str(password));
        if (users == null) {
            model.addAttribute("errMsg","用户名或密码不正确");
            return "login";
        }

        //2.实现用户的redis会话
        String uniqueToken = UUID.randomUUID().toString().trim();
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(users,usersVO);
        usersVO.setUserUniqueToken(uniqueToken);
        redisOperator.set(REDIS_USER_TOKEN + ":" + users.getId(), JsonUtils.objectToJson(usersVO));

        //3 生成ticket门票,全局门票.代表用户在cas端登录过
        String userTicket = UUID.randomUUID().toString().trim();
        //3.1 全局门票放入cas端的cookie中
        setCookie(COOKIE_USER_TICKET,userTicket,response);

        //4.userTicket关联用户id,放入redis中
        redisOperator.set(REDIS_USER_TICKET + ":" + userTicket,users.getId());

        //5.生成临时票据,回跳到调用端网站,由cas签发的一次性临时ticket
        String tmpTicket = createTmpTicket();

//        return "login";
        return "redirect:" + returnUrl + "?tmpTicket=" + tmpTicket;
    }

    @PostMapping("verifyTmpTicket")
    @ResponseBody
    public IMOOCJSONResult verifyTmpTicket(String tmpTicket, HttpServletRequest request, HttpServletResponse response)
        throws Exception {
        //通过一次性票据查询用户登录,如果登录,把用户信息返回给站点
        //使用完毕后 销毁票据
        String tmpTicketValue = redisOperator.get(REDIS_TMP_TICKET + ":" + tmpTicket);
        if (StringUtils.isBlank(tmpTicketValue)) {
            return IMOOCJSONResult.errorUserTicket("用户票据异常");
        }

        //0. 用户票据正常 销毁票据并且拿到cas端全局userTicket,以此获取用户会话
        if (!tmpTicketValue.equals(MD5Utils.getMD5Str(tmpTicket))) {
            return IMOOCJSONResult.errorUserTicket("用户票据异常");
        } else {
            //销毁临时票据
            redisOperator.del(REDIS_TMP_TICKET + ":" + tmpTicket);
        }

        //1. 验证并且获取用户的userTicket
        String userTicket = getCookie(request, COOKIE_USER_TICKET);
        String userId = redisOperator.get(REDIS_USER_TICKET + ":" + userTicket);
        if (StringUtils.isBlank(userId)) {
            return IMOOCJSONResult.errorUserTicket("用户票据异常");
        }

        //2. 验证门票对应的会话是否存在
        String userRedis = redisOperator.get(REDIS_USER_TOKEN + ":" + userId);
        if (StringUtils.isBlank(userRedis)) {
            return IMOOCJSONResult.errorUserTicket("用户票据异常");
        }

        //验证成功,返回ok,携带用户会话
        return IMOOCJSONResult.ok(JsonUtils.jsonToPojo(userRedis, UsersVO.class));
    }


    /**
     * 创建临时票据
     * @return
     */
    private String createTmpTicket() {
        String tmpTicket = UUID.randomUUID().toString().trim();

        try {
            redisOperator.set(REDIS_TMP_TICKET + ":" +tmpTicket,MD5Utils.getMD5Str(tmpTicket),600);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return tmpTicket;
    }

    private void setCookie(String key, String val,HttpServletResponse response) {
        Cookie cookie = new Cookie(key, val);
        cookie.setDomain("sso.com");
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    private void deleteCookie(String key,HttpServletResponse response) {
        Cookie cookie = new Cookie(key, null);
        cookie.setDomain("sso.com");
        cookie.setPath("/");
        cookie.setMaxAge(-1);
        response.addCookie(cookie);
    }

    private String getCookie(HttpServletRequest request,String key) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null || StringUtils.isBlank(key)) {
            return null;
        }

        String cookieValue = null;

        for (int i = 0; i < cookies.length; i++) {
            if (cookies[i].getName().equals(key)) {
                cookieValue = cookies[i].getValue();
                break;
            }
        }
        return cookieValue;
    }
}
