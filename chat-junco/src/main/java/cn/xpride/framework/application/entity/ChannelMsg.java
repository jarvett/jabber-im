package cn.xpride.framework.application.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 通讯频道消息
 * </p>
 *
 * @author Anonymous
 * @since 2021-05-06
 */
@Data

public class ChannelMsg implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 日志ID */

    private String id;

    /** 发送消息的用户ID */
    private Long subscriberId;

    /** 频道ID */
    private Long channelId;

    /** 消息JSON内容 */
    private String content;

    /** 时间 */
    private Long time;


}
