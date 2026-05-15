package cn.xpride.framework.utility;

import com.alibaba.fastjson.serializer.*;

import java.util.Date;


public class FastJsonUtil {

    /**
     * 对序列化的Long类型进行特殊处理,避免位数过大导致和js精度的丢失,只用于向页面发送json数据时使用
     */
    static ObjectSerializer longSerializer = (serializer, object, fieldName, fieldType, features) -> {
        SerializeWriter out = serializer.getWriter();
        if (object == null) {
            if (out.isEnabled(SerializerFeature.WriteNullNumberAsZero)) {
                out.write('0');
            } else {
                out.writeNull();
            }
            return;
        }
        out.writeString(object.toString());
    };

    /**
     * 【对Long型兼容js的json串】
     *
     * @param object 对象
     * @return
     */
    public static final String toCompatibleJSON(Object object) {
        if (null == object) return "";
        return toCompatibleJSON(object, null);
    }

    /**
     * 【对Long型兼容js的json串】
     *
     * @param object 对象
     * @param format 指定日期类型格式
     * @return
     */
    public static final String toCompatibleJSON(Object object, String format) {
        SerializeWriter out = new SerializeWriter();
        try {
            //此处必须new一个SerializeConfig,防止修改默认的配置
            JSONSerializer serializer = new JSONSerializer(out, new SerializeConfig());
            serializer.getMapping().put(Long.class, longSerializer);
            if (format != null) {
                serializer.getMapping().put(Date.class, new SimpleDateFormatSerializer(format));
            }
            serializer.write(object);
            return out.toString();
        } finally {
            out.close();
        }
    }

}
