package cn.xpride.framework.utility;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

/**
 * 这是基于Twitter的snowflake算法的全局ID生成类
 */
public final class LongUtil {

    public static String joint(List<String> list) {
        return String.join("_", new TreeSet<>(list));
    }

    public static List<String> split(String s) {
        return Arrays.asList(s.split("_"));
    }

    private final static long offsetLong = 515312128000000L;
    private final static int workerIdBits = 0;//机器位0位，支持2^0=1实例,分布式时可调整满足workerIdBits+sequenceBits=9即可
    private final static int sequenceBits = 9;//自增序列9位,每毫秒2^9=512个
    private final static int maxWorkerId = ~(-1 << workerIdBits);
    private final static int workerIdShift = sequenceBits;
    private final static int milliSecondLeftShift = sequenceBits + workerIdBits;
    private final static int sequenceMask = ~(-1 << sequenceBits);
    private static LongUtil[] instances = new LongUtil[maxWorkerId + 1];
    private int workerId = 0;
    private int sequence = 0;
    private long lastTimeStamp = -1L;

    private LongUtil() {
        this.workerId = 0;
    }

    private LongUtil(int workerId) {
        this.workerId = workerId;
    }

    public static LongUtil getInstance() {
        if (null == instances[0]) {
            synchronized (LongUtil.class) {
                if (null == instances[0]) {
                    instances[0] = new LongUtil();
                }
            }
        }
        return instances[0];
    }

    public static LongUtil getInstance(int workerId) {
        if (workerId > maxWorkerId || workerId < 0) {
            throw new IllegalArgumentException(String.format(
                "workerId can't be greater than %d or less than 0",
                maxWorkerId));
        }
        if (null == instances[workerId]) {
            synchronized (LongUtil.class) {
                if (null == instances[workerId]) {
                    instances[workerId] = new LongUtil(workerId);
                }
            }
        }
        return instances[workerId];
    }

    private static long generateTimeStamp() {
        return System.currentTimeMillis();
    }

    private static long tillNextTimeStamp(long lastTimeStamp) {
        long nowTimeStamp = generateTimeStamp();
        while (nowTimeStamp <= lastTimeStamp) {
            nowTimeStamp = generateTimeStamp();
        }
        return nowTimeStamp;
    }

    public int getWorkerId() {
        return workerId;
    }

    public long nextId() {
        synchronized (LongUtil.class) {
            long nowTimeStamp = generateTimeStamp();
            if (this.lastTimeStamp == nowTimeStamp) {
                this.sequence = (this.sequence + 1)
                    & LongUtil.sequenceMask;
                if (this.sequence == 0L) {
                    nowTimeStamp = tillNextTimeStamp(nowTimeStamp);
                }
            } else {
                this.sequence = 0;
            }
            if (nowTimeStamp < this.lastTimeStamp) {
                try {
                    throw new Exception(
                        String.format(
                            "Clock moved backwards.  Refusing to generate id for %d milliseconds",
                            this.lastTimeStamp - nowTimeStamp));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            this.lastTimeStamp = nowTimeStamp;
            return LongUtil.offsetLong
                + ((nowTimeStamp << LongUtil.milliSecondLeftShift)
                | (this.workerId << LongUtil.workerIdShift) | (this.sequence));
        }
    }

}
