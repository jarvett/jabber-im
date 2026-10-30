package cn.xpride.framework.application.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class ChannelMsgModel implements Serializable {

    /** 消息ID */
    private String id;

    /** 通讯频道ID */
    private Long channelId;

    /** 发送消息的用户ID */
    private Long senderId;

    /** 存储的消息格式对象模型 */
    private MessageContentModel content;

    /** 时间 */
    private Long time;

}
