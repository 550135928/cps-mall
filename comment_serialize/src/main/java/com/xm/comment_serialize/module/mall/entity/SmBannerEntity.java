package com.xm.comment_serialize.module.mall.entity;

import lombok.Data;
import javax.persistence.*;
import java.io.Serializable;

@Data
@Table(name = "sm_banner")
public class SmBannerEntity implements Serializable{
	@Id
	@Column(name = "Id")
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	/**
	 * 图片路径
	 */
	private String img;

	/**
	 * 标题
	 */
	private String name;

	/**
	 * 排序(从大到小)
	 */
	private Integer sort;

	/**
	 * 目标(1:普通url跳转,2:小程序跳转,3:唤醒其他小程序,4:唤醒app)
	 */
	private Integer targrt;

	/**
	 * 包含的信息
	 */
	private String url;

	/**
	 * 类型(1:首页轮播图,2:首页滑动列表,3:tabbar底部导航)
	 */
	private Integer type;

	private java.util.Date createTime;
}
