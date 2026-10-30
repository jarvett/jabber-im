package cn.xpride.framework.application.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriberModel implements Serializable {

    /** SID */
    private Long sid;

    /** 头像 */
    private String nickName;

    /** 昵称 */
    private String avatarUrl;

    /** 注册时间 */
    private Long createTime;

    /** 开放ID,或者本系统手机号 */
    private String uniqueId;

    /** 简介 */
    private String intro;

}
