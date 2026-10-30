package cn.xpride.framework.application.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DialogueModel {

    /** 对话ID */
    private Long id;

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

    /** 未读消息数 */
    private Integer unreadCount;

}
