package cn.xpride.framework.application.dto;

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
public class TokenDto extends BaseResponse {

    @ApiModelProperty(value = "SID")
    private Long sid;
    @ApiModelProperty(value = "token")
    private String token;
    @ApiModelProperty(value = "token失效时间戳")
    private Long expiresAtStamp;

}
