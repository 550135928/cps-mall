package com.xm.api_user.message.handler;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import com.google.common.collect.Lists;
import com.xm.api_user.mapper.SuOrderMapper;
import com.xm.api_user.mapper.SuSummaryMapper;
import com.xm.api_user.service.MenuTipService;
import com.xm.api_user.service.SummaryService;
import com.xm.comment.utils.LockHelper;
import com.xm.comment_mq.handler.MessageHandler;
import com.xm.comment_mq.message.AbsUserActionMessage;
import com.xm.comment_mq.message.impl.*;
import com.xm.comment_serialize.module.user.constant.BillStateConstant;
import com.xm.comment_serialize.module.user.constant.BillTypeConstant;
import com.xm.comment_serialize.module.user.entity.SuBillEntity;
import com.xm.comment_serialize.module.user.entity.SuOrderEntity;
import com.xm.comment_serialize.module.user.entity.SuSummaryEntity;
import io.seata.spring.annotation.GlobalTransactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.integration.redis.util.RedisLockRegistry;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.locks.Lock;

/**
 * 计算用户收益概要
 */
@Component
public class SummaryHandler implements MessageHandler {

    @Autowired
    private SummaryService summaryService;
    @Autowired
    private SuSummaryMapper suSummaryMapper;
    @Autowired
    private SuOrderMapper suOrderMapper;
    @Autowired
    private RedisLockRegistry redisLockRegistry;

    @Override
    public List<Class> getType() {
        return Lists.newArrayList(
                UserBillCreateMessage.class,
                UserBillStateChangeMessage.class,
                UserAddProxyMessage.class
        );
    }

