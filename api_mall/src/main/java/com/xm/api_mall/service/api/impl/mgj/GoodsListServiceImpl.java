package com.xm.api_mall.service.api.impl.mgj;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.xm.api_mall.component.MgjSdkComponent;
import com.xm.api_mall.component.PddSdkComponent;
import com.xm.api_mall.mapper.SmOptMapper;
import com.xm.api_mall.service.api.GoodsListService;
import com.xm.api_mall.service.api.OptionService;
import com.xm.comment_feign.module.user.feign.UserFeignClient;
import com.xm.comment_serialize.module.mall.bo.ProductCriteriaBo;
import com.xm.comment_serialize.module.mall.constant.GoodsSortContant;
import com.xm.comment_serialize.module.mall.entity.SmOptEntity;
import com.xm.comment_serialize.module.mall.entity.SmProductEntity;
import com.xm.comment_serialize.module.mall.ex.SmProductEntityEx;
import com.xm.comment_serialize.module.mall.form.*;
import com.xm.comment_serialize.module.user.form.AddSearchForm;
import com.xm.comment_utils.exception.GlobleException;
import com.xm.comment_utils.mybatis.PageBean;
import com.xm.comment_utils.response.MsgEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service("mgjGoodsListService")
public class GoodsListServiceImpl implements GoodsListService {

    @Autowired
    private MgjSdkComponent mgjSdkComponent;
    @Autowired
    private UserFeignClient userFeignClient;
    @Autowired
    private OptionService optionService;

    @Override
    public PageBean<SmProductEntityEx> index(GoodsListForm goodsListForm) throws Exception {
        ProductCriteriaBo productCriteriaBo = new ProductCriteriaBo();
        productCriteriaBo.setPid(goodsListForm.getPid());
        productCriteriaBo.setUserId(goodsListForm.getUserId());
        productCriteriaBo.setPageNum(goodsListForm.getPageNum());
        productCriteriaBo.setPageSize(goodsListForm.getPageSize());
        productCriteriaBo.setHasCoupon(true);
        productCriteriaBo.setOrderBy(GoodsSortContant.SALES_DESC);
        return mgjSdkComponent.convertSmProductEntityEx(goodsListForm.getUserId(),mgjSdkComponent.getProductByCriteria(productCriteriaBo));
    }

    @Override
    public PageBean<SmProductEntityEx> recommend(GoodsListForm goodsListForm) throws Exception {
        ProductCriteriaBo productCriteriaBo = new ProductCriteriaBo();
        productCriteriaBo.setPid(goodsListForm.getPid());
        productCriteriaBo.setUserId(goodsListForm.getUserId());
        productCriteriaBo.setPageNum(goodsListForm.getPageNum());
        productCriteriaBo.setPageSize(goodsListForm.getPageSize());
        productCriteriaBo.setHasCoupon(true);
        productCriteriaBo.setOrderBy(GoodsSortContant.PRICE_ASC);
        return mgjSdkComponent.convertSmProductEntityEx(goodsListForm.getUserId(),mgjSdkComponent.getProductByCriteria(productCriteriaBo));
    }

    @Override
    public PageBean<SmProductEntityEx> my(GoodsListForm goodsListForm) throws Exception {
        return index(goodsListForm);
    }

    @Override
    public PageBean<SmProductEntityEx> keyword(KeywordGoodsListForm keywordGoodsListForm) throws Exception {
        if(keywordGoodsListForm.getKeywords() == null || keywordGoodsListForm.getKeywords().trim().equals(""))
            throw new GlobleException(MsgEnum.PARAM_VALID_ERROR,"keywords 不能为空");
        //添加搜索历史
        if(keywordGoodsListForm.getPageNum() == 1 && keywordGoodsListForm.getUserId() != null){
            //只在搜索第一页添加
            AddSearchForm addSearchForm = new AddSearchForm();
            addSearchForm.setKeyWords(keywordGoodsListForm.getKeywords());
            userFeignClient.addSearch(keywordGoodsListForm.getUserId(),addSearchForm);
        }
        return keyworkSearch(keywordGoodsListForm.getUserId(),keywordGoodsListForm.getPid(),keywordGoodsListForm);
    }

