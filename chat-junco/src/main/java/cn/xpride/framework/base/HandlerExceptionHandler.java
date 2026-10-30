package cn.xpride.framework.base;

import io.github.jarvett.core.ioc.annotation.JuncoManaged;
import io.github.jarvett.web.JuncoHttpRequest;
import io.github.jarvett.web.annotation.ExceptionHandler;
import io.github.jarvett.web.annotation.ResponseStatus;
import lombok.extern.slf4j.Slf4j;

@JuncoManaged
@Slf4j
public class HandlerExceptionHandler {
    /** 旧客户端依靠响应 code 判断错误，业务异常仍返回 HTTP 200。 */
    @ExceptionHandler(HandlerException.class)
    @ResponseStatus(200)
    public BaseResponse business(HandlerException error) {
        log.warn("聊天请求被拒绝: code={}, msg={}", error.getCode(), error.getMsg());
        return new BaseResponse(error.getCode(), error.getMsg());
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(200)
    public BaseResponse unexpected(Exception error, JuncoHttpRequest request) {
        log.error("请求失败: path={}", request.getPath(), error);
        return new BaseResponse(500, "系统异常,请稍后再试");
    }
}
