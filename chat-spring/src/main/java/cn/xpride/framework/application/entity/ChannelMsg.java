package cn.xpride.framework.application.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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
@Table(name = "im_channel_msg")
@Entity
public class ChannelMsg implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "日志ID")
    @Id
    private String id;

    @ApiModelProperty(value = "发送消息的用户ID")
    private Long subscriberId;

    @ApiModelProperty(value = "频道ID")
    private Long channelId;

    @ApiModelProperty(value = "消息JSON内容")
    private String content;

    @ApiModelProperty(value = "时间")
    private Long time;


}
