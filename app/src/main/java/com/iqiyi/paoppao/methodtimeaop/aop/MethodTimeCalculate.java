package com.iqiyi.paoppao.methodtimeaop.aop;

import android.util.Log;


public class MethodTimeCalculate {
    private static final String TAG = "MethodTimeCalculate";
    private Time mTime;
    private String className, methodName;
    private String methodDesc;
    private String fullMethodInfo;

    public static MethodTimeCalculate startTime(String className, String methodName,
            String methodDesc) {
        MethodTimeCalculate methodTimeCalculate = new MethodTimeCalculate();
        methodTimeCalculate.className = className;
        methodTimeCalculate.methodName = methodName;
        methodTimeCalculate.methodDesc = methodDesc;
        methodTimeCalculate.mTime = new Time();
        methodTimeCalculate.mTime.start();
        methodTimeCalculate.fullMethodInfo = className + "..." + methodName;
        return methodTimeCalculate;
    }

    public static void endTime(MethodTimeCalculate methodTimeCalculate) {
        methodTimeCalculate.mTime.stop();
        Log.i(TAG, methodTimeCalculate.fullMethodInfo + " cost:"
                + methodTimeCalculate.mTime.getTotalTime() + "ms");
    }


}
