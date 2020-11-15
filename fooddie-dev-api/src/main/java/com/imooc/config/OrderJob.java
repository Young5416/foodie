package com.imooc.config;

import com.imooc.service.OrderService;
import com.imooc.utils.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @description:
 * @author: Young
 * @create: 2020-11-15 22:46
 **/

@Component
public class OrderJob {

    @Autowired
    private OrderService orderService;


    //TODO 后续使用MQ关闭订单

//    @Scheduled(cron = "0 0 0/1 * * ?")
    public void autoCloserOrder() {
        orderService.closeOrder();
        System.out.println("执行定时任务,当前时间为:" + DateUtil.getCurrentDateString(DateUtil.DATETIME_PATTERN));
    }
}
