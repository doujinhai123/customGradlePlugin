package com.iqiyi.paoppao.methodtimeaop;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

import paopao.iqiyi.com.moduleone.TestInsetCode;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        testTime();
        TestInsetCode.testOne("");
    }

    private String testTime() {
        for (int i = 0; i < 10000; i++) {
            Log.d("sssssss", "");
        }
        return "";
    }
}
