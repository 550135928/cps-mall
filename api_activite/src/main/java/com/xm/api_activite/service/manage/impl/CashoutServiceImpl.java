package com.xm.api_activite.service.manage.impl;

import com.xm.api_activite.mapper.SaCashOutRecordMapper;
import com.xm.api_activite.service.manage.CashoutService;
import com.xm.comment_mq.message.config.PayMqConfig;
import com.xm.comment_serialize.module.active.entity.SaCashOutRecordEntity;
import com.xm.comment_serialize.module.pay.message.ActiveEntPayMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service("cashoutService")
public class CashoutServiceImpl implements CashoutService {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private SaCashOutRecordMapper saCashOutRecordMapper;

    @Override
    public void approval(List<Integer> cashoutRecordIds) {
        Example example = new Example(SaCashOutRecordEntity.class);
        example.createCriteria().andIn("id",cashoutRecordIds);
        List<SaCashOutRecordEntity> list = saCashOutRecordMapper.selectByExample(example);
        if(list == null || list.isEmpty())
            return;
        for (SaCashOutRecordEntity cashOutRecordEntity : list) {
            ActiveEntPayMessage message = new ActiveEntPayMessage();
            message.setDesc("粉饰生活-活动奖励-提现成功");
            message.setIp(cashOutRecordEntity.getIp());
            message.setUserId(cashOutRecordEntity.getUserId());
            message.setSaCashOutRecordEntity(cashOutRecordEntity);
            rabbitTemplate.convertAndSend(PayMqConfig.EXCHANGE,PayMqConfig.KEY_WX_ENT_PAY_ACTIVE,message);
        }

    }
}
