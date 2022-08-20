package com.macro.mall.portal.service.impl;

import cn.hutool.core.util.BooleanUtil;
import com.alibaba.fastjson2.JSON;
import com.macro.mall.common.api.CommonPage;
import com.macro.mall.common.exception.Asserts;
import com.macro.mall.common.service.RedisService;
import com.macro.mall.mapper.OmsOrderMapper;
import com.macro.mall.mapper.PmsSkuStockMapper;
import com.macro.mall.model.OmsCartItem;
import com.macro.mall.model.OmsOrder;
import com.macro.mall.model.PmsSkuStock;
import com.macro.mall.portal.component.CancelOrderSender;
import com.macro.mall.portal.domain.ConfirmOrderResult;
import com.macro.mall.portal.domain.OmsOrderDetail;
import com.macro.mall.portal.domain.OrderParam;
import com.macro.mall.portal.service.OmsCartItemService;
import com.macro.mall.portal.service.OmsPortalOrderService;
import com.macro.mall.portal.util.UserUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@Primary
public class MyOrderServiceImpl implements OmsPortalOrderService {
    @Autowired
    private RedisService redisService;
    @Autowired
    private OmsOrderMapper orderMapper;
    @Autowired
    private PmsSkuStockMapper skuStockMapper;
    @Autowired
    private OmsCartItemService cartItemService;
    @Autowired
    private CancelOrderSender cancelOrderSender;
    private static final String tokenKey = "mall:portal:orderGenToken:%s:%s";
    private static final String cartStr = "mall:portal:cart:%s";
    private static final String rankStr = "mall:portal:rank:day:%s";
    @Override
    public ConfirmOrderResult generateConfirmOrder(List<Long> cartIds) {
        return null;
    }

    @Override
    public OmsOrder generateOrder(OrderParam orderParam) {
        if (BooleanUtil.isFalse(orderParam.isSeckill()) && !redisService.setIfAbs(String.format(tokenKey, UserUtils.getUserDetail().getId(),orderParam.getToken()),1,10*60L)) {
            Asserts.fail("下单中,请勿重复提交订单");
        }

        if (BooleanUtil.isTrue(orderParam.isSeckill())){
            log.info("秒杀消息生成订单!");
            log.info("生成一个订单......");
            return new OmsOrder();
        }


        Map<Object, Object> objectObjectMap = redisService.hGetAll(String.format(cartStr, UserUtils.getUserDetail().getId()));

        if (CollectionUtils.isEmpty(objectObjectMap.values())) {
            Asserts.fail("购物车为空,无法创建订单");
        }
        List<OmsCartItem> cartItems = objectObjectMap.values().stream().map(item -> JSON.parseObject((String) item, OmsCartItem.class)).collect(Collectors.toList());
        for (OmsCartItem cartItem : cartItems) {
            PmsSkuStock pmsSkuStock = skuStockMapper.selectByPrimaryKey(cartItem.getProductSkuId());
            // 下单数量
            Integer quantity = cartItem.getQuantity();
            pmsSkuStock.setLockStock(quantity + pmsSkuStock.getLockStock());
            if (pmsSkuStock.getStock()-pmsSkuStock.getLockStock() < 0) {
                Asserts.fail("下单失败,[" + cartItem.getProductName() + "]存货不足");
            }
            // 锁定库存存货重新赋值
            skuStockMapper.updateByPrimaryKey(pmsSkuStock);
        }
        String collect = cartItems.stream()
                .map(omsCartItem -> String.valueOf(omsCartItem.getProductSkuId()))
                .collect(Collectors.joining(","));
        OmsOrder omsOrder = new OmsOrder();
        // 测试  就直接用发票内容当订单的商品项了
        omsOrder.setBillContent(collect);
        String countList = cartItems.stream()
                .map(omsCartItem -> String.valueOf(omsCartItem.getQuantity()))
                .collect(Collectors.joining(","));
        omsOrder.setBillHeader(countList);
        omsOrder.setOrderSn(generateOrderSn());
        omsOrder.setMemberId(UserUtils.getUserDetail().getId());
        // 等待付款
        omsOrder.setStatus(0);
        // 总价
        BigDecimal pay = new BigDecimal(0);
        for (OmsCartItem cartItem : cartItems) {
            BigDecimal price = cartItem.getPrice();
            pay = pay.add(price);
        }
        omsOrder.setPayAmount(pay);
        String formatDate = DateUtils.formatDate(new Date(), "yyyyMMdd");
        String key = String.format(rankStr, formatDate);
        redisService.zSet(key,UserUtils.getUserDetail().getId(),pay.doubleValue());
        omsOrder.setReceiverName("收货人");
        omsOrder.setReceiverPhone("18322126876");
        omsOrder.setDeleteStatus(0);
        // 保存订单信息
        orderMapper.insert(omsOrder);
        log.info("保存订单信息[{}]",omsOrder);

        // 发送延时消息，去删除订单
        sendDelayMessageCancelOrder(omsOrder.getId());

        return omsOrder;
    }
    /**
     * 生成订单编号:8位日期+6位以上自增id
     */
    private String generateOrderSn() {
        StringBuilder sb = new StringBuilder();
        String date = new SimpleDateFormat("yyyyMMdd").format(new Date());
        String key = "mall:portal:order:orderSn" + date;
        Long increment = redisService.incr(key, 1);
        sb.append(date);
        String incrementStr = increment.toString();
        if (incrementStr.length() <= 6) {
            sb.append(String.format("%06d", increment));
        } else {
            sb.append(incrementStr);
        }
        return sb.toString();
    }
    @Override
    public Integer paySuccess(Long orderId, Integer payType) {
        return null;
    }

    @Override
    public Integer cancelTimeOutOrder() {
        return null;
    }

    @Override
    public void cancelOrder(Long orderId) {
        OmsOrder omsOrder = orderMapper.selectByPrimaryKey(orderId);
        if (omsOrder.getStatus() == 0) {
            // 处于未支付状态
            log.info("订单[{}]未支付",orderId);
            orderMapper.deleteByPrimaryKey(orderId);
            String skuIds = omsOrder.getBillContent();
            String skuCounts = omsOrder.getBillHeader();
            String[] skuId = skuIds.split(",");
            String[] skuCount = skuCounts.split(",");
            for (int i = 0; i < skuId.length; i++) {
                PmsSkuStock pmsSkuStock = skuStockMapper.selectByPrimaryKey(Long.parseLong(skuId[i]));
                pmsSkuStock.setLockStock(pmsSkuStock.getLockStock() - Integer.parseInt(skuCount[i]));
                skuStockMapper.updateByPrimaryKey(pmsSkuStock);
                log.info("解锁[{}]的库存[{}]件",skuId[i],skuCount[i]);
            }
        }
    }

    @Override
    public void sendDelayMessageCancelOrder(Long orderId) {
        cancelOrderSender.sendMessage(orderId,10000);
    }

    @Override
    public void confirmReceiveOrder(Long orderId) {

    }

    @Override
    public CommonPage<OmsOrderDetail> list(Integer status, Integer pageNum, Integer pageSize) {
        return null;
    }

    @Override
    public OmsOrderDetail detail(Long orderId) {
        return null;
    }

    @Override
    public void deleteOrder(Long orderId) {

    }
}