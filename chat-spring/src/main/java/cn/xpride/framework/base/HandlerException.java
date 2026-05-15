package cn.xpride.framework.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class HandlerException extends Exception {

    private int code;
    private String msg;

    public HandlerException(String msg){
        super(msg);
        this.code = 10;
        this.msg = msg;
    }

}
