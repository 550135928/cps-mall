package com.xm.wind_control.message;

import com.rabbitmq.client.Channel;
import com.xm.comment_mq.constant.RabbitMqConstant;
import com.xm.comment_mq.message.config.OrderMqConfig;
import com.xm.comment_mq.message.config.WindMqConfig;
import com.xm.comment_serialize.module.user.entity.SuOrderEntity;
import com.xm.comment_serialize.module.wind.entity.SwApiRecordEntity;
import com.xm.comment_serialize.module.wind.entity.SwLoginRecordEntity;
import com.xm.comment_utils.ip.IpUtil;
import com.xm.wind_control.mapper.SwApiRecordMapper;
import com.xm.wind_control.mapper.SwLoginRecordMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 保存API请求记录、用户登录记录
 */
@Slf4j
@Component
public class ApiRecordReceiver {

    @Autowired
    private SwApiRecordMapper swApiRecordMapper;
    @Autowired
    private SwLoginRecordMapper swLoginRecordMapper;

    /**
     * 保存用户API请求记录
     * @param swApiRecordEntity
     * @param channel
     * @param message
     * @throws IOException
     */
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(WindMqConfig.EXCHANGE),
            key = WindMqConfig.KEY_API,
            value = @Queue(value = WindMqConfig.QUEUE_API)
    ))
    public void onApiMessage(SwApiRecordEntity swApiRecordEntity, Channel channel, Message message) throws IOException {
        Long msgId = message.getMessageProperties().getDeliveryTag();
        try{
            swApiRecordEntity.setIpAddr(IpUtil.getCityInfo(swApiRecordEntity.getIp()));
            swApiRecordMapper.insertSelective(swApiRecordEntity);
        }catch (Exception e){
            channel.basicReject(msgId,false);
            log.error("消息：{} 风控平台API记录保存失败 处理失败 error：{}",msgId,e);
        } finally {
            channel.basicAck(msgId,false);
        }
    }

    /**
     * 保存用户登录记录
     * @param swLoginRecordEntity
     * @param channel
     * @param message
     * @throws IOException
     */
    @RabbitListener(bindings = @QueueBinding(
            exchange = @Exchange(WindMqConfig.EXCHANGE),
            key = WindMqConfig.KEY_LOGIN,
            value = @Queue(value = WindMqConfig.QUEUE_LOGIN)
    ))
    public void onLoginMessage(SwLoginRecordEntity swLoginRecordEntity, Channel channel, Message message) throws IOException {
        Long msgId = message.getMessageProperties().getDeliveryTag();
        try{
            swLoginRecordEntity.setIpAddr(IpUtil.getCityInfo(swLoginRecordEntity.getIp()));
            swLoginRecordMapper.insertSelective(swLoginRecordEntity);
        }catch (Exception e){
            channel.basicReject(msgId,false);
            log.error("消息：{} 风控平台用户登录记录保存失败 处理失败 error：{}",msgId,e);
        } finally {
            channel.basicAck(msgId,false);
        }
    }

}
