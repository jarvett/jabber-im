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
public class PhoneCodeDto extends BaseResponse {

    /** 打码的手机号 */
    private String phone;

}
