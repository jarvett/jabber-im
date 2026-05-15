package cn.xpride.framework.application.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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
@Table(name = "im_dialogue")
@Entity
public class Dialogue implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @ApiModelProperty(value = "对话ID")
    private Long id;

    @ApiModelProperty(value = "订阅者ID")
    private Long subscriberId;

    @ApiModelProperty(value = "通讯频道ID")
    private Long channelId;

    @ApiModelProperty(value = "对话类型: 0-私聊 1-群聊 2-系统助手 3-服务号 4-折叠组")
    private Integer type;

    @ApiModelProperty(value = "对话名称")
    private String name;

    @ApiModelProperty(value = "对话头像")
    private String avatar;

    @ApiModelProperty(value = "对话简要")
    private String brief;

    @ApiModelProperty(value = "最新消息时间")
    private Long lastTime;

    @ApiModelProperty(value = "最后读取消息时间")
    private Long lastReadTime;

    @ApiModelProperty(value = "未读消息数")
    private Integer unreadCount;

    @ApiModelProperty(value = "用户是否可见:1-是 0-否")
    private Integer isVisible;

    @ApiModelProperty(value = "是否在外层显示:1-是 2-否")
    private Integer showOuter;


}
