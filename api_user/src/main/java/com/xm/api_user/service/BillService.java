package com.xm.api_user.service;

import com.xm.api_user.mapper.custom.SuBillMapperEx;
import com.xm.comment_serialize.module.lottery.ex.SlPropSpecEx;
import com.xm.comment_serialize.module.pay.entity.SpWxEntPayOrderInEntity;
import com.xm.comment_serialize.module.user.bo.SuBillToPayBo;
import com.xm.comment_serialize.module.user.dto.BillOrderDto;
import com.xm.comment_serialize.module.user.dto.OrderBillDto;
import com.xm.comment_serialize.module.user.entity.SuBillEntity;
import com.xm.comment_serialize.module.user.entity.SuOrderEntity;
import com.xm.comment_serialize.module.user.vo.BillVo;
import com.xm.comment_utils.mybatis.PageBean;

import java.util.List;

public interface BillService {

    /**
     * 通过订单创建订单收益账单
     * @param order
     */
    public void createByOrder(SuOrderEntity order);

    /**
     * 创建正常购买账单
     * @param order
     */
    public void createNormalOrderBill(SuOrderEntity order);

    /**
     * 创建分享购买账单
     * @param shareUserId
     * @param order
     */
    public void createShareOrderBill(Integer shareUserId,SuOrderEntity order);

    /**
     * 订单达到成功条件
     * @param order
     */
    public void payOutOrderBill(SuOrderEntity order);
    /**
     * 订单达到失败条件
     */
    public void invalidOrderBill(SuOrderEntity order);


    public PageBean<BillVo> getList(Integer userId, Integer state, Integer type, Integer pageNum, Integer pageSize);

    /**
     * 获取账单详情
     * @param userId
     * @return
     */
    public List<BillOrderDto> getBillInfo(Integer userId, List<String> billIds);

    /**
     * 创建账单
     * @param suBillEntity
     */
    public void addBill(SuBillEntity suBillEntity);

    /**
     * 修改账单状态
     * @param suBillEntity
     * @param newState
     */
    public void updateBillState(SuBillEntity suBillEntity,Integer newState,String failReason);

    /**
     * 创建道具订单
     * @param suBillEntity
     * @return
     */
    public SuBillToPayBo createByProp(SlPropSpecEx suBillEntity);

    /**
     * 待支付账单超时
     * @param suBillEntity
     */
    public void payOvertime(SuBillEntity suBillEntity);

    /**
     * 账单支付成功(付款)
     * @param suBillEntity
     */
    public void paySucess(SuBillEntity suBillEntity);

    /**
     * 企业付款成功
     * @param spWxEntPayOrderInEntity
     */
    public void onEntPayResult(SpWxEntPayOrderInEntity spWxEntPayOrderInEntity);


    /**
     * 信用账单延时支付到期
     * @param suBillEntity
     */
    public void onCreditDelayArrived(SuBillEntity suBillEntity);

    /**
     * 根据订单状态，设置账单状态
     * @param oldOrder
     * @param newState
     */
    public void updateBillStateByOrderStateChange(SuOrderEntity oldOrder, Integer newState,String failReason);
}
