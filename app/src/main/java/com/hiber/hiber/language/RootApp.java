package com.hiber.hiber.language;

import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.support.multidex.MultiDexApplication;

import com.hiber.hiber.ActivityHelper;
import com.hiber.hiber.CrashHelper;

import java.util.ArrayList;
import java.util.List;

/* 必须使用MultiDexApplication配合依赖multidex:1.0.1使用 */
public class RootApp extends MultiDexApplication {

    /**
     * Activity统一管理集合
     */
    public static List<Activity> activities = new ArrayList<>();
    public static String TAG = "RootApp";

    @Override
    public void onCreate() {
        super.onCreate();
        // 语言工具初始化
        LangHelper.init(this);
        // 全局异常捕获工具初始化
        CrashHelper crashHelper = new CrashHelper();
        crashHelper.setCrash(this);
        // 初始化Activity统一管理
        initActicityLife();
    }

    /**
     * 初始化Activity统一管理
     */
    private void initActicityLife() {
        registerActivityLifecycleCallbacks(new ActivityHelper());
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
