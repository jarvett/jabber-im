package cn.xpride.framework.application.model;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class ChannelConfigModel implements Serializable {

    /** 类型:0-私聊 1-群聊  2-客服通道 3-服务消息通道 */
    private Integer type;

    /** 加入聊天的用户SID列表 */
    private List<Long> sidList;

    //下面是群聊用的字段

    /** 聊名称 */
    private String groupName;

    /** 聊头像 */
    private String groupAvatar;


}
