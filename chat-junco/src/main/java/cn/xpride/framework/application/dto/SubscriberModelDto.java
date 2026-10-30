package cn.xpride.framework.application.dto;

import cn.xpride.framework.application.model.SubscriberModel;
import cn.xpride.framework.base.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SubscriberModelDto extends BaseResponse {

    /** 用户详情 */
    private SubscriberModel profile;

}
