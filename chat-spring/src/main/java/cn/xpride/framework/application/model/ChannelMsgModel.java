package cn.xpride.framework.application.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

@Data
public class ChannelMsgModel implements Serializable {

    @ApiModelProperty(value = "消息ID")
    private String id;

    @ApiModelProperty(value = "通讯频道ID")
    private Long channelId;

    @ApiModelProperty(value = "发送消息的用户ID")
    private Long senderId;

    @ApiModelProperty(value = "存储的消息格式对象模型")
    private MessageContentModel content;

    @ApiModelProperty(value = "时间")
    private Long time;

}
