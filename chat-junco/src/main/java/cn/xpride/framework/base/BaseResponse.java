package cn.xpride.framework.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseResponse implements Serializable {

    public static final BaseResponse OK = new BaseResponse(0,"success");

    private int code;
    private String msg;

    static BaseResponse notFound() {
        return new BaseResponse(404,"请求URL不存在");
    }

}
