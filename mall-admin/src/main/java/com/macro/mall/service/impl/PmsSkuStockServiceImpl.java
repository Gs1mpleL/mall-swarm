package com.macro.mall.service.impl;

import com.alibaba.otter.canal.protocol.CanalEntry;
import com.macro.mall.component.WebSocket;
import com.macro.mall.dao.PmsSkuStockDao;
import com.macro.mall.mapper.PmsSkuStockMapper;
import com.macro.mall.model.PmsSkuStock;
import com.macro.mall.model.PmsSkuStockExample;
import com.macro.mall.service.PmsSkuStockService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Set;

/**
 * 商品sku库存管理Service实现类
 * Created by macro on 2018/4/27.
 */
@Service
@Slf4j
public class PmsSkuStockServiceImpl implements PmsSkuStockService {
    @Autowired
    private WebSocket webSocket;
    @Autowired
    private PmsSkuStockMapper skuStockMapper;
    @Autowired
    private PmsSkuStockDao skuStockDao;

    @Override
    public List<PmsSkuStock> getList(Long pid, String keyword) {
        PmsSkuStockExample example = new PmsSkuStockExample();
        PmsSkuStockExample.Criteria criteria = example.createCriteria().andProductIdEqualTo(pid);
        if (!StringUtils.isEmpty(keyword)) {
            criteria.andSkuCodeLike("%" + keyword + "%");
        }
        return skuStockMapper.selectByExample(example);
    }

    @Override
    public int update(Long pid, List<PmsSkuStock> skuStockList) {
        return skuStockDao.replaceList(skuStockList);
    }

    @Override
    public void listenStock(CanalEntry.RowData rowDatas) {
        // 获取之前的库存
        String id = rowDatas.getAfterColumns(0).getValue();
        Integer beforeValue = Integer.parseInt(rowDatas.getBeforeColumns(4).getValue());
        Integer afterValue = Integer.parseInt(rowDatas.getAfterColumns(4).getValue());
        Integer lowValue = Integer.parseInt(rowDatas.getAfterColumns(5).getValue());
        // 库存发生变化
        if (!beforeValue.equals(afterValue) ){
            if (afterValue < beforeValue){
                log.info("[{}]的库存减少 [{}] -> [{}]", id, beforeValue, afterValue);
                // 库存预警 库存少于20件
                if (afterValue <= lowValue){
                    log.info("库存预警，[{}]号商品库存剩余[{}],达到告警阈值[{}]",id,afterValue,lowValue);
                }
                Set<String> allUserList = webSocket.getAllUserList();
                webSocket.sendMoreMessage(allUserList,"库存预警，[{"+id+"}]号商品库存剩余[{"+afterValue+"}],达到告警阈值[{"+lowValue+"}]");
            }else {
                // 货物补充
                log.info("[{}]号商品补货[{}]件",id,afterValue-beforeValue);
            }
        }
    }
}
