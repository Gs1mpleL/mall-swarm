package com.macro.mall.search.repository;

import com.macro.mall.search.domain.MyEsProduct;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

/**
 * 搜索商品ES操作类
 * Created by macro on 2018/6/19.
 */
public interface MyEsProductRepository extends ElasticsearchRepository<MyEsProduct, Long> {

    List<MyEsProduct> searchByNameOrDesc(String name,String desc);

    List<MyEsProduct> searchByDesc(String desc);
}
