package cn.xpride.framework.utility;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;

public class CustomBase62 {

    public static String ALPHABET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static char[] encodes = "ivRoZ6TFu9JA4aNkxygt8LcCXpO7E2GefKnjs3dz5QHIVq1B0lDMPSWrUmbhYw+/".toCharArray();

    private static byte[] decodes = new byte[256];

    static {
        for (int i = 0; i < encodes.length; i++) {
            decodes[encodes[i]] = (byte) i;
        }
    }

    public static String encodeStr(String data) {
        return encode(data.getBytes());
    }

    /**
     * 将data编码成Base62的字符串
     *
     * @param data
     * @return
     */
    public static String encode(byte[] data) {
        StringBuffer sb = new StringBuffer(data.length * 2);
        int pos = 0, val = 0;
        for (int i = 0; i < data.length; i++) {
            val = (val << 8) | (data[i] & 0xFF);
            pos += 8;
            while (pos > 5) {
                char c = encodes[val >> (pos -= 6)];
                sb.append(
                    c == 'i' ? "ia" :
                        c == '+' ? "ib" :
                            c == '/' ? "ic" : c + ""
                );
                val &= ((1 << pos) - 1);
            }
        }
        if (pos > 0) {
            char c = encodes[val << (6 - pos)];
            sb.append(
                c == 'i' ? "ia" :
                    c == '+' ? "ib" :
                        c == '/' ? "ic" : c + ""
            );
        }
        return sb.toString();
    }

    public static String decodeToStr(String val) {
        return new String(decode(val));
    }

    /**
     * 将字符串解码成byte数组
     *
     * @param string
     * @return
     */
    public static byte[] decode(String string) {
        if (string == null) {
            return null;
        }
        char[] data = string.toCharArray();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(string.toCharArray().length);
        int pos = 0, val = 0;
        for (int i = 0; i < data.length; i++) {
            char c = data[i];
            if (c == 'i') {
                c = data[++i];
                c =
                    c == 'a' ? 'i' :
                        c == 'b' ? '+' :
                            c == 'c' ? '/' : data[--i];
            }
            val = (val << 6) | decodes[c];
            pos += 6;
            while (pos > 7) {
                baos.write(val >> (pos -= 8));
                val &= ((1 << pos) - 1);
            }
        }
        return baos.toByteArray();
    }

    public static String convert(long num) {
        if (num < 1)
            throw new IllegalArgumentException("num must be greater than 0.");
        StringBuilder sb = new StringBuilder();
        for (; num > 0; num /= 62) {
            sb.append(ALPHABET.charAt((int) (num % 62)));
        }
        return sb.toString();
    }

    public static long revert(String str) {
        if (str == null || str.trim().length() == 0) {
            throw new IllegalArgumentException("str must not be empty.");
        }
        long result = 0;
        BigInteger bs = BigInteger.valueOf(62);
        for (int i = 0; i < str.length(); i++) {
            result += ALPHABET.indexOf(str.charAt(i)) * bs.pow(i).longValue();
        }
        return result;
    }

}
