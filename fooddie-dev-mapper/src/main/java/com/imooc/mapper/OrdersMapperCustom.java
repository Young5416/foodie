package com.imooc.mapper;

import com.imooc.my.mapper.MyMapper;
import com.imooc.pojo.OrderStatus;
import com.imooc.pojo.vo.MyOrdersVO;
import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Param;

/**
 * @description:
 * @author: Young
 * @create: 2020-11-16 16:12
 **/

public interface OrdersMapperCustom{

    public List<MyOrdersVO> queryMyOrders(@Param("paramsMap") Map<String, Object> paramsMap);

    public int getMyOrderStatusCounts(@Param("paramsMap") Map<String, Object> map);

    public List<OrderStatus> getMyOrderTrend(@Param("paramsMap") Map<String, Object> map);

}
