package com.xm.wind_control.controller;

import com.alibaba.fastjson.JSON;
import com.github.pagehelper.PageHelper;
import com.xm.comment.annotation.LoginUser;
import com.xm.comment_serialize.form.BaseForm;
import com.xm.comment_serialize.module.mall.ex.SmProductEntityEx;
import com.xm.comment_serialize.module.mall.form.ListForm;
import com.xm.comment_serialize.module.wind.constant.CreditConfigEnmu;
import com.xm.comment_serialize.module.wind.entity.SwCreditBillBindRecordEntity;
import com.xm.comment_serialize.module.wind.entity.SwCreditBillConfEntity;
import com.xm.comment_serialize.module.wind.entity.SwCreditRecordEntity;
import com.xm.comment_serialize.module.wind.form.CreditBindListForm;
import com.xm.comment_serialize.module.wind.vo.UserCreditVo;
import com.xm.comment_utils.mybatis.PageBean;
import com.xm.wind_control.mapper.SwCreditBillBindRecordMapper;
import com.xm.wind_control.mapper.SwCreditBillConfMapper;
import com.xm.wind_control.mapper.SwCreditRecordMapper;
import com.xm.wind_control.service.CreditBillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import tk.mybatis.orderbyhelper.OrderByHelper;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/credit")
public class CreditController {

    @Autowired
    private SwCreditRecordMapper swCreditRecordMapper;
    @Autowired
    private SwCreditBillBindRecordMapper swCreditBillBindRecordMapper;
    @Autowired
    private CreditBillService creditBillService;
    @Autowired
    private SwCreditBillConfMapper swCreditBillConfMapper;

    @GetMapping
    public UserCreditVo myCredit(@LoginUser BaseForm baseForm){
        UserCreditVo result = new UserCreditVo();
        SwCreditRecordEntity userCredit = creditBillService.getUserCredit(baseForm.getUserId());
        SwCreditBillConfEntity billConfEntity = creditBillService.getConfByScores(userCredit);
        result.setScores(userCredit.getScores());
        result.setSwCreditBillConfEntity(billConfEntity);
        result.setGetCredit(JSON.parseArray(creditBillService.getConfig(CreditConfigEnmu.GET_CREDIT_METHOD).getVal()).toJavaList(String.class));
        if(billConfEntity == null) {
            result.setDesc(creditBillService.getConfig(CreditConfigEnmu.DEFAULT_CREDIT_NO_CONF_DESC).getVal());
        }else {
            result.setDesc(creditBillService.getConfig(CreditConfigEnmu.DEFAULT_CREDIT_HAS_CONF_DESC).getVal());
        }
        SwCreditBillBindRecordEntity bindExample = new SwCreditBillBindRecordEntity();
        bindExample.setUserId(baseForm.getUserId());
        bindExample.setState(1);
        List<SwCreditBillBindRecordEntity> swCreditBillBindRecordEntities = swCreditBillBindRecordMapper.select(bindExample);
        result.setBindCount(swCreditBillBindRecordEntities == null ? 0 : swCreditBillBindRecordEntities.size());
        result.setBindTotalMoney(swCreditBillBindRecordEntities == null ? 0 : swCreditBillBindRecordEntities.stream().mapToInt(SwCreditBillBindRecordEntity::getBillMoney).sum());
        return result;
    }

    @GetMapping("/desc")
    public List<SwCreditBillConfEntity> creditBillConfEntities(@LoginUser BaseForm baseForm){
        OrderByHelper.orderBy("scores asc");
        return swCreditBillConfMapper.selectAll();
    }

    @GetMapping("/history")
    public PageBean historyList(@LoginUser ListForm listForm){
        SwCreditRecordEntity example = new SwCreditRecordEntity();
        example.setUserId(listForm.getUserId());
        OrderByHelper.orderBy("create_stamp+0 desc");
        PageHelper.startPage(listForm.getPageNum(),listForm.getPageSize());
        List<SwCreditRecordEntity> recordEntities =  swCreditRecordMapper.select(example);
        PageBean pageBean = new PageBean(recordEntities);
        return pageBean;
    }

    @GetMapping("/bind")
    public PageBean bindList(@LoginUser CreditBindListForm listForm){
        SwCreditBillBindRecordEntity example = new SwCreditBillBindRecordEntity();
        example.setUserId(listForm.getUserId());
        example.setState(listForm.getState());
        OrderByHelper.orderBy("create_time desc");
        PageHelper.startPage(listForm.getPageNum(),listForm.getPageSize());
        List<SwCreditBillBindRecordEntity> recordEntities =  swCreditBillBindRecordMapper.select(example);
        PageBean pageBean = new PageBean(recordEntities);
        return pageBean;
    }

    @PostMapping("/product/check")
    public List<SmProductEntityEx> productCheck(@RequestBody List<SmProductEntityEx> smProductEntityExes){
        if(smProductEntityExes == null || smProductEntityExes.isEmpty())
            return smProductEntityExes;
        return creditBillService.productCheck(smProductEntityExes);
    }
}
