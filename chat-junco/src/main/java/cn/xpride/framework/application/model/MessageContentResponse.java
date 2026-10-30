package cn.xpride.framework.application.model;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class MessageContentResponse extends ChannelMsgModel {

    /** 接收消息的用户ID列表 */
    private List<Long> receiveIdList;

}
