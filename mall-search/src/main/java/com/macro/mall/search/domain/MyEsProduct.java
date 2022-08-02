package com.macro.mall.search.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.math.BigDecimal;

@Data
@Document(indexName = "myproduct")
@Setting(shards = 1,replicas = 0)
public class MyEsProduct {
    @Id
    private Long id;

    /*Keyword类型不会被拆分来查找，只能精确查找*/
    @Field(type = FieldType.Keyword)
    private String orderSn;

    /*Keyword类型不会被拆分来查找，只能精确查找*/
    @Field(type = FieldType.Keyword)
    private String brandName;

    /*普通属性*/
    private String picUrl;

    /*Text类型设置使用ik分词器分词后索引 ik_max_word会列举所有情况包含重复*/
    @Field(analyzer = "ik_max_word",type = FieldType.Text)
    private String name;

    /*Text类型设置使用ik分词器分词后索引 ik_max_word会列举所有情况包含重复*/
    @Field(analyzer = "ik_max_word",type = FieldType.Text)
    private String desc;

    /*普通属性*/
    private BigDecimal price;

    /*Nested嵌套属性*/
//    @Field(type =FieldType.Nested)
//    private List<EsProductAttributeValue> attrValueList;
}
