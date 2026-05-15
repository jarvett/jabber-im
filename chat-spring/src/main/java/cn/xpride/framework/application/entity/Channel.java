package cn.xpride.framework.application.entity;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
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
@Table(name = "im_channel")
@Entity
public class Channel implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "频道主键")
    @Id
    private Long id;

    @ApiModelProperty(value = "类型:0-私聊 1-群聊  2-客服通道 3-服务消息通道")
    private Integer type;

    @ApiModelProperty(value = "通讯频道设置")
    private String channelConfig;

    @ApiModelProperty(value = "是否需要客服接入:1-是 0-否")
    private Integer isNeedAccess;

    @ApiModelProperty(value = "加入的用户ID")
    private String joinSubscriberIds;

    @ApiModelProperty(value = "消息数")
    private Integer msgCount;

    @ApiModelProperty(value = "创建时间")
    private Long createTime;

    @ApiModelProperty(value = "更新时间")
    private Long updateTime;

}
