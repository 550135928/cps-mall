package com.xm.comment_serialize.module.mall.ex;

import com.xm.comment_serialize.module.mall.entity.SmProductEntity;
import lombok.Data;

@Data
public class SmProductEntityEx extends SmProductEntity {
    //用户id
    private Integer userId;
    //分享收益比例
    private Integer shareRate;
    //分享收益
    private Integer sharePrice;
    //购买收益比例
    private Integer buyRate;
    //购买收益
    private Integer buyPrice;
    //分享用户id
    private Integer shareUserId;
    //是否支持急速返现
    private Boolean creditPay;
}