    @GlobalTransactional(rollbackFor = Exception.class)
    @Override
    public void handle(AbsUserActionMessage message) {
        if(message.getUserId() == null)
            return;
        Lock lock = null;
        try {
            lock = redisLockRegistry.obtain(SummaryHandler.class.getSimpleName() + ":" + message.getUserId());
            lock.lock();
            SuSummaryEntity suSummaryEntity = summaryService.getUserSummary(message.getUserId());
            if(message instanceof UserBillCreateMessage){
                UserBillCreateMessage userBillCreateMessage = (UserBillCreateMessage)message;
                SuBillEntity userBill = userBillCreateMessage.getSuBillEntity();
                if(!CollUtil.newArrayList(
                        BillTypeConstant.BUY_NORMAL,
                        BillTypeConstant.PROXY_PROFIT,
                        BillTypeConstant.BUY_SHARE,
                        BillTypeConstant.SHARE_PROFIT
                ).contains(userBill.getType()))
                    return;
                Integer billMoney = userBill.getMoney();
                //设置当天收入
                String toDayStr = DateUtil.today();
                suSummaryEntity.setProfitToday(
                        suSummaryEntity.getProfitToday() == null
                                || suSummaryEntity.getProfitToday() == 0
                                || !suSummaryEntity.getProfitTodayLastUpdate().equals(toDayStr) ? billMoney : suSummaryEntity.getProfitToday() + billMoney);
                suSummaryEntity.setProfitTodayLastUpdate(toDayStr);
                //设置历史收益
                suSummaryEntity.setProfitHistory(suSummaryEntity.getProfitHistory() == null ? billMoney : billMoney + suSummaryEntity.getProfitHistory());
                //设置等待确认
                suSummaryEntity.setProfitWait(suSummaryEntity.getProfitWait() == null ? billMoney : billMoney + suSummaryEntity.getProfitWait());

                if(CollUtil.newArrayList(BillTypeConstant.BUY_NORMAL,BillTypeConstant.BUY_SHARE).contains(userBill.getType())){
                    SuOrderEntity suOrderEntity = suOrderMapper.selectByPrimaryKey(userBill.getAttach());
                    //设置优惠券信息
                    suSummaryEntity.setTotalCoupon(suSummaryEntity.getTotalCoupon() == null ? suOrderEntity.getOriginalPrice() - suOrderEntity.getAmount() : (suOrderEntity.getOriginalPrice() - suOrderEntity.getAmount()) + suSummaryEntity.getTotalCoupon());
                    //设置自购成交额
                    suSummaryEntity.setTotalBuy(suSummaryEntity.getTotalBuy() == null ? suOrderEntity.getAmount() : suOrderEntity.getAmount() + suSummaryEntity.getTotalBuy());
                }
                //设置分享成交额
                if(userBill.getType().equals(BillTypeConstant.SHARE_PROFIT)){
                    SuOrderEntity suOrderEntity = suOrderMapper.selectByPrimaryKey(userBill.getAttach());
                    suSummaryEntity.setTotalShare(suSummaryEntity.getTotalShare() == null ? suOrderEntity.getAmount() : suOrderEntity.getAmount() + suSummaryEntity.getTotalShare());
                }

            }
            if(message instanceof UserBillStateChangeMessage){
                UserBillStateChangeMessage userBillStateChangeMessage = (UserBillStateChangeMessage)message;
                SuBillEntity userBill = userBillStateChangeMessage.getOldBill();
                if(!CollUtil.newArrayList(
                        BillTypeConstant.BUY_NORMAL,
                        BillTypeConstant.PROXY_PROFIT,
                        BillTypeConstant.BUY_SHARE,
                        BillTypeConstant.SHARE_PROFIT
                ).contains(userBill.getType()))
                    return;
                Integer billMoney = userBill.getMoney();
                if(userBill.getState().equals(BillStateConstant.WAIT)){
                    //设置等待确认
                    suSummaryEntity.setProfitWait(suSummaryEntity.getProfitWait() == null || suSummaryEntity.getProfitWait() < billMoney ? 0 : suSummaryEntity.getProfitWait() - billMoney);
                    //设置准备发放
                    if(userBillStateChangeMessage.getNewState().equals(BillStateConstant.READY)){
                        suSummaryEntity.setProfitReady(suSummaryEntity.getProfitReady() == null ? userBill.getMoney() : userBill.getMoney() + suSummaryEntity.getProfitReady());
                    }
                    //账单失败
                    if(userBillStateChangeMessage.getNewState().equals(BillStateConstant.FAIL)){
                        //设置历史收益
                        suSummaryEntity.setProfitHistory(suSummaryEntity.getProfitHistory() == null || suSummaryEntity.getProfitHistory() < billMoney ? 0 : suSummaryEntity.getProfitHistory() - billMoney);
                        if(CollUtil.newArrayList(
                                BillTypeConstant.BUY_NORMAL,
                                BillTypeConstant.BUY_SHARE).contains(userBill.getType())){
                            //设置优惠券
                            SuOrderEntity suOrderEntity = suOrderMapper.selectByPrimaryKey(userBill.getAttach());
                            suSummaryEntity.setTotalCoupon(suSummaryEntity.getTotalCoupon() == null || suSummaryEntity.getTotalCoupon() < (suOrderEntity.getOriginalPrice() - suOrderEntity.getAmount()) ? 0 : suSummaryEntity.getTotalCoupon() - (suOrderEntity.getOriginalPrice() - suOrderEntity.getAmount()));
                            //设置自购成交额
                            suSummaryEntity.setTotalBuy(suSummaryEntity.getTotalBuy() == null || suSummaryEntity.getTotalBuy() < billMoney ? 0 : suSummaryEntity.getTotalBuy() - suOrderEntity.getAmount());
                        }
                        //设置分享成交额
                        if(userBill.getType().equals(BillTypeConstant.SHARE_PROFIT)){
                            SuOrderEntity suOrderEntity = suOrderMapper.selectByPrimaryKey(userBill.getAttach());
                            suSummaryEntity.setTotalShare(suSummaryEntity.getTotalShare() == null || suSummaryEntity.getTotalShare() < billMoney ? 0 : suSummaryEntity.getTotalShare() - suOrderEntity.getAmount());
                        }
                    }

                }
                if(userBill.getState().equals(BillStateConstant.READY) && userBillStateChangeMessage.getNewState().equals(BillStateConstant.ALREADY)){
                    //设置已返
                    suSummaryEntity.setProfitCash(suSummaryEntity.getProfitCash() == null ? userBill.getMoney() : userBill.getMoney() + suSummaryEntity.getProfitCash());
                    //设置准备发放
                    suSummaryEntity.setProfitReady(suSummaryEntity.getProfitReady() == null || suSummaryEntity.getProfitReady() < billMoney ? 0 : suSummaryEntity.getProfitReady() - userBill.getMoney());
                }
            }

            if(message instanceof UserAddProxyMessage){
                UserAddProxyMessage userAddProxyMessage = (UserAddProxyMessage)message;
                //设置锁定用户
                suSummaryEntity.setTotalProxyUser(suSummaryEntity.getTotalProxyUser() == null ? 1 : suSummaryEntity.getTotalProxyUser() + 1);
            }
            summaryService.updateSummary(suSummaryEntity);
        }finally {
            if(lock != null)
                lock.unlock();
        }
    }


    @Override
    public void onError(Exception e) {

    }
}
