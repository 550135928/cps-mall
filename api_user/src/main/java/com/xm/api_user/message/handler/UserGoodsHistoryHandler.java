package com.xm.api_user.message.handler;

import com.xm.api_user.service.ProductService;
import com.xm.comment_mq.handler.MessageHandler;
import com.xm.comment_mq.message.AbsUserActionMessage;
import com.xm.comment_mq.message.impl.UserClickGoodsMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * 用户浏览商品历史记录
 */
@Component
public class UserGoodsHistoryHandler implements MessageHandler {

    @Autowired
    private ProductService productService;

    @Override
    public List<Class> getType() {
        return Arrays.asList(UserClickGoodsMessage.class);
    }

    @Override
    public void handle(AbsUserActionMessage message) {
        if(!(message instanceof UserClickGoodsMessage))
            return;
        UserClickGoodsMessage userClickGoodsMessage = (UserClickGoodsMessage)message;
        productService.addOrUpdateHistory(
                userClickGoodsMessage.getUserId(),
                userClickGoodsMessage.getFromUserId(),
                userClickGoodsMessage.getIp(),
                userClickGoodsMessage.getAppType(),
                userClickGoodsMessage.getSmProductEntityEx());
    }

    @Override
    public void onError(Exception e) {

    }
}
