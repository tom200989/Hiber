package com.hiber.hiber.language;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.support.multidex.MultiDexApplication;
import android.support.v4.content.ContextCompat;

import com.hiber.hiber.ActivityHelper;
import com.hiber.hiber.CrashHelper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/* 必须使用MultiDexApplication配合依赖multidex:1.0.1使用 */
public class RootApp extends MultiDexApplication {

    /**
     * Activity统一管理集合
     */
    public static List<Activity> activities = new ArrayList<>();
    public static String TAG = "RootApp";
    public static Activity TOP_ACTIVITY;// 当前置顶的activity

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
        // 兼容调起摄像头--android 7.0以上系统解决拍照的问题
        impactCamera();
        // 创建日志文件夹
        createLogDir();
    }

    /**
     * 创建日志文件夹
     */
    private void createLogDir() {
        synchronized (Object.class) {
            /* 定义LOG文件的格式: sdcard/applications/log/159289121238798 */
            // 1.获取读写权限
            int wrPer = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
            int rePer = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
            // 2.如果读写权限通过
            if (wrPer == PackageManager.PERMISSION_GRANTED & rePer == PackageManager.PERMISSION_GRANTED) {
                // 3.查询文件夹是否存在 -- 兼容android Q sdk = 29
                String appsPath;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    appsPath = Objects.requireNonNull(getExternalFilesDir(null)).getAbsolutePath() + "/applications";
                } else {
                    appsPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/applications";
                }
                File appFile = new File(appsPath);
                // 4.如果［applications］文件夹不存在 -- 创建
                if (!appFile.exists() || !appFile.isDirectory()) {
                    // 4.1.创建 applications 目录
                    appFile.mkdirs();
                    // 4.2.再创建 log 目录
                    File logFile = new File(appFile, "log");
                    logFile.mkdirs();

                } else {
                    // 4.如果［applications］文件夹存在
                    List<File> toDelFileList = new ArrayList<>();
                    File[] files = appFile.listFiles();
                    boolean isLogDirExists = false;// 检查Log文件夹是否存在
                    for (File tempF : files) {
                        // 4.1.并且找到了log文件夹
                        if (tempF.isDirectory() & tempF.getName().contains("log")) {
                            isLogDirExists = true;
                            // 4.2.遍历并删除7天前的文件
                            File[] logFiles = tempF.listFiles();
                            if (logFiles.length > 0) {
                                // 4.3.获取当前时间并计算出7天前的最值
                                long currentTime = System.currentTimeMillis();
                                long sevenTime = 7 * 24 * 3600 * 1000;
                                long minTime = currentTime - sevenTime;
                                // 4.4.取出文件名并比较, 如果小于最小值则删除这个文件夹
                                for (File logFile : logFiles) {
                                    long logFileName = Long.valueOf(logFile.getName());
                                    if (logFileName < minTime) {
                                        toDelFileList.add(logFile);
                                    }
                                }
                                // 4.5.遍历临时删除集合并删除对应文件
                                for (int i = 0; i < toDelFileList.size(); i++) {
                                    toDelFileList.get(i).delete();
                                }
                                toDelFileList.clear();
                            }
                        }
                    }
                    // 判断最终是否没有LOG文件夹 -- 无:创建
                    if (!isLogDirExists) {
                        File logFile = new File(appFile, "log");
                        logFile.mkdirs();
                    }
                }
            }
        }
    }

    /**
     * 兼容调起摄像头--android 7.0以上系统解决拍照的问题
     */
    private void impactCamera() {
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();
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
