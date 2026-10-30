package cn.xpride.framework.application.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 订阅者
 * </p>
 *
 * @author Anonymous
 * @since 2021-05-06
 */
@Data

public class Subscriber implements Serializable {


    /** 主键 */
    private Long id;

    /** 0-普通用户 1-VIP(可以群聊) 2-系统客服 */
    private Integer type;

    /** 应用ID,native为本系统 */
    private String appId;

    /** 开放ID,或者本系统手机号 */
    private String uniqueId;

    /** 昵称 */
    private String nickname;

    /** 头像 */
    private String avatar;

    /** 简介 */
    private String intro;

    /** 注册时间 */
    private Long createTime;

    /** 更新时间 */
    private Long updateTime;

    /** 最近登录时间 */
    private Long lastLoginTime;

}
