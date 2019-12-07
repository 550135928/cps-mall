package com.xm.api_user.service.impl;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import com.xm.api_user.mapper.*;
import com.xm.api_user.mapper.custom.SuRoleMapperEx;
import com.xm.api_user.service.UserService;
import com.xm.comment.exception.GlobleException;
import com.xm.comment.module.mall.feign.MallFeignClient;
import com.xm.comment.response.MsgEnum;
import com.xm.comment_serialize.module.mall.constant.ConfigEnmu;
import com.xm.comment_serialize.module.mall.constant.ConfigTypeConstant;
import com.xm.comment_serialize.module.user.constant.UserTypeConstant;
import com.xm.comment_serialize.module.user.entity.*;
import com.xm.comment_serialize.module.user.ex.RolePermissionEx;
import com.xm.comment_serialize.module.user.ex.UserRoleEx;
import com.xm.comment_serialize.module.user.form.GetUserInfoForm;
import com.xm.comment_serialize.module.user.form.UpdateUserInfoForm;
import com.xm.comment_utils.encry.MD5;
import me.chanjar.weixin.common.error.WxErrorException;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service("userService")
public class UserServiceImpl implements UserService {

    @Autowired
    private SuUserMapper suUserMapper;
    @Autowired
    private SuRoleMapperEx suRoleMapperEx;
    @Autowired
    private SuPermissionMapper suPermissionMapper;
    @Autowired
    private SuUserRoleMapMapper suUserRoleMapMapper;
    @Autowired
    private SuRolePermissionMapMapper suRolePermissionMapMapper;
    @Autowired
    private WxMaService wxMaService;
    @Autowired
    private MallFeignClient mallFeignClient;

    @Override
    public SuUserEntity getUserInfo(GetUserInfoForm getUserInfoForm) throws WxErrorException {
        if(StringUtils.isNotBlank(getUserInfoForm.getCode())){
            WxMaJscode2SessionResult wxMaJscode2SessionResult = wxMaService.getUserService().getSessionInfo(getUserInfoForm.getCode());
            SuUserEntity record = new SuUserEntity();
            record.setOpenId(wxMaJscode2SessionResult.getOpenid());
            record = suUserMapper.selectOne(record);
            if(record != null)
                return record;
            return saveUser(wxMaJscode2SessionResult.getOpenid());
        }else if(StringUtils.isNotBlank(getUserInfoForm.getOpenId())){
            SuUserEntity record = new SuUserEntity();
            record.setOpenId(getUserInfoForm.getOpenId());
            return suUserMapper.selectOne(record);
        }else if(getUserInfoForm.getUserId() != null){
            return suUserMapper.selectByPrimaryKey(getUserInfoForm.getUserId());
        }else {
            throw new GlobleException(MsgEnum.PARAM_VALID_ERROR);
        }
    }

    /**
     * 添加一个新用户
     * @param openId
     * @return
     */
    private SuUserEntity saveUser(String openId){
        SuUserEntity user = new SuUserEntity();
        user.setOpenId(openId);
        user.setNickname("Su_"+ MD5.md5(openId,"").substring(0,5));
        user.setSex(0);
        user.setHeadImg("https://mall-share.oss-cn-shanghai.aliyuncs.com/comment/img/headImg.png?x-oss-process=image/resize,m_fixed,h_132,w_132");
        user.setCreateTime(new Date());
        user.setLastLogin(new Date());
        suUserMapper.insertUseGeneratedKeys(user);
        return suUserMapper.selectByPrimaryKey(user.getId());
    }


    @Override
    public void updateUserInfo(Integer userId, UpdateUserInfoForm updateUserInfoForm){
        SuUserEntity suUserEntity = new SuUserEntity();
        suUserEntity.setId(userId);
        suUserEntity.setNickname(updateUserInfoForm.getNickName());
        suUserEntity.setHeadImg(updateUserInfoForm.getAvatarUrl());
        suUserEntity.setSex(updateUserInfoForm.getGender());
        suUserEntity.setLanguage(updateUserInfoForm.getLanguage());
        suUserEntity.setCity(updateUserInfoForm.getCity());
        suUserEntity.setProvince(updateUserInfoForm.getProvince());
        suUserEntity.setCountry(updateUserInfoForm.getCountry());
        suUserMapper.updateByPrimaryKeySelective(suUserEntity);
    }

    @Override
    public List<RolePermissionEx> getUserRole(Integer userId) {
//        SuUserRoleMapEntity userRoleMapEntity = new SuUserRoleMapEntity();
//        userRoleMapEntity.setUserId(userId);
//        List<Integer> roleIds = suUserRoleMapMapper.select(userRoleMapEntity).stream().map(SuUserRoleMapEntity::getRoleId).collect(Collectors.toList());
//        Example roleExample = new Example(SuRoleEntity.class);
//        roleExample.createCriteria().andIn("id",roleIds);
//        List<SuRoleEntity> roles = suRoleMapper.selectByExample(roleExample);
//        Example rolePermMapExample = new Example(SuRolePermissionMapEntity.class);
//        rolePermMapExample.createCriteria().andIn("roleId",roles.stream().map(SuRoleEntity::getId).collect(Collectors.toList()));
//        List<Integer> permissionIds = suRolePermissionMapMapper.selectByExample(rolePermMapExample).stream().map(SuRolePermissionMapEntity::getPermissionId).collect(Collectors.toList());
//        Example permissionExample = new Example(SuPermissionEntity.class);
//        permissionExample.createCriteria().andIn("id",permissionIds);
//        suRolePermissionMapMapper.selectByExample(permissionExample);
        return suRoleMapperEx.getUserRoleEx(userId);
    }

    @Override
    public SuUserEntity getSuperUser(Integer userId, int userType) {
        SuUserEntity self = suUserMapper.selectByPrimaryKey(userId);
        if(self == null){
            throw new GlobleException(MsgEnum.USER_NOFOUND_ERROR);
        }
        switch (userType){
            case UserTypeConstant.SELF:{
                return self;
            }
            case UserTypeConstant.PARENT:{
                if(self.getParentId() == null){
                    throw new NullPointerException(String.format("用户：%s 不存在父用户",userId));
                }
                SuUserEntity parent = suUserMapper.selectByPrimaryKey(self.getParentId());
                if(parent == null){
                    throw new GlobleException(MsgEnum.USER_ID_ERROR);
                }
                return parent;
            }
            case UserTypeConstant.PROXY:{
                if(self.getParentId() == null || self.getParentId() == 0){
                    return null;
                }
                Integer level = Integer.valueOf(mallFeignClient.getOneConfig(userId, ConfigEnmu.PROXY_LEVEL.getName(), ConfigTypeConstant.SYS_CONFIG).getData().getVal());
                SuUserEntity target = null;
                for (int i = 0; i < level; i++) {
                    target = suUserMapper.selectByPrimaryKey(self.getParentId());
                    if(target.getParentId() == null || target.getParentId() == 0){
                        break;
                    }
                }
                return target;
            }
        }
        throw new GlobleException(MsgEnum.TYPE_NOTFOUND_ERROR);
    }
}