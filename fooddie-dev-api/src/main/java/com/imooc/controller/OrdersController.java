package com.imooc.controller;

import com.imooc.enums.PayMethod;
import com.imooc.pojo.bo.SubmitOrderBO;
import com.imooc.service.OrderService;
import com.imooc.utils.CookieUtils;
import com.imooc.utils.IMOOCJSONResult;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: Young
 * @create: 2020-11-14 16:53
 **/

@Api(value = "订单接口", tags = {"订单相关的api接口"})
@RestController
@RequestMapping("orders")
public class OrdersController extends BaseController{
    @Autowired
    private OrderService orderService;

    @ApiOperation(value = "用户下单", notes = "用户下单", httpMethod = "POST")
    @PostMapping("/create")
    public IMOOCJSONResult create(@RequestBody SubmitOrderBO submitOrderBO, HttpServletRequest request,
        HttpServletResponse response) {

        if (!submitOrderBO.getPayMethod().equals(PayMethod.ALIPAY.type) && !submitOrderBO.getPayMethod()
            .equals(PayMethod.WEIXIN.type)) {
            return IMOOCJSONResult.errorMsg("支付方式不支持");
        }
        //1.创建订单
        String orderId = orderService.createOrder(submitOrderBO);
        //2.移除购物车已结算的商品

        //TODO 在redis中,将已结算商品清除,同步到前端的cookie中
//        CookieUtils.setCookie(request,response,FOODIE_SHOPCART,"",true);

        //3.向支付中心发送订单,保存订单数据

        return IMOOCJSONResult.ok(orderId);
    }
}
