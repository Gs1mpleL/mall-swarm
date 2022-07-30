package com.macro.mall.portal.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.common.limit.RateLimit;
import com.macro.mall.portal.service.RankService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/rank")
public class RankController {

    @Autowired
    private RankService rankService;


    @RateLimit(capacity = 10, time = 60)
    @GetMapping("/dayRank")
    public CommonResult getDayRank(){
        return CommonResult.success(rankService.getDayRank());
    }

    @GetMapping("/monthRank")
    public CommonResult getWeekRank(){
        return CommonResult.success(rankService.getMonthRank());
    }

}
