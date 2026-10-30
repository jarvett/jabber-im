package cn.xpride.framework.base;

public enum UserType {

    APP_USER(1, 7 * 24 * 60),// C端用户,7天

    ;

    //用户类型:看上面注释
    private Integer userType;
    //超时时间 分钟
    private Integer timeout;

    UserType(Integer userType, Integer timeout) {
        this.userType = userType;
        this.timeout = timeout;
    }

    public Integer getUserType() {
        return userType;
    }

    public static UserType getByUserType(Integer userType) {
        for (UserType u : UserType.values()) {
            if (u.getUserType().equals(userType)) {
                return u;
            }
        }
        return null;
    }

    public Integer getTimeout() {
        return timeout;
    }


    private static final String PREFIX = "_s";
    private static final String SUFFIX = "_";

    //redis存前缀
    public String getPrefix() {
        return PREFIX + this.userType + SUFFIX;
    }

    //根据token前缀判断userType
    public static UserType getUserTypeByPrefix(String token) {
        for (UserType e : UserType.values()) {
            if (token.startsWith(e.getPrefix())) {
                return e;
            }
        }
        return null;
    }

}
