package cn.xpride.framework.application.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DialogueModel {

    @ApiModelProperty(value = "对话ID")
    private Long id;

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

    @ApiModelProperty(value = "未读消息数")
    private Integer unreadCount;

}
