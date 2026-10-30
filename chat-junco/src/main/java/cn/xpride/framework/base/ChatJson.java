package cn.xpride.framework.base;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.serializer.ToStringSerializer;

/** HTTP 沿用旧序列化规则；WebSocket 继续使用 FastJsonUtil。 */
public final class ChatJson {
    private static final SerializeConfig CONFIG = new SerializeConfig();
    static {
        CONFIG.put(Long.class, ToStringSerializer.instance);
        CONFIG.put(Long.TYPE, ToStringSerializer.instance);
    }
    public static String write(Object value) {
        return JSON.toJSONString(value, CONFIG, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteNullListAsEmpty);
    }
}
