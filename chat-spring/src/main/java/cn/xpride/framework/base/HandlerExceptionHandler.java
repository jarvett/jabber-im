package cn.xpride.framework.base;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * webapp应用层的全局异常处理
 */
@Slf4j
@RestControllerAdvice
@Controller
@ApiIgnore
public class HandlerExceptionHandler implements ErrorController {

    @ExceptionHandler(value = Exception.class)
    public ResponseEntity<BaseResponse> handleException(HttpServletRequest request, HttpServletResponse response, Exception e) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Type", MediaType.APPLICATION_JSON_VALUE);
        return new ResponseEntity<>(handleExceptionData(request, e), headers, HttpStatus.OK);
    }

    public BaseResponse handleExceptionData(HttpServletRequest request, Exception e) {
        if (e instanceof HandlerException) {
            return new BaseResponse(((HandlerException) e).getCode(), ((HandlerException) e).getMsg());
        } else if (e instanceof MissingServletRequestParameterException) {
            MissingServletRequestParameterException me = (MissingServletRequestParameterException) e;
            return new BaseResponse(400, "参数缺失:" + me.getParameterName() + " 类型:" + me.getParameterType());
        } else if (e instanceof MethodArgumentTypeMismatchException) {
            MethodArgumentTypeMismatchException me = (MethodArgumentTypeMismatchException) e;
            return new BaseResponse(400, "参数类型错误:" + me.getName() + " 需要类型:" + me.getRequiredType().getSimpleName());
        } else if (e instanceof NoHandlerFoundException) {//配合spring.mvc.throw-exception-if-no-handler-found=true和spring.resources.add-mappings=false才起作用
            return BaseResponse.notFound();//本项目由于不是纯接口项目,所以不能配置,这里不生效,需要配合
        } else if (e instanceof HttpRequestMethodNotSupportedException) {
            HttpRequestMethodNotSupportedException he = (HttpRequestMethodNotSupportedException) e;
            return new BaseResponse(405, "请求方式错误,只支持:" + he.getSupportedHttpMethods());
        } else if (e instanceof ServletRequestBindingException) {
            ServletRequestBindingException se = (ServletRequestBindingException) e;
            return new BaseResponse(400, "错误:" + se.getLocalizedMessage());
        } else if (e instanceof BindException) {
            BindException be = (BindException) e;
            return new BaseResponse(400, be.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        } else if (e instanceof MethodArgumentNotValidException) {
            MethodArgumentNotValidException be = (MethodArgumentNotValidException) e;
            return new BaseResponse(400, be.getBindingResult().getAllErrors().get(0).getDefaultMessage());
        } else {
            String uri = request.getRequestURI();
            log.error("请求:{} 发生异常", uri, e);
            return new BaseResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "系统异常,请稍后再试");
        }
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }

    @GetMapping(value = "/error")
    public ResponseEntity<?> handleNotFound(){
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }


}
