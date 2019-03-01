package com.hiber.hiber.language;

import android.content.Context;
import android.content.res.Configuration;
import android.support.multidex.MultiDexApplication;

/* 必须使用MultiDexApplication配合依赖multidex:1.0.1使用 */
public class RootApp extends MultiDexApplication {

    @Override
    public void onCreate() {
        super.onCreate();
        LangHelper.init(this);
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
