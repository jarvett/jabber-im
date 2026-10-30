package cn.xpride.framework.application.dto;

import cn.xpride.framework.application.model.ChannelMsgModel;
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
public class ChannelMsgModelDto extends BaseResponse {

    /** 通讯记录列表 */
    private List<ChannelMsgModel> channelMsgList;

}
