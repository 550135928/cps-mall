package com.xm.comment_serialize.module.mall.bo;

import lombok.Data;

import java.util.List;

@Data
public class ProductCriteriaBo {
    private Integer userId;
    private Integer pageNum;
    private Integer pageSize;

    private Integer optionId;
    private String keyword;
    private Integer orderBy;
    private Integer minPrice;
    private Integer maxPrice;
    private List<Long> goodsIdList;

}