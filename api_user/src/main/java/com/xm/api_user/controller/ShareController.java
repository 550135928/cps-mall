package com.xm.api_user.controller;

import com.xm.api_user.service.ShareService;
import com.xm.comment.annotation.LoginUser;
import com.xm.comment_utils.exception.GlobleException;
import com.xm.comment_feign.module.mall.feign.MallFeignClient;
import com.xm.comment_utils.response.MsgEnum;
import com.xm.comment_serialize.module.user.form.GetUserShareForm;
import com.xm.comment_serialize.module.user.vo.ShareVo;
import com.xm.comment_utils.mybatis.PageBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/share")
public class ShareController {

    @Autowired
    private ShareService shareService;
    @Autowired
    private MallFeignClient mallFeignClient;


    @GetMapping
    public PageBean<ShareVo> get(@LoginUser Integer userId, GetUserShareForm getUserShareForm){
        return shareService.getList(
                userId,
                getUserShareForm.getOrderType(),
                getUserShareForm.getOrder(),
                getUserShareForm.getPageNum(),
                getUserShareForm.getPageSize());

    }

    @DeleteMapping("/{id}")
    public void deleteAll(@LoginUser Integer userId,@PathVariable("id")Integer id){
        if(id == null || id == 0)
            throw new GlobleException(MsgEnum.PARAM_VALID_ERROR);
        shareService.del(userId,id);
    }
}
