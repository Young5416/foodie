package com.imooc.controller;

import org.springframework.web.bind.annotation.GetMapping;
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

    public static final String FOODIE_SHOPCART = "shopcart";
    public static final Integer COMMON_PAGE_SIZE = 10;
    public static final Integer PAGE_SIZE = 20;

    // 支付中心的调用地址
    String paymentUrl = "http://payment.t.mukewang.com/foodie-payment/payment/createMerchantOrder";		// produce

    //微信支付回调url
    public String payReturnUrl = "http://yd4s79.natappfree.cc/orders/notifyMerchantOrderPaid";
}
