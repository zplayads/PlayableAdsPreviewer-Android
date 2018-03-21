package com.zplay.zplayads;

import android.app.Application;

import com.umeng.commonsdk.UMConfigure;

/**
 * Description:
 * <p>
 * Created by lgd on 2018/3/20.
 */

public class MainApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        UMConfigure.init(this, UMConfigure.DEVICE_TYPE_PHONE, "5ab0e6ed8f4a9d59e100038b");
    }

}
