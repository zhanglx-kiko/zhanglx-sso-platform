package com.zhanglx.sso.mybatis.utils;

import cn.hutool.core.lang.Singleton;

/**
 * 作者：Zhang L X
 * 创建时间：2026/3/26 17:59
 * 类名：SnowFlakeUtils
 * 说明：
 */
public class SnowFlakeUtils {

    private static final long START_STMP = 1420041600000L;
    private static final long SEQUENCE_BIT = 9L;
    private static final long MACHINE_BIT = 2L;
    private static final long DATACENTER_BIT = 2L;
    private static final long MAX_SEQUENCE = 511L;
    private static final long MAX_MACHINE_NUM = 3L;
    private static final long MAX_DATACENTER_NUM = 3L;
    private static final long MACHINE_LEFT = 9L;
    private static final long DATACENTER_LEFT = 11L;
    private static final long TIMESTMP_LEFT = 13L;
    /**
     * datacenter标识。
     */
    private final long datacenterId;
    /**
     * machine标识。
     */
    private final long machineId;
    /**
     * sequence。
     */
    private long sequence = 0L;
    /**
     * 上次时间戳。
     */
    private long lastSTmp = -1L;

    public SnowFlakeUtils(long datacenterId, long machineId) {
        if (datacenterId <= 3L && datacenterId >= 0L) {
            if (machineId <= 3L && machineId >= 0L) {
                this.datacenterId = datacenterId;
                this.machineId = machineId;
            } else {
                throw new IllegalArgumentException("machineId can't be greater than MAX_MACHINE_NUM or less than 0");
            }
        } else {
            throw new IllegalArgumentException("datacenterId can't be greater than MAX_DATACENTER_NUM or less than 0");
        }
    }

    /**
     * 生成ID
     *
     * @return
     */
    public static Long generateId() {
        return Singleton.get(SnowFlakeUtils.class, new Object[]{1L, 1L}).nextId();
    }

    public synchronized long nextId() {
        long currSTmp = this.getNewsTmp();
        if (currSTmp < this.lastSTmp) {
            throw new RuntimeException("Clock moved backwards.  Refusing to generate id");
        } else {
            if (currSTmp == this.lastSTmp) {
                this.sequence = this.sequence + 1L & 511L;
                if (this.sequence == 0L) {
                    currSTmp = this.getNextMill();
                }
            } else {
                this.sequence = 0L;
            }

            this.lastSTmp = currSTmp;
            return currSTmp - 1420041600000L << 13 | this.datacenterId << 11 | this.machineId << 9 | this.sequence;
        }
    }

    /**
     * 获取下一个可用时间戳。
     */
    private long getNextMill() {
        long mill;
        for (mill = this.getNewsTmp(); mill <= this.lastSTmp; mill = this.getNewsTmp()) {
        }
        return mill;
    }

    /**
     * 获取当前系统时间戳。
     */
    private long getNewsTmp() {
        return System.currentTimeMillis();
    }

}