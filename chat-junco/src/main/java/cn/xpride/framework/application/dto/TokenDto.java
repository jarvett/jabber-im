package cn.xpride.framework.application.dto;

import cn.xpride.framework.base.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class TokenDto extends BaseResponse {

    /** SID */
    private Long sid;

    /** token */
    private String token;

    /** token失效时间戳 */
    private Long expiresAtStamp;

}
