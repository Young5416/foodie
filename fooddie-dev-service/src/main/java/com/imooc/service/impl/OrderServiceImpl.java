package com.imooc.service.impl;

import com.imooc.enums.OrderStatusEnum;
import com.imooc.enums.YesOrNo;
import com.imooc.mapper.OrderItemsMapper;
import com.imooc.mapper.OrderStatusMapper;
import com.imooc.mapper.OrdersMapper;
import com.imooc.pojo.Items;
import com.imooc.pojo.ItemsSpec;
import com.imooc.pojo.OrderItems;
import com.imooc.pojo.OrderStatus;
import com.imooc.pojo.Orders;
import com.imooc.pojo.UserAddress;
import com.imooc.pojo.bo.SubmitOrderBO;
import com.imooc.pojo.vo.MerchantOrdersVO;
import com.imooc.pojo.vo.OrderVO;
import com.imooc.service.AddressService;
import com.imooc.service.ItemService;
import com.imooc.service.OrderService;
import java.util.Date;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/**
 * @description:
 * @author: Young
 * @create: 2020-11-14 17:03
 **/

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderItemsMapper orderItemsMapper;

    @Autowired
    private OrdersMapper ordersMapper;

    @Autowired
    private OrderStatusMapper orderStatusMapper;


    @Autowired
    private Sid sid;

    @Autowired
    private AddressService addressService;

    @Autowired
    private ItemService itemService;

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public OrderVO createOrder(SubmitOrderBO submitOrderBO) {

        Integer payMethod = submitOrderBO.getPayMethod();
        String addressId = submitOrderBO.getAddressId();
        String itemSpecIds = submitOrderBO.getItemSpecIds();
        String leftMsg = submitOrderBO.getLeftMsg();
        String userId = submitOrderBO.getUserId();
        //包邮费用默认设置为0
        Integer postAmount = 0;

        String orderId = sid.nextShort();

        //1.新订单数据保存
        Orders newOrders = new Orders();
        newOrders.setId(orderId);
        newOrders.setUserId(userId);

        //订单接收者信息
        UserAddress userAddress = addressService.queryUserAddres(userId, addressId);
        newOrders.setReceiverName(userAddress.getReceiver());
        newOrders.setReceiverAddress(userAddress.getProvince()
                                     + " " + userAddress.getCity()
                                     + " " + userAddress.getDistrict()
                                      + " " +userAddress.getDetail());
        newOrders.setReceiverMobile(userAddress.getMobile());
//        newOrders.setTotalAmount();
//        newOrders.setRealPayAmount();
        newOrders.setPayMethod(payMethod);
        newOrders.setLeftMsg(leftMsg);
        newOrders.setPostAmount(postAmount);

        newOrders.setIsComment(YesOrNo.NO.type);
        newOrders.setIsDelete(YesOrNo.NO.type);
        newOrders.setUpdatedTime(new Date());
        newOrders.setCreatedTime(new Date());

        //2.根据itemSpecIds保存订单信息关联表

        String[] itemSpecIdArr = itemSpecIds.split(",");
        Integer totalAmount = 0; //商品原价累计
        Integer realAmount = 0; //优惠后的实际支付价格累计
        //2.1 根据规格id查询规格信息,主要获取价格
        for (String itemSpecId : itemSpecIdArr) {

            //TODO 商品数量从redis中取
            int buyCounts = 1;

            ItemsSpec itemsSpec = itemService.queryItemSpecById(itemSpecId);
            totalAmount += itemsSpec.getPriceNormal() * buyCounts;
            realAmount += itemsSpec.getPriceDiscount() * buyCounts;

            //2.2 根据商品id获取商品信息及图片
            String itemId = itemsSpec.getItemId();
            Items items = itemService.queryItemById(itemId);
            String imgUrl = itemService.queryItemMainImgById(itemId);

            //2.3 循环保存子订单到数据库
            String subOrderId = sid.nextShort();
            OrderItems subOrderItem = new OrderItems();
            subOrderItem.setId(subOrderId);
            subOrderItem.setOrderId(orderId);
            subOrderItem.setItemId(itemId);
            subOrderItem.setItemImg(imgUrl);
            subOrderItem.setItemName(items.getItemName());
            subOrderItem.setBuyCounts(buyCounts);
            subOrderItem.setItemSpecId(itemSpecId);
            subOrderItem.setItemSpecName(itemsSpec.getName());
            subOrderItem.setPrice(itemsSpec.getPriceDiscount());
            orderItemsMapper.insert(subOrderItem);

            //2.4 用户提交订单后,规格表中需要扣除库存
            itemService.decreaseItemSpecStock(itemSpecId,buyCounts);
        }

        newOrders.setTotalAmount(totalAmount);
        newOrders.setRealPayAmount(realAmount);
        ordersMapper.insert(newOrders);

        //3.保存订单状态
        OrderStatus waitForOrderStatus = new OrderStatus();
        waitForOrderStatus.setOrderId(orderId);
        waitForOrderStatus.setOrderStatus(OrderStatusEnum.WAIT_PAY.type);
        waitForOrderStatus.setCreatedTime(new Date());
        orderStatusMapper.insert(waitForOrderStatus);

        //4.创建商户订单号
        MerchantOrdersVO merchantOrdersVO = new MerchantOrdersVO();
        merchantOrdersVO.setMerchantOrderId(orderId);
        merchantOrdersVO.setMerchantUserId(userId);
        merchantOrdersVO.setAmount(realAmount + postAmount);
        merchantOrdersVO.setPayMethod(payMethod);

        OrderVO orderVO = new OrderVO();
        orderVO.setOrderId(orderId);
        orderVO.setMerchantOrdersVO(merchantOrdersVO);
        return orderVO;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateOrderStatus(String orderId, Integer orderStatus) {
        OrderStatus paidStatus = new OrderStatus();
        paidStatus.setOrderId(orderId);
        paidStatus.setOrderStatus(orderStatus);
        paidStatus.setPayTime(new Date());

        orderStatusMapper.updateByPrimaryKeySelective(paidStatus);
    }

    @Override public OrderStatus queryOrderStatusInfo(String orderId) {
        return orderStatusMapper.selectByPrimaryKey(orderId);
    }

}
