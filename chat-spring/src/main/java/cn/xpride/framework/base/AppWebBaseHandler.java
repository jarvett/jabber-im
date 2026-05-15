package cn.xpride.framework.base;

import cn.xpride.framework.utility.MyStringUtil;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

public abstract class AppWebBaseHandler {

    public static final String TOKEN = "token";

    private static String getToken(HttpServletRequest request) {
        String token = request.getHeader(TOKEN);
        if (MyStringUtil.isEmpty(token)) {
            token = request.getParameter(TOKEN);
        }
        if (MyStringUtil.isEmpty(token)) {
            Cookie[] cookies = request.getCookies();
            if (null != cookies) {
                for (Cookie cookie : cookies) {
                    switch (cookie.getName()) {
                        case TOKEN:
                            token = cookie.getValue();
                        default:
                            break;
                    }
                }
            }
        }
        return token;
    }

    protected MyToken getMyToken() throws HandlerException {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String token = getToken(request);
        MyToken myToken = MyTokenUtil.getToken(token);
        HandlerExceptionHelper.throwTokenExpiredIfNull(myToken);
        return myToken;
    }

}
