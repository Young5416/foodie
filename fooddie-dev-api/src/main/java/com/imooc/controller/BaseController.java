package com.imooc.controller;

import com.imooc.pojo.Orders;
import com.imooc.pojo.Users;
import com.imooc.pojo.vo.UsersVO;
import com.imooc.service.center.MyOrdersService;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.RedisOperator;
import java.util.UUID;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import springfox.documentation.annotations.ApiIgnore;

/**
 * @description:
 * @author: Young
 * @create: 2020-11-09 14:14
 **/

@ApiIgnore
@RestController
public class BaseController {


    @Autowired
    private MyOrdersService myOrdersService;

    @Autowired
    public RedisOperator redisOperator;

    public static final String FOODIE_SHOPCART = "shopcart";
    public static final Integer COMMON_PAGE_SIZE = 10;
    public static final Integer PAGE_SIZE = 20;

    public static final String REDIS_USER_TOKEN = "redis_user_token";

    // 支付中心的调用地址
    String paymentUrl = "http://payment.t.mukewang.com/foodie-payment/payment/createMerchantOrder";		// produce

    //微信支付回调url
    public String payReturnUrl = "http://47.104.85.220:8088/fooddie-dev-api/orders/notifyMerchantOrderPaid";

    /**
     * 用于验证用户和订单是否有关联关系，避免非法用户调用
     * @return
     */
    public IMOOCJSONResult checkUserOrder(String userId, String orderId) {
        Orders order = myOrdersService.queryMyOrder(userId, orderId);
        if (order == null) {
            return IMOOCJSONResult.errorMsg("订单不存在！");
        }
        return IMOOCJSONResult.ok(order);
    }
    /**
     * redis存储用户会话
     * @return
     */
    public UsersVO conventUserVo(Users users) {
        String uniqueToken = UUID.randomUUID().toString().trim();
        redisOperator.set(REDIS_USER_TOKEN + ":" + users.getId(), uniqueToken);
        UsersVO usersVO = new UsersVO();
        BeanUtils.copyProperties(users,usersVO);
        usersVO.setUserUniqueToken(uniqueToken);
        return usersVO;
    }
}
