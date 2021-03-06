package com.xm.comment_serialize.module.user.dto;

import com.xm.comment_serialize.module.user.entity.SuBillEntity;
import com.xm.comment_serialize.module.user.entity.SuOrderEntity;
import com.xm.comment_serialize.module.user.entity.SuUserEntity;
import lombok.Data;

@Data
public class OrderBillDto extends SuOrderEntity {
    private SuBillEntity suBillEntity;
    private SuUserEntity suUserEntity;
}
