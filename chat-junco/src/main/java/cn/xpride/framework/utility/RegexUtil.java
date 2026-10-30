package cn.xpride.framework.utility;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class RegexUtil {

    /**
     * 是否包含空字符 如：空格，制表符，换行符等
     */
    public static final String HAS_BLANK = "\\s";
    /**
     * 是否包含汉字
     */
    public static final String HAS_HANZI = "[\\u4e00-\\u9fa5]";
    /**
     * 是否JSONARRAY形式的数组 如： [123,456]
     */
    public static final String IS_NUM_JSONARRAY = "^\\[((\\d,)*\\d)*\\]$";
    /**
     * 是否只包含字母汉字数字的字符串，数字不能开头
     */
    public static final String IS_RIGHT_USERNAME = "^[\\u4e00-\\u9fa5a-zA-Z][\\u4e00-\\u9fa5a-zA-Z\\d]+$";
    /**
     * 是否手机号码 如：13866088868
     */
    public static final String IS_MOBILE_PHONE = "^1[3|4|5|7|8][0-9]{9}$";
    /**
     * 是否固定电话号码  如：010-66886688
     */
    public static final String IS_TELE_PHONE = "^0\\d{2,3}-\\d{7,8}";
    /**
     * 是否电子邮箱  如：jarvetty@qq.com
     */
    public static final String IS_EMAIL = "^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+((\\.[a-zA-Z0-9_-]{2,4}){1,2})$";

    private RegexUtil() {
    }

    /**
     * 是否包含空白字符串,或者为null,空字符串
     *
     * @param str
     * @return
     */
    public static boolean hasBlank(String str) {
        if (null == str || "".equals(str)) {
            return true;
        }
        return hasRegex(str, HAS_BLANK);
    }

    /**
     * 是否包含汉字
     *
     * @param str
     * @return
     */
    public static boolean hasHanzi(String str) {
        return hasRegex(str, HAS_HANZI);
    }

    /**
     * 是否JsonArray形式的数字数组，如 [123,456]
     *
     * @param str
     * @return
     */
    public static boolean isNumJsonArray(String str) {
        return isRegex(str, IS_NUM_JSONARRAY);
    }

    /**
     * 是否只包含字母汉字数字的字符串，数字不能开头
     *
     * @param userName
     * @return
     */
    public static boolean isRightUserName(String userName) {
        return isRegex(userName, IS_RIGHT_USERNAME);
    }

    /**
     * 是否11位的手机号码
     *
     * @param mobile
     * @return
     */
    public static boolean isMobilePhone(String mobile) {
        return isRegex(mobile, IS_MOBILE_PHONE);
    }

    /**
     * 是否座机号码
     *
     * @param telephone
     * @return
     */
    public static boolean isTelePhone(String telephone) {
        return isRegex(telephone, IS_TELE_PHONE);
    }

    /**
     * 是否邮箱
     *
     * @param email
     * @return
     */
    public static boolean isEmail(String email) {
        return isRegex(email, IS_EMAIL);
    }


    private static boolean hasRegex(String str, String regex) {
        checkIn(str);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.find();
    }

    private static boolean isRegex(String str, String regex) {
        checkIn(str);
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }

    private static void checkIn(String s) {
        if (null == s) {
            throw new IllegalArgumentException("the input string can't be null");
        }
    }

}
