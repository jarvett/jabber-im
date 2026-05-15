package cn.xpride.framework.application.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.io.Serializable;

@Data
public class MessageContentModel implements Serializable {

    @ApiModelProperty(value = "消息体类型:text-纯文本 image-纯图片 event-事件(前端不允许发送)")
    @NotBlank(message = "消息体类型不能为空")
    private String type;

    @ApiModelProperty(value = "文本内容")
    private String text;

    @ApiModelProperty(value = "图片链接")
    private String imageUrl;

    @ApiModelProperty(value = "事件key",hidden = true)
    private String eventKey;

}
