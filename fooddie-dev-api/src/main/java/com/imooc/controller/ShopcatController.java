package com.imooc.controller;

import com.imooc.pojo.bo.ShopcartBO;
import com.imooc.pojo.vo.ShopcartVO;
import com.imooc.service.ItemService;
import com.imooc.utils.IMOOCJSONResult;
import com.imooc.utils.JsonUtils;
import com.imooc.utils.RedisOperator;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @author: Young
 * @create: 2020-11-14 11:17
 **/

@Api(value = "购物车接口", tags = {"购物车接口的相关接口"})
@RestController
@RequestMapping("shopcart")
public class ShopcatController extends BaseController{

    @Autowired
    private RedisOperator redisOperator;

    @ApiOperation(value = "添加商品到购物车", notes = "添加商品到购物车", httpMethod = "POST")
    @PostMapping("/add")
    public IMOOCJSONResult add(@RequestParam String  userId,@RequestBody ShopcartBO shopcartBO, HttpServletRequest request,
        HttpServletResponse response) {

        if (StringUtils.isBlank(userId)) {
            return IMOOCJSONResult.errorMsg("");
        }

        // 购物车同步到redis
        String redisShopcartStr = redisOperator.get(FOODIE_SHOPCART + ":" + userId);
        List<ShopcartBO> redisShopcartList = null;
        //1.判断是否有购物车
        if (StringUtils.isNotBlank(redisShopcartStr)) {
            //有购物车 判断是否已有商品
            redisShopcartList = JsonUtils.jsonToList(redisShopcartStr, ShopcartBO.class);
            boolean isHaving = false;
            for (ShopcartBO sb : redisShopcartList) {
                String specId = sb.getSpecId();
                if (specId.equals(shopcartBO.getSpecId())){
                    // 有就增加数量
                    sb.setBuyCounts(sb.getBuyCounts() + shopcartBO.getBuyCounts());
                    isHaving = true;
                }
            }
            if (!isHaving) {
                //没有就直接添加
                redisShopcartList.add(shopcartBO);
            }

        } else {
            //无购物车 新建购物车并添加商品
            redisShopcartList = new ArrayList<>();
            redisShopcartList.add(shopcartBO);
        }

        redisOperator.set(FOODIE_SHOPCART + ":" + userId, JsonUtils.objectToJson(redisShopcartList));

        return IMOOCJSONResult.ok();
    }


    @ApiOperation(value = "从购物车中删除商品", notes = "从购物车中删除商品", httpMethod = "POST")
    @PostMapping("/del")
    public IMOOCJSONResult del(
        @RequestParam String userId,
        @RequestParam String itemSpecId,
        HttpServletRequest request,
        HttpServletResponse response
    ) {

        if (StringUtils.isBlank(userId) || StringUtils.isBlank(itemSpecId)) {
            return IMOOCJSONResult.errorMsg("参数不能为空");
        }

        String redisShopcartStr = redisOperator.get(FOODIE_SHOPCART + ":" + userId);

        // 用户在页面删除购物车中的商品数据，如果此时用户已经登录，则需要同步删除redis购物车中的商品
        if (StringUtils.isNotBlank(redisShopcartStr)) {
            //判断是否已有商品
            List<ShopcartBO> redisShopcartList = JsonUtils.jsonToList(redisShopcartStr, ShopcartBO.class);
            for (ShopcartBO sb : redisShopcartList) {
                String specId = sb.getSpecId();
                if (specId.equals(itemSpecId)){
                    // 删除商品
                    redisShopcartList.remove(sb);
                    break;
                }
            }
            redisOperator.set(FOODIE_SHOPCART + ":" + userId, JsonUtils.objectToJson(redisShopcartList));
        }

        return IMOOCJSONResult.ok();
    }
}
