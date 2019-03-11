package com.hiber.hiber.language;

import android.content.Context;
import android.content.res.Configuration;
import android.support.multidex.MultiDexApplication;

import com.hiber.hiber.CrashHelper;

/* 必须使用MultiDexApplication配合依赖multidex:1.0.1使用 */
public class RootApp extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        // 语言工具初始化
        LangHelper.init(this);
        // 全局异常捕获工具初始化
        CrashHelper crashHelper = new CrashHelper();
        crashHelper.setCrash(this);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LangHelper.init(this);
    }

    @Override
    protected void attachBaseContext(Context context) {
        super.attachBaseContext(LangHelper.getContext(context));
    }
}
