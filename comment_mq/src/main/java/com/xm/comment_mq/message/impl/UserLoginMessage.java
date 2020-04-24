package com.xm.comment_mq.message.impl;

import com.xm.comment_mq.message.AbsUserActionMessage;
import com.xm.comment_mq.message.UserActionEnum;
import com.xm.comment_serialize.module.user.entity.SuUserEntity;
import lombok.Data;

@Data
public class UserLoginMessage extends AbsUserActionMessage {

    public UserLoginMessage(){}

    public UserLoginMessage(Integer userId, SuUserEntity suUserEntity) {
        super(userId);
        this.suUserEntity = suUserEntity;
    }

    private final UserActionEnum userActionEnum = UserActionEnum.USER_LOGIN;
    private SuUserEntity suUserEntity;
}
