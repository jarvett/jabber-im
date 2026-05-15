package cn.xpride.framework.application.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriberModel implements Serializable {

    @ApiModelProperty(value = "SID")
    private Long sid;

    @ApiModelProperty(value = "头像")
    private String nickName;

    @ApiModelProperty(value = "昵称")
    private String avatarUrl;

    @ApiModelProperty(value = "注册时间")
    private Long createTime;

    @ApiModelProperty(value = "开放ID,或者本系统手机号")
    private String uniqueId;

    @ApiModelProperty(value = "简介")
    private String intro;

}
