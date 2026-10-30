package cn.xpride.framework.base;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MyToken implements Serializable {

    //用户ID
    private Long userId;
    //用户类型
    private Integer userType;
    //token
    private String token;
    //token失效时间戳
    private Long expiresAtStamp;

}
