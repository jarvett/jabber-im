package cn.xpride.framework.utility;

public final class RadixUtil {

    private static final char[] NUMS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ_-*!#$%&'()+,./:;<=>?@[]^`{|}~\\\"".toCharArray();

    private static String toString(long i, int radix) {
        if (radix < Character.MIN_RADIX || radix > NUMS.length)
            radix = 10;
        if (radix == 10)
            return Long.valueOf(i).toString();
        char[] buf = new char[65];
        int charPos = 64;
        boolean negative = (i < 0);

        if (!negative) {
            i = -i;
        }
        while (i <= -radix) {
            buf[charPos--] = NUMS[(int) (-(i % radix))];
            i = i / radix;
        }
        buf[charPos] = NUMS[(int) (-i)];

        if (negative) {
            buf[--charPos] = '-';
        }
        return new String(buf, charPos, (65 - charPos));
    }

    private static int charAtNum(char c) {
        for (int i = 0, len = NUMS.length; i < len; i++) {
            if (NUMS[i] == c) {
                return i;
            }
        }
        return 0;
    }

    private static long parseLong(String s, int radix) {
        if (s == null || radix < Character.MIN_RADIX || radix > NUMS.length) {
            throw new NumberFormatException("null");
        }
        long result = 0;
        boolean negative = false;
        int i = 0, len = s.length();
        long limit = -Long.MAX_VALUE;
        long multmin;
        int digit;
        if (len > 0) {
            char firstChar = s.charAt(0);
            if (firstChar < '0') { // Possible leading "+" or "-"
                if (firstChar == '-') {
                    negative = true;
                    limit = Long.MIN_VALUE;
                } else if (firstChar != '+')
                    throw new NumberFormatException("null");

                if (len == 1) // Cannot have lone "+" or "-"
                    throw new NumberFormatException("null");
                i++;
            }
            multmin = limit / radix;
            while (i < len) {
                digit = charAtNum(s.charAt(i++));
                if (digit < 0) {
                    throw new NumberFormatException("null");
                }
                if (result < multmin) {
                    throw new NumberFormatException("null");
                }
                result *= radix;
                if (result < limit + digit) {
                    throw new NumberFormatException("null");
                }
                result -= digit;
            }
        } else {
            throw new NumberFormatException("null");
        }
        return negative ? result : -result;
    }

    public static String toString(long i){
        return toString(i,62);
    }

    public static long parseLong(String s){
        return parseLong(s,62);
    }

}
