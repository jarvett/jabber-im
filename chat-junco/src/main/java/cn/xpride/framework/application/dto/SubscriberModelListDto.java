package cn.xpride.framework.application.dto;

import cn.xpride.framework.application.model.SubscriberModel;
import cn.xpride.framework.base.BaseResponse;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class SubscriberModelListDto extends BaseResponse {

    /** 用户列表 */
    private List<SubscriberModel> subscriberList;

}
