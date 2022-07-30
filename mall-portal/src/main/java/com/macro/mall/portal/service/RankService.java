package com.macro.mall.portal.service;


import com.macro.mall.portal.domain.RankCountVo;

import java.util.List;

public interface RankService {
    public List<RankCountVo> getDayRank();
    public List<RankCountVo> getMonthRank();
}
