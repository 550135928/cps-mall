package com.xm.api_mall.service.api.impl.pdd;

import cn.hutool.core.collection.CollUtil;
import com.xm.api_mall.component.PddSdkComponent;
import com.xm.api_mall.service.api.OptGoodsListService;
import com.xm.comment_serialize.module.mall.form.GoodsListForm;
import com.xm.comment_serialize.module.mall.form.ThemeGoodsListForm;
import com.xm.comment_serialize.module.mall.bo.ProductCriteriaBo;
import com.xm.comment_serialize.module.mall.ex.SmProductEntityEx;
import com.xm.comment_utils.mybatis.PageBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("pddOptGoodsListService")
public class OptGoodsListServiceImpl implements OptGoodsListService {

    @Autowired
    private PddSdkComponent pddSdkComponent;

    @Override
    public PageBean<SmProductEntityEx> theme(ThemeGoodsListForm themeGoodsListForm) throws Exception {
        return pddSdkComponent.convertSmProductEntityEx(
                themeGoodsListForm.getUserId(),
                pddSdkComponent.getThemeGoodsList(
                        themeGoodsListForm.getThemeId(),
                        themeGoodsListForm.getPid()));
    }

    @Override
    public PageBean<SmProductEntityEx> one(GoodsListForm goodsListForm) throws Exception {
        return getByCriteria(goodsListForm,CollUtil.newArrayList(4));
    }

    @Override
    public PageBean<SmProductEntityEx> two(GoodsListForm goodsListForm) throws Exception {
        return getByCriteria(goodsListForm,CollUtil.newArrayList(7));
    }

    @Override
    public PageBean<SmProductEntityEx> three(GoodsListForm goodsListForm) throws Exception {
        return pddSdkComponent.convertSmProductEntityEx(
                goodsListForm.getUserId(),
                pddSdkComponent.getRecommendGoodsList(
                        goodsListForm.getPid(),
                        0,
                        goodsListForm.getPageNum(),
                        goodsListForm.getPageSize()));

    }

    @Override
    public PageBean<SmProductEntityEx> four(GoodsListForm goodsListForm) throws Exception {
        return pddSdkComponent.convertSmProductEntityEx(
                goodsListForm.getUserId(),
                pddSdkComponent.getRecommendGoodsList(
                        goodsListForm.getPid(),
                        1,
                        goodsListForm.getPageNum(),
                        goodsListForm.getPageSize()));
    }

    @Override
    public PageBean<SmProductEntityEx> five(GoodsListForm goodsListForm) throws Exception {
        return pddSdkComponent.convertSmProductEntityEx(
                goodsListForm.getUserId(),
                pddSdkComponent.getRecommendGoodsList(
                        goodsListForm.getPid(),
                        2,
                        goodsListForm.getPageNum(),
                        goodsListForm.getPageSize()));
    }

    private PageBean<SmProductEntityEx> getByCriteria(GoodsListForm goodsListForm, List<Integer> activityTags) throws Exception {
        ProductCriteriaBo productCriteriaBo = new ProductCriteriaBo();
        productCriteriaBo.setActivityTags(activityTags);
        productCriteriaBo.setPid(goodsListForm.getPid());
        productCriteriaBo.setUserId(goodsListForm.getUserId());
        productCriteriaBo.setPageNum(goodsListForm.getPageNum());
        productCriteriaBo.setPageSize(goodsListForm.getPageSize());
        return pddSdkComponent.convertSmProductEntityEx(
                goodsListForm.getUserId(),
                pddSdkComponent.getProductByCriteria(productCriteriaBo));
    }

}
