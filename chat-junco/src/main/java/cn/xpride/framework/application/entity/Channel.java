package cn.xpride.framework.application.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 通讯频道
 * </p>
 *
 * @author Anonymous
 * @since 2021-05-06
 */
@Data

public class Channel implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 频道主键 */

    private Long id;

    /** 类型:0-私聊 1-群聊  2-客服通道 3-服务消息通道 */
    private Integer type;

    /** 通讯频道设置 */
    private String channelConfig;

    /** 是否需要客服接入:1-是 0-否 */
    private Integer isNeedAccess;

    /** 加入的用户ID */
    private String joinSubscriberIds;

    /** 消息数 */
    private Integer msgCount;

    /** 创建时间 */
    private Long createTime;

    /** 更新时间 */
    private Long updateTime;

}
