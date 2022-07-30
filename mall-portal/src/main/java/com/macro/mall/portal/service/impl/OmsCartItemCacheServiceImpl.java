package com.macro.mall.portal.service.impl;

import com.alibaba.fastjson.JSON;
import com.macro.mall.common.exception.ApiException;
import com.macro.mall.common.service.RedisService;
import com.macro.mall.model.OmsCartItem;
import com.macro.mall.portal.domain.CartProduct;
import com.macro.mall.portal.domain.CartPromotionItem;
import com.macro.mall.portal.service.OmsCartItemService;
import com.macro.mall.portal.util.UserUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 基于Redis的Hash实现的购物车功能
 * 这里的商品内容就不考虑那么多了
 * [商品数量],[商品SKU],[商品名称],[商品最终单价]
 */
@Service
@Primary
public class OmsCartItemCacheServiceImpl implements OmsCartItemService {
    @Autowired
    private RedisService redisService;

    private static final String cartStr = "mall:portal:cart:%s";


    @Override
    public int add(OmsCartItem cartItem) {
        String key = String.format(cartStr, UserUtils.getUserDetail().getId());
        try {
            redisService.hSet(key, String.valueOf(cartItem.getProductSkuId()), JSON.toJSONString(cartItem));
            return 1;
        }catch (Exception e){
            throw new ApiException("添加购物车失败");
        }
    }

    @Override
    public List<OmsCartItem> list(Long memberId) {
        String key = String.format(cartStr, UserUtils.getUserDetail().getId());
        Map<Object, Object> objectObjectMap = redisService.hGetAll(key);
        return objectObjectMap.values().stream().map(item -> {
//            return (OmsCartItem) item;
            return JSON.parseObject((String) item, OmsCartItem.class);
        }).collect(Collectors.toList());
    }

    @Override
    public List<CartPromotionItem> listPromotion(Long memberId, List<Long> cartIds) {
        return null;
    }

    @Override
    public int updateQuantity(Long id, Long memberId, Integer quantity) {
        String key = String.format(cartStr, UserUtils.getUserDetail().getId());
        String string = (String) redisService.hGet(key, String.valueOf(id));
        OmsCartItem omsCartItem = JSON.parseObject(string, OmsCartItem.class);
        omsCartItem.setQuantity(quantity);
        redisService.hSet(key,String.valueOf(id),omsCartItem);
        return 1;
    }

    @Override
    public int delete(Long memberId, List<Long> ids) {
        return 0;
    }

    @Override
    public CartProduct getCartProduct(Long productId) {
        return null;
    }

    @Override
    public int updateAttr(OmsCartItem cartItem) {
        return 0;
    }

    @Override
    public int clear() {
        String key = String.format(cartStr, UserUtils.getUserDetail().getId());
        try{
            redisService.del(key);
            return 1;
        }catch (Exception e){
            e.printStackTrace();
            throw new ApiException("清空购物车失败");
        }
    }
}
