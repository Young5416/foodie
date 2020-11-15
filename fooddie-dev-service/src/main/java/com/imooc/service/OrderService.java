package com.imooc.service;

import com.imooc.pojo.OrderStatus;
import com.imooc.pojo.bo.SubmitOrderBO;
import com.imooc.pojo.vo.OrderVO;

/**
 * @description:
 * @author: Young
 * @create: 2020-11-14 17:02
 **/

public interface OrderService {

    /**
     * 创建订单
     * @param submitOrderBO
     * @return
     */
    public OrderVO createOrder(SubmitOrderBO submitOrderBO);

    /**
     * 修改订单状态
     * @param orderId
     * @param orderStatus
     */
    public void updateOrderStatus(String orderId,Integer orderStatus);

    /**
     * 查询订单状态
     * @param orderId
     */
    public OrderStatus queryOrderStatusInfo(String orderId);
}
