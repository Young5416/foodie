package com.imooc.service;

import com.imooc.pojo.bo.SubmitOrderBO;

/**
 * @description:
 * @author: Young
 * @create: 2020-11-14 17:02
 **/

public interface OrderService {

    /**
     * 创建订单
     * @param submitOrderBO
     */
    public String createOrder(SubmitOrderBO submitOrderBO);
}
