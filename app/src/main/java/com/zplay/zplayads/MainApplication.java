package com.zplay.zplayads;

import android.app.Application;

import com.tencent.bugly.crashreport.CrashReport;

/**
 * Description:
 * <p>
 * Created by lgd on 2018/3/20.
 */

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        CrashReport.initCrashReport(getApplicationContext(), "be7ee661c6", false);
    }

}
