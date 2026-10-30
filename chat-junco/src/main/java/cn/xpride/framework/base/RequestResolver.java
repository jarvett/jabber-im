package cn.xpride.framework.base;

import io.github.jarvett.core.ioc.annotation.JuncoManaged;
import io.github.jarvett.web.*;
import java.lang.reflect.Parameter;

/** 将真实请求注入兼容层，避免被 Junco 按请求体 DTO 解析。 */
@JuncoManaged
public class RequestResolver implements ParamResolver {
    public boolean supports(Parameter parameter) {
        return parameter.getType() == JuncoHttpRequest.class;
    }
    public Object resolve(Parameter parameter, JuncoHttpRequest request, JuncoHttpResponse response) {
        return request;
    }
}
