package com.xm.comment.module.user.feign;

import com.xm.comment.annotation.LoginUser;
import com.xm.comment.config.FeignConfiguration;
import com.xm.comment.module.user.feign.fallback.UserFeignClientFallBack;
import com.xm.comment.response.Msg;
import com.xm.comment_serialize.module.mall.constant.ConfigEnmu;
import com.xm.comment_serialize.module.user.entity.SuConfigEntity;
import com.xm.comment_serialize.module.user.entity.SuPidEntity;
import com.xm.comment_serialize.module.user.entity.SuUserEntity;
import com.xm.comment_serialize.module.user.ex.RolePermissionEx;
import com.xm.comment_serialize.module.user.form.GetUserInfoForm;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(value = "api-user",fallback = UserFeignClientFallBack.class,configuration = FeignConfiguration.class)
public interface UserFeignClient {

    @PostMapping(value = "/user/info",consumes = "application/json")
    public Msg<SuUserEntity> getUserInfo(@RequestBody GetUserInfoForm getUserInfoForm);

    @GetMapping("/role")
    public Msg<List<RolePermissionEx>> role(Integer userId);

    @GetMapping("/config/all")
    public Msg<List<SuConfigEntity>> getAllConfig(Integer userId);

    @GetMapping("/config/one")
    public Msg<SuConfigEntity> getOneConfig(@RequestParam Integer userId, @RequestParam String key);

    @GetMapping("/user/superUser")
    public Msg<SuUserEntity> superUser(@RequestParam Integer userId,@RequestParam int userType);

    @PostMapping("/product/history")
    public Msg addProductHistory(@RequestParam Integer userId,@RequestParam Integer platformType,@RequestParam String goodsId);

    @GetMapping("/pid")
    public Msg<SuPidEntity> getPid(@RequestParam Integer userId,@RequestParam Integer platformType);
}
