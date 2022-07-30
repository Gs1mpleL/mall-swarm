package com.macro.mall.portal.service.impl;

import com.macro.mall.common.domain.RedisZSetVo;
import com.macro.mall.common.service.RedisService;
import com.macro.mall.portal.domain.RankCountVo;
import com.macro.mall.portal.service.RankService;
import io.netty.util.internal.StringUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.client.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RankServiceImpl implements RankService {
    private static final String dayRankKey = "mall:portal:rank:day:%s";
    @Autowired
    private RedisService redisService;
    @Override
    public List<RankCountVo> getDayRank() {
        String formatDate = DateUtils.formatDate(new Date(), "yyyyMMdd");
        String key = String.format(dayRankKey, formatDate);
        List<RedisZSetVo> redisZSetVos = redisService.zGetTop(key, 10L);
        return redisZSetVos.stream().map(item -> {
            RankCountVo rankCountVo = new RankCountVo();
            rankCountVo.setPay(BigDecimal.valueOf(item.getValue()));
            rankCountVo.setUserId(Long.parseLong(String.valueOf( item.getKey())));
            return rankCountVo;
        }).collect(Collectors.toList());

    }

    @Override
    public List<RankCountVo> getMonthRank() {
        ArrayList<String> keys = new ArrayList<>();
        for (int i = 1; i <= LocalDate.now().getDayOfMonth(); i++) {
            LocalDate localDate = LocalDate.now().withDayOfMonth(i);
            String yyyyMMdd = localDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
            log.info("统计[{}]",yyyyMMdd);
            keys.add(String.format(dayRankKey,yyyyMMdd));
        }
        List<RedisZSetVo> redisZSetVos = redisService.zGetAllTop(keys, 30L);
        return redisZSetVos.stream().map(item -> {
            RankCountVo rankCountVo = new RankCountVo();
            rankCountVo.setPay(BigDecimal.valueOf(item.getValue()));
            rankCountVo.setUserId(Long.parseLong(String.valueOf( item.getKey())));
            return rankCountVo;
        }).collect(Collectors.toList());
    }
}
