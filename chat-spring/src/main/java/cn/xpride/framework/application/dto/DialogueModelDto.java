package cn.xpride.framework.application.dto;

import cn.xpride.framework.application.model.DialogueModel;
import cn.xpride.framework.base.BaseResponse;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class DialogueModelDto extends BaseResponse {

    @ApiModelProperty(value = "对话详情")
    private DialogueModel model;

}
