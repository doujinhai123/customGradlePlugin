package com.iqiyi.paoppao.methodtimeaop.aop;

import android.os.SystemClock;

import java.text.NumberFormat;

/**
 * Created by LiYong on 2018/9/25
 *
 * Email:liyong@qiyi.com / lee131483@gmail.com
 */
public class Time {
    private long startTime;
    private long endTime;
    private long elapsedTime;

    public void reset() {
        startTime = 0;
        endTime = 0;
        elapsedTime = 0;
    }

    public void start() {
        reset();
        startTime = SystemClock.elapsedRealtime();
    }

    public void stop() {
        if (startTime != 0) {
            endTime = SystemClock.elapsedRealtime();
            elapsedTime = endTime - startTime;
        } else {
            reset();
        }
    }

    public long getTotalTime() {
        return elapsedTime;
    }
}
