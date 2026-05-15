package cn.xpride.framework.utility;

public final class MyStringUtil {

    private static final char MASK = '*';

    private MyStringUtil() {
    }

    private static boolean isNullOrEmpty(String str) {
        return (null == str) || str.trim().isEmpty();
    }

    public static boolean isEmpty(String str) {
        return isNullOrEmpty(str);
    }

    public static boolean isNotEmpty(String str) {
        return !isNullOrEmpty(str);
    }

    /**
     * 隐藏名称，如 张三 -> 张** , Zhangsan -> Z***n
     *
     * @param name
     * @return
     */
    public static String hideName(String name) {
        if (isNullOrEmpty(name)) {
            return name;
        } else {
            StringBuilder sb = new StringBuilder();
            int len = name.length();
            sb.append(name.charAt(0));
            sb.append(MASK);
            sb.append(MASK);
            if (len >= 4) {
                sb.append(MASK);
                sb.append(name.charAt(len - 1));
            }
            return sb.toString();
        }
    }

    /**
     * 隐藏电话号码，如 0775-4675882 -> 077****5882 , 13028841632 -> 130****1632
     *
     * @param phone
     * @return
     */
    public static String hidePhone(String phone) {
        if (isNullOrEmpty(phone)) {
            return phone;
        } else {
            StringBuilder sb = new StringBuilder();
            int len = phone.length();
            sb.append(phone.charAt(0));
            if (len >= 7) {
                sb.append(phone.charAt(1));
            }
            if (len >= 11) {
                sb.append(phone.charAt(2));
            }
            sb.append(MASK);
            sb.append(MASK);
            sb.append(MASK);
            sb.append(MASK);
            if (len >= 11) {
                sb.append(phone.charAt(len - 4));
                sb.append(phone.charAt(len - 3));
            }
            if (len >= 8) {
                sb.append(phone.charAt(len - 2));
                sb.append(phone.charAt(len - 1));
            }
            return sb.toString();
        }
    }

    /**
     * 隐藏电子邮箱账号，如 jarvetty@qq.com-> ja***@qq.com
     *
     * @param email
     * @return
     */
    public static String hideEmail(String email) {
        if (isNullOrEmpty(email)) {
            return email;
        } else {
            int len = email.indexOf('@');
            StringBuilder sb = new StringBuilder();
            sb.append(email.charAt(0));
            if (len >= 4) {
                sb.append(email.charAt(1));
                sb.append(MASK);
            }
            sb.append(MASK);
            sb.append(MASK);
            sb.append(email.substring(len));
            return sb.toString();
        }
    }

    public static String capitalize(String str) {
        if (isEmpty(str)) return null;
        StringBuilder sb = new StringBuilder();
        sb.append(str.trim());
        if (!Character.isLowerCase(sb.charAt(0)) || !Character.isUpperCase(sb.charAt(1))) {
            sb.setCharAt(0, Character.toUpperCase(sb.charAt(0)));
        }
        return sb.toString();
    }

    public static String deCapitalize(String str) {
        StringBuilder sb = new StringBuilder();
        sb.append(str.trim());
        if (!Character.isUpperCase(sb.charAt(0)) || !Character.isUpperCase(sb.charAt(1))) {
            sb.setCharAt(0, Character.toLowerCase(sb.charAt(0)));
        }
        return sb.toString();
    }

    public static boolean isLongGtZero(Long l) {
        return l != null && l > 0;
    }

    public static String ellipsis(String str) {
        if (str.length() < 15) {
            return str;
        } else {
            return str.substring(0, 12) + "...";
        }
    }

}
