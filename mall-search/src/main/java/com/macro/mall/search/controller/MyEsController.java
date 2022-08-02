package com.macro.mall.search.controller;

import com.macro.mall.common.api.CommonResult;
import com.macro.mall.search.domain.MyEsProduct;
import com.macro.mall.search.service.EsProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 搜索商品管理Controller
 * Created by macro on 2018/6/19.
 */
@RestController
@RequestMapping("/es")
public class MyEsController {
    @Autowired
    private EsProductService esProductService;

    @PostMapping(value = "/add")
    public CommonResult add(MyEsProduct myEsProduct){
        return CommonResult.success(esProductService.add(myEsProduct));
    }

    @GetMapping("/search")
    public CommonResult search(String name,String desc){
        return CommonResult.success(esProductService.mySearch(name,desc));
    }

}
