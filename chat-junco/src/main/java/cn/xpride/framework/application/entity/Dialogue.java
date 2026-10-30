package cn.xpride.framework.application.entity;

import lombok.Data;

import java.io.Serializable;

/**
 * <p>
 * 对话
 * </p>
 *
 * @author Anonymous
 * @since 2021-05-06
 */
@Data

public class Dialogue implements Serializable {

    private static final long serialVersionUID = 1L;


    /** 对话ID */
    private Long id;

    /** 订阅者ID */
    private Long subscriberId;

    /** 通讯频道ID */
    private Long channelId;

    /** 对话类型: 0-私聊 1-群聊 2-系统助手 3-服务号 4-折叠组 */
    private Integer type;

    /** 对话名称 */
    private String name;

    /** 对话头像 */
    private String avatar;

    /** 对话简要 */
    private String brief;

    /** 最新消息时间 */
    private Long lastTime;

    /** 最后读取消息时间 */
    private Long lastReadTime;

    /** 未读消息数 */
    private Integer unreadCount;

    /** 用户是否可见:1-是 0-否 */
    private Integer isVisible;

    /** 是否在外层显示:1-是 2-否 */
    private Integer showOuter;


}
