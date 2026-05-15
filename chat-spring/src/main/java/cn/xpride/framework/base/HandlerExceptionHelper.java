package cn.xpride.framework.base;



/**
 * 抛异常帮助类
 */
public class HandlerExceptionHelper {

    public static void throwException(String msg) throws HandlerException {
        throw new HandlerException(msg);
    }

    public static void throwException(boolean flag, String msg) throws HandlerException {
        if (flag) {
            throw new HandlerException(msg);
        }
    }

    public static void throwException(boolean flag, int code, String msg) throws HandlerException {
        if (flag) {
            throw new HandlerException(code, msg);
        }
    }

    public static void throwExceptionIfNull(Object obj, String msg) throws HandlerException {
        if (null == obj) {
            throw new HandlerException(msg);
        }
    }

    public static void throwTokenExpiredIfNull(Object obj) throws HandlerException {
        if (null == obj) {
            throw new HandlerException(666,  "已超时,请重新登录");
        }
    }

}