    private PageBean<SmProductEntityEx> keyworkSearch(Integer userId,String pid, KeywordGoodsListForm keywordGoodsListForm) throws Exception {
        ProductCriteriaBo productCriteriaBo = new ProductCriteriaBo();
        productCriteriaBo.setPid(pid);
        productCriteriaBo.setUserId(userId);
        productCriteriaBo.setPageNum(keywordGoodsListForm.getPageNum());
        productCriteriaBo.setPageSize(keywordGoodsListForm.getPageSize());
        productCriteriaBo.setOrderBy(keywordGoodsListForm.getSort());
        productCriteriaBo.setHasCoupon(keywordGoodsListForm.getHasCoupon());
        if(keywordGoodsListForm.getMinPrice() != null && keywordGoodsListForm.getMaxPrice() != null){
            productCriteriaBo.setMinPrice(keywordGoodsListForm.getMinPrice());
            productCriteriaBo.setMaxPrice(keywordGoodsListForm.getMaxPrice());
        }
        productCriteriaBo.setKeyword(keywordGoodsListForm.getKeywords());
        return mgjSdkComponent.convertSmProductEntityEx(userId,mgjSdkComponent.getProductByCriteria(productCriteriaBo));
    }

    @Override
    public PageBean<SmProductEntityEx> option(OptionGoodsListForm optionGoodsListForm) throws Exception {
        if(optionGoodsListForm.getOptionId() == null)
            throw new GlobleException(MsgEnum.PARAM_VALID_ERROR,"goodsId 不能为空");
        OptionForm optionForm = new OptionForm();
        BeanUtil.copyProperties(optionGoodsListForm,optionForm);
        optionForm.setTargetOptId(optionGoodsListForm.getOptionId());
        List<SmOptEntity> optEntities = optionService.allParentList(optionForm);
        if(optEntities == null)
            throw new GlobleException(MsgEnum.DATA_INVALID_ERROR,"无效optId:"+optionGoodsListForm.getOptionId());
        String keyWords = String.join(" ",optEntities.stream().map(SmOptEntity::getName).collect(Collectors.toList()));
        KeywordGoodsListForm keywordGoodsListForm = new KeywordGoodsListForm();
        BeanUtil.copyProperties(optionGoodsListForm,keywordGoodsListForm);
        keywordGoodsListForm.setKeywords(keyWords);
        return keyworkSearch(optionGoodsListForm.getUserId(),optionGoodsListForm.getPid(),keywordGoodsListForm);
    }

    @Override
    public PageBean<SmProductEntityEx> similar(SimilarGoodsListForm similarGoodsListForm) throws Exception {
        if(StrUtil.isBlank(similarGoodsListForm.getGoodsId()))
            throw new GlobleException(MsgEnum.PARAM_VALID_ERROR,"goodsId 不能为空");
        SmProductEntity smProductEntity = mgjSdkComponent.detail(similarGoodsListForm.getGoodsId(),similarGoodsListForm.getPid());
        KeywordGoodsListForm keywordGoodsListForm = new KeywordGoodsListForm();
        BeanUtil.copyProperties(similarGoodsListForm,keywordGoodsListForm);
        keywordGoodsListForm.setKeywords(smProductEntity.getName());
        keywordGoodsListForm.setSort(3);
        PageBean<SmProductEntityEx> productEntityExPageBean = keyworkSearch(similarGoodsListForm.getUserId(),similarGoodsListForm.getPid(),keywordGoodsListForm);
        productEntityExPageBean.setList(productEntityExPageBean.getList().stream().filter(o-> !smProductEntity.getGoodsId().equals(o.getGoodsId())).collect(Collectors.toList()));
        return productEntityExPageBean;
    }

    @Override
    public PageBean<SmProductEntityEx> mall(MallGoodsListForm mallGoodsListForm) throws Exception {
        return null;
    }
}
