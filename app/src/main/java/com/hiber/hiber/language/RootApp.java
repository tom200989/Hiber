package com.hiber.hiber.language;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

public class RootApp extends Application {

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
