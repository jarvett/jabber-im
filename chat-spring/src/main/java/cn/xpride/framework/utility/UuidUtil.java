package cn.xpride.framework.utility;

import java.util.Random;
import java.util.UUID;

public final class UuidUtil {

    private final static char[] DIGITS62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"
        .toCharArray();
    private static Random random = new Random();

    public static String uuid() {
        return uuidFast();
    }

    /**
     * 数字加大小写字母混合
     *
     * @param len 生成长度
     * @return
     */
    public static String uuid(int len) {
        return pruuid(len, DIGITS62.length, 0);
    }

    /**
     * 纯数字
     *
     * @param len 生成长度
     * @return
     */
    public static String number(int len) {
        return pruuid(len, 10, 0);
    }

    /**
     * 纯大写字母
     *
     * @param len 生成长度
     * @return
     */
    public static String letter(int len) {
        return pruuid(len, 26, 10);
    }

    /**
     * 数字加大写字母混合
     *
     * @param len 生成长度
     * @return
     */
    public static String hybrid(int len) {
        return pruuid(len, 36, 0);
    }

    /**
     * 随机16进制字符串
     *
     * @param len
     * @return
     */
    public static String hexString(int len) {
        return pruuid(len, 16, 0);
    }

    /**
     * radix=10,stIndex=0 ===纯数字<br/>
     * radix=26,stIndex=10 ===纯小写字母<br/>
     * radix=36,stIndex=0 ===数字加小写字母<br/>
     *
     * @param len     生成长度
     * @param radix   随机范围,1-61
     * @param stIndex 开始位置,DIGITS62的索引0-61
     * @return
     */
    private static String pruuid(int len, int radix, int stIndex) {
        if (0 >= len) {
            len = 6;
        }
        char[] uuid = new char[len];
        stIndex = stIndex % DIGITS62.length;
        stIndex = stIndex < 0 ? -stIndex : stIndex;
        radix = radix <= 0 ? (DIGITS62.length - stIndex) : radix;
        radix = radix > (DIGITS62.length - stIndex) ? (DIGITS62.length - stIndex) : radix;
        for (int i = 0; i < len; i++) {
            uuid[i] = DIGITS62[random.nextInt(radix) + stIndex];
        }
        return new String(uuid);
    }

    public static String uuidCompact() {
        return UUID.randomUUID().toString();
    }

    public static String uuidFast() {
        char[] uuid = new char[36];
        int rnd = 0, r = 0;
        for (int i = 0; i < 36; i++) {
            if (i == 8 || i == 13 || i == 18 || i == 23) {
                uuid[i] = '-';
            } else if (i == 14) {
                uuid[i] = '4';
            } else {
                if (rnd <= 0x02)
                    rnd = 0x2000000 + (random.nextInt(0x1000000));
                r = rnd & 0xf;
                rnd = rnd >> 4;
                uuid[i] = DIGITS62[(i == 19) ? (r & 0x3) | 0x8 : r];
            }
        }
        return new String(uuid);
    }

    public static String uuidSpe() {
        char[] uuid = new char[32];
        int rnd = 0, r = 0;
        for (int i = 0; i < 32; i++) {
            if (i == 12) {
                uuid[i] = '4';
            } else {
                if (rnd <= 0x02)
                    rnd = 0x2000000 + (random.nextInt(0x1000000));
                r = rnd & 0xf;
                rnd = rnd >> 4;
                uuid[i] = DIGITS62[(i == 16) ? (r & 0x3) | 0x8 : r];
            }
        }
        return new String(uuid);
    }

    public static Long getSerialVersionUID() {
        return UUID.randomUUID().getMostSignificantBits();
    }


    public static String getLongShortId(){
        return RadixUtil.toString(LongUtil.getInstance().nextId());
    }

}
