package cn.xpride.framework.application.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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
@Table(name = "im_subscriber")
@Entity
public class Subscriber implements Serializable {

    @Id
    @ApiModelProperty(value = "主键")
    private Long id;

    @ApiModelProperty(value = "0-普通用户 1-VIP(可以群聊) 2-系统客服")
    private Integer type;

    @ApiModelProperty(value = "应用ID,native为本系统")
    private String appId;

    @ApiModelProperty(value = "开放ID,或者本系统手机号")
    private String uniqueId;

    @ApiModelProperty(value = "昵称")
    private String nickname;

    @ApiModelProperty(value = "头像")
    private String avatar;

    @ApiModelProperty(value = "简介")
    private String intro;

    @ApiModelProperty(value = "注册时间")
    private Long createTime;

    @ApiModelProperty(value = "更新时间")
    private Long updateTime;

    @ApiModelProperty(value = "最近登录时间")
    private Long lastLoginTime;

}
