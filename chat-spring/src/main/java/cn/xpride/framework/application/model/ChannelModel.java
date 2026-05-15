package cn.xpride.framework.application.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChannelModel {

    @ApiModelProperty(value = "频道主键")
    private Long id;

    @ApiModelProperty(value = "类型:0-私聊 1-群聊 2-客服通道 3-服务消息通道")
    private Integer type;

}
