package cn.xpride.framework.application.model;

import lombok.Data;

import java.io.Serializable;

@Data
public class MessageContentModel implements Serializable {

    /** 消息体类型:text-纯文本 image-纯图片 event-事件(前端不允许发送) */

    private String type;

    /** 文本内容 */
    private String text;

    /** 图片链接 */
    private String imageUrl;

    /** 事件key */
    private String eventKey;

}
